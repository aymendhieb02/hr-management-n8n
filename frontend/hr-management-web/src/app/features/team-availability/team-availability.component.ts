import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { JourFerieResponse } from '../jours-feries/models/jour-ferie.model';
import { JourFerieService } from '../jours-feries/services/jour-ferie.service';
import { LeaveRequestResponse } from '../leaves/models/leave-request.model';
import { LeaveRequestService } from '../leaves/services/leave-request.service';
import { UserResponse } from '../users/models/user.model';
import { UserService } from '../users/services/user.service';
import { AppIconComponent } from '../../shared/components/app-icon/app-icon.component';

type AvailabilityKind = 'AVAILABLE' | 'LEAVE' | 'AUTHORIZATION' | 'HOLIDAY';
interface AvailabilityState { kind: AvailabilityKind; label: string; detail: string; }

@Component({
  selector: 'app-team-availability',
  imports: [AppIconComponent, FormsModule],
  templateUrl: './team-availability.component.html',
  styleUrl: './team-availability.component.scss'
})
export class TeamAvailabilityComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly holidayService = inject(JourFerieService);

  protected readonly members = signal<UserResponse[]>([]);
  protected readonly requests = signal<LeaveRequestResponse[]>([]);
  protected readonly holidays = signal<JourFerieResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly search = signal('');
  protected readonly selectedDate = signal(this.toLocalDate(new Date()));
  protected readonly week = computed(() => this.weekDates(this.selectedDate()));
  protected readonly filteredMembers = computed(() => {
    const value = this.search().trim().toLowerCase();
    return this.members().filter((member) => !value || `${member.firstName} ${member.lastName} ${member.position?.name ?? ''}`.toLowerCase().includes(value));
  });
  protected readonly selectedSummary = computed(() => {
    const states = this.members().map((member) => this.state(member, this.selectedDate()));
    return {
      total: states.length,
      available: states.filter((item) => item.kind === 'AVAILABLE').length,
      absent: states.filter((item) => item.kind === 'LEAVE').length,
      authorization: states.filter((item) => item.kind === 'AUTHORIZATION').length
    };
  });

  ngOnInit(): void {
    const manager = this.authService.getCurrentUser();
    if (!manager) return;
    forkJoin({
      members: this.userService.findTeamMembers(manager.id),
      requests: (manager.role === 'DG' || manager.role === 'DT') ? this.leaveRequestService.findAll() : this.leaveRequestService.findByApprover(manager.id),
      holidays: this.holidayService.getActive()
    }).subscribe({
      next: ({ members, requests, holidays }) => {
        this.members.set(members.filter((member) => member.enabled));
        this.requests.set(requests.filter((request) => request.status === 'APPROVED'));
        this.holidays.set(holidays);
        this.loading.set(false);
      },
      error: () => { this.error.set("Impossible de charger la disponibilité de l'équipe."); this.loading.set(false); }
    });
  }

  protected state(member: UserResponse, date: string): AvailabilityState {
    const holiday = this.holidays().find((item) => item.actif && item.date === date);
    if (holiday) return { kind: 'HOLIDAY', label: 'Jour férié', detail: holiday.nom };
    const request = this.requests().find((item) => item.requester.id === member.id && item.startDate <= date && item.endDate >= date);
    if (!request) return { kind: 'AVAILABLE', label: 'Disponible', detail: 'Journée disponible' };
    if (request.nature === 'AUTORISATION_ABSENCE') {
      return { kind: 'AUTHORIZATION', label: 'Autorisation', detail: `${request.startTime?.slice(0, 5)} – ${request.endTime?.slice(0, 5)}` };
    }
    return { kind: 'LEAVE', label: 'En congé', detail: request.reason || request.leaveType.name };
  }

  protected moveWeek(offset: number): void {
    const date = new Date(`${this.selectedDate()}T12:00:00`);
    date.setDate(date.getDate() + offset * 7);
    this.selectedDate.set(this.toLocalDate(date));
  }
  protected goToCurrentWeek(): void { this.selectedDate.set(this.toLocalDate(new Date())); }

  protected initials(member: UserResponse): string { return `${member.firstName[0] ?? ''}${member.lastName[0] ?? ''}`.toUpperCase(); }
  protected dayName(date: string): string { return new Intl.DateTimeFormat('fr-FR', { weekday: 'short' }).format(new Date(`${date}T12:00:00`)); }
  protected dayNumber(date: string): string { return new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: '2-digit' }).format(new Date(`${date}T12:00:00`)); }
  protected fullDate(date: string): string { return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'long' }).format(new Date(`${date}T12:00:00`)); }

  private weekDates(value: string): string[] {
    const monday = new Date(`${value}T12:00:00`);
    const day = monday.getDay() || 7;
    monday.setDate(monday.getDate() - day + 1);
    return Array.from({ length: 7 }, (_, index) => { const date = new Date(monday); date.setDate(monday.getDate() + index); return this.toLocalDate(date); });
  }
  private toLocalDate(date: Date): string {
    const offset = date.getTimezoneOffset() * 60_000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 10);
  }
}
