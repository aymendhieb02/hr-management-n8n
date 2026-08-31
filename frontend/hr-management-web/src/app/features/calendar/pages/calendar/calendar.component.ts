import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveRequestService } from '../../../leaves/services/leave-request.service';
import { UserService } from '../../../users/services/user.service';
import { WeekPlannerComponent } from '../../components/week-planner/week-planner.component';
import { CalendarEvent } from '../../models/calendar-event.model';
import { CalendarService } from '../../services/calendar.service';
import { JourFerieService } from '../../../jours-feries/services/jour-ferie.service';

@Component({
  selector: 'app-calendar',
  imports: [FormsModule, WeekPlannerComponent],
  templateUrl: './calendar.component.html',
  styleUrls: ['../../../shared/resource-page.scss', './calendar.component.scss']
})
export class CalendarComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);
  private readonly leaveRequests = inject(LeaveRequestService);
  private readonly users = inject(UserService);
  private readonly calendar = inject(CalendarService);
  private readonly holidays = inject(JourFerieService);
  protected readonly month = signal(new Date());
  protected readonly events = signal<CalendarEvent[]>([]);
  protected readonly leaveTypeFilter = signal('');
  protected readonly statusFilter = signal('');
  protected readonly departmentFilter = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly title = computed(() => this.route.snapshot.routeConfig?.path === 'team-calendar' ? "Calendrier de l'équipe" : this.route.snapshot.routeConfig?.path === 'calendar' ? 'Calendrier global' : 'Mon calendrier');
  protected readonly filteredEvents = computed(() => this.events()
    .filter((event) => event.kind === 'HOLIDAY' || !this.leaveTypeFilter() || event.leaveTypeName === this.leaveTypeFilter())
    .filter((event) => event.kind === 'HOLIDAY' || !this.statusFilter() || event.status === this.statusFilter())
    .filter((event) => !this.departmentFilter() || event.departmentName === this.departmentFilter()));
  protected readonly leaveTypes = computed(() => [...new Set(this.events().filter((event) => event.kind !== 'HOLIDAY').map((event) => event.leaveTypeName))]);
  protected readonly statuses = computed(() => [...new Set(this.events().filter((event) => event.kind !== 'HOLIDAY' && !!event.status).map((event) => event.status))]);
  protected readonly departments = computed(() => [...new Set(this.events().map((event) => event.departmentName).filter(Boolean) as string[])]);

  ngOnInit(): void { this.load(); }

  protected load(): void {
    const user = this.auth.getCurrentUser();
    if (!user) return;
    this.loading.set(true);
    const path = this.route.snapshot.routeConfig?.path;
    // DG et DT disposent d'une vision complète du calendrier de l'équipe.
    const visionGlobale = user.role === 'DG' || user.role === 'DT';
    const requests$ = path === 'calendar' || (path === 'team-calendar' && visionGlobale)
      ? this.leaveRequests.findAll()
      : path === 'team-calendar' ? this.leaveRequests.findByApprover(user.id) : this.leaveRequests.findByRequester(user.id);
    forkJoin({ requests: requests$, users: path === 'calendar' ? this.users.findAll() : of([]), holidays: this.holidays.getActive() }).subscribe({
      next: ({ requests, users, holidays }) => {
        const holidayEvents: CalendarEvent[] = holidays.map((holiday) => ({
          kind: 'HOLIDAY', leaveRequestId: -holiday.id, userId: 0, employeeName: holiday.nom,
          leaveTypeName: 'Jour férié', departmentName: null, startDate: holiday.date, endDate: holiday.date,
          status: '', nature: 'CONGE', startTime: null, endTime: null, reason: null, description: holiday.description
        }));
        this.events.set([...this.calendar.toEvents(requests, users), ...holidayEvents]);
        this.loading.set(false);
      },
      error: () => { this.error.set("Les données du calendrier n'ont pas pu être chargées."); this.loading.set(false); }
    });
  }

  protected previousMonth(): void { this.month.update((date) => new Date(date.getFullYear(), date.getMonth() - 1, 1)); }
  protected nextMonth(): void { this.month.update((date) => new Date(date.getFullYear(), date.getMonth() + 1, 1)); }
  protected previousWeek(): void { this.month.update((date) => new Date(date.getFullYear(), date.getMonth(), date.getDate() - 7)); }
  protected nextWeek(): void { this.month.update((date) => new Date(date.getFullYear(), date.getMonth(), date.getDate() + 7)); }
  protected today(): void { this.month.set(new Date()); }
  protected selectDate(dateKey: string): void {
    const [year, month, day] = dateKey.split('-').map(Number);
    this.month.set(new Date(year, month - 1, day, 12));
  }
  protected statusLabel(status: string): string {
    return ({ DRAFT: 'Brouillon', APPROVED: 'Approuvée', PENDING: 'En attente', REJECTED: 'Refusée', CANCELLED: 'Annulée' } as Record<string, string>)[status] ?? status.replaceAll('_', ' ');
  }
}
