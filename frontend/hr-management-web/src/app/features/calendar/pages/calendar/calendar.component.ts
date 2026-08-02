import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveRequestService } from '../../../leaves/services/leave-request.service';
import { UserService } from '../../../users/services/user.service';
import { CalendarEventListComponent } from '../../components/calendar-event-list/calendar-event-list.component';
import { MonthCalendarComponent } from '../../components/month-calendar/month-calendar.component';
import { CalendarEvent } from '../../models/calendar-event.model';
import { CalendarService } from '../../services/calendar.service';

@Component({
  selector: 'app-calendar',
  imports: [CalendarEventListComponent, FormsModule, MonthCalendarComponent],
  templateUrl: './calendar.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class CalendarComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);
  private readonly leaveRequests = inject(LeaveRequestService);
  private readonly users = inject(UserService);
  private readonly calendar = inject(CalendarService);
  protected readonly month = signal(new Date());
  protected readonly events = signal<CalendarEvent[]>([]);
  protected readonly leaveTypeFilter = signal('');
  protected readonly departmentFilter = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly title = computed(() => this.route.snapshot.routeConfig?.path === 'team-calendar' ? 'Team Calendar' : this.route.snapshot.routeConfig?.path === 'calendar' ? 'Global Calendar' : 'My Calendar');
  protected readonly filteredEvents = computed(() => this.events()
    .filter((event) => !this.leaveTypeFilter() || event.leaveTypeName === this.leaveTypeFilter())
    .filter((event) => !this.departmentFilter() || event.departmentName === this.departmentFilter()));
  protected readonly leaveTypes = computed(() => [...new Set(this.events().map((event) => event.leaveTypeName))]);
  protected readonly departments = computed(() => [...new Set(this.events().map((event) => event.departmentName).filter(Boolean) as string[])]);

  ngOnInit(): void { this.load(); }

  protected load(): void {
    const user = this.auth.getCurrentUser();
    if (!user) return;
    this.loading.set(true);
    const path = this.route.snapshot.routeConfig?.path;
    const requests$ = path === 'calendar' ? this.leaveRequests.findAll() : path === 'team-calendar' ? this.leaveRequests.findByApprover(user.id) : this.leaveRequests.findByRequester(user.id);
    forkJoin({ requests: requests$, users: path === 'calendar' ? this.users.findAll() : of([]) }).subscribe({
      next: ({ requests, users }) => { this.events.set(this.calendar.toEvents(requests, users)); this.loading.set(false); },
      error: () => { this.error.set('Calendar data could not be loaded.'); this.loading.set(false); }
    });
  }

  protected previousMonth(): void { this.month.update((date) => new Date(date.getFullYear(), date.getMonth() - 1, 1)); }
  protected nextMonth(): void { this.month.update((date) => new Date(date.getFullYear(), date.getMonth() + 1, 1)); }
  protected today(): void { this.month.set(new Date()); }
}
