import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { isTodayWithin } from '../../../../shared/utils/report-utils';
import { LeaveRequestResponse } from '../../../leaves/models/leave-request.model';
import { LeaveRequestService } from '../../../leaves/services/leave-request.service';
import { NotificationService } from '../../../notifications/services/notification.service';
import { UserResponse } from '../../../users/models/user.model';
import { UserService } from '../../../users/services/user.service';
import { RecentLeaveTableComponent } from '../../components/recent-leave-table/recent-leave-table.component';
import { AppIconComponent } from '../../../../shared/components/app-icon/app-icon.component';

interface ChartItem { label: string; value: number; percent: number; tone?: string; }
interface MonthActivity { label: string; submitted: number; approved: number; }

@Component({
  selector: 'app-dashboard',
  imports: [AppIconComponent, RecentLeaveTableComponent, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['../../../shared/resource-page.scss', './dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly requests = inject(LeaveRequestService);
  private readonly notifications = inject(NotificationService);
  private readonly auth = inject(AuthService);

  protected readonly userList = signal<UserResponse[]>([]);
  protected readonly requestList = signal<LeaveRequestResponse[]>([]);
  protected readonly unreadCount = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly lastUpdated = signal<Date | null>(null);

  protected readonly currentUser = computed(() => this.auth.getCurrentUser());
  protected readonly isManager = computed(() => ['DG','DT'].includes(this.currentUser()?.role ?? ''));
  protected readonly pending = computed(() => this.count('PENDING'));
  protected readonly approved = computed(() => this.count('APPROVED'));
  protected readonly rejected = computed(() => this.count('REJECTED'));
  protected readonly cancelled = computed(() => this.count('CANCELLED'));
  protected readonly activeEmployees = computed(() => this.userList().filter((user) => user.enabled && user.status === 'ACTIVE').length);
  protected readonly blockedEmployees = computed(() => this.userList().filter((user) => !user.enabled).length);
  protected readonly absentToday = computed(() => this.requestList().filter((request) => request.status === 'APPROVED' && isTodayWithin(request.startDate, request.endDate)).length);
  protected readonly coverageRate = computed(() => this.activeEmployees() ? Math.max(0, Math.round(((this.activeEmployees() - this.absentToday()) / this.activeEmployees()) * 100)) : 100);
  protected readonly upcomingApproved = computed(() => {
    const today = this.today();
    const limit = new Date();
    limit.setDate(limit.getDate() + 30);
    const end = this.localIso(limit);
    return this.requestList().filter((request) => request.status === 'APPROVED' && request.startDate >= today && request.startDate <= end).length;
  });
  protected readonly approvalRate = computed(() => {
    const decided = this.approved() + this.rejected();
    return decided ? Math.round((this.approved() / decided) * 100) : 0;
  });
  protected readonly averageDecisionHours = computed(() => {
    const durations = this.requestList()
      .filter((request) => request.decisionAt && request.submittedAt)
      .map((request) => (new Date(request.decisionAt!).getTime() - new Date(request.submittedAt).getTime()) / 3_600_000)
      .filter((hours) => Number.isFinite(hours) && hours >= 0);
    return durations.length ? Math.round(durations.reduce((sum, value) => sum + value, 0) / durations.length) : 0;
  });
  protected readonly daysApprovedThisYear = computed(() => this.requestList()
    .filter((request) => request.status === 'APPROVED' && request.nature === 'CONGE' && request.startDate.startsWith(String(new Date().getFullYear())))
    .reduce((sum, request) => sum + (request.requestedDays || 0), 0));
  protected readonly authorizationCount = computed(() => this.requestList().filter((request) => request.nature === 'AUTORISATION_ABSENCE').length);
  protected readonly recent = computed(() => [...this.requestList()].sort((a, b) => b.submittedAt.localeCompare(a.submittedAt)).slice(0, 6));

  protected readonly statusBreakdown = computed<ChartItem[]>(() => this.toPercent([
    { label: 'En attente', value: this.pending(), tone: 'warning' },
    { label: 'Approuvées', value: this.approved(), tone: 'success' },
    { label: 'Refusées', value: this.rejected(), tone: 'danger' },
    { label: 'Annulées', value: this.cancelled(), tone: 'neutral' }
  ]));

  protected readonly roleBreakdown = computed<ChartItem[]>(() => this.toPercent([
    { label: 'Employés', value: this.roleCount('EMPLOYEE') },
    { label: 'Direction', value: this.roleCount('DG') + this.roleCount('DT') },
    { label: 'Ressources humaines', value: this.roleCount('HR') },
    { label: 'Administrateurs', value: this.roleCount('ADMIN') }
  ]));

  protected readonly leaveTypeBreakdown = computed<ChartItem[]>(() => {
    const counts = new Map<string, number>();
    this.requestList().filter((request) => request.nature === 'CONGE').forEach((request) => {
      const label = request.reason || request.leaveType?.name || 'Non précisé';
      counts.set(label, (counts.get(label) ?? 0) + 1);
    });
    return this.toPercent([...counts.entries()].map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value).slice(0, 5));
  });

  protected readonly monthlyActivity = computed<MonthActivity[]>(() => {
    const result: MonthActivity[] = [];
    const now = new Date();
    for (let offset = 5; offset >= 0; offset--) {
      const date = new Date(now.getFullYear(), now.getMonth() - offset, 1);
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      result.push({
        label: new Intl.DateTimeFormat('fr-FR', { month: 'short' }).format(date).replace('.', ''),
        submitted: this.requestList().filter((request) => request.submittedAt?.startsWith(key)).length,
        approved: this.requestList().filter((request) => request.status === 'APPROVED' && request.startDate?.startsWith(key)).length
      });
    }
    return result;
  });
  protected readonly monthlyMax = computed(() => Math.max(1, ...this.monthlyActivity().flatMap((month) => [month.submitted, month.approved])));

  ngOnInit(): void { this.loadDashboard(); }

  protected loadDashboard(): void {
    const current = this.auth.getCurrentUser();
    if (!current) return;
    this.loading.set(true);
    this.error.set(null);
    const usersSource = this.isManager() ? this.users.findTeamMembers(current.id) : this.users.findAll();
    const requestsSource = this.isManager() ? this.requests.findByApprover(current.id) : this.requests.findAll();
    forkJoin({
      users: usersSource,
      requests: requestsSource,
      unread: this.notifications.findUnread(current.id)
    }).subscribe({
      next: ({ users, requests, unread }) => {
        this.userList.set(users);
        this.requestList.set(requests);
        this.unreadCount.set(unread.length);
        this.lastUpdated.set(new Date());
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les données du tableau de bord.');
        this.loading.set(false);
      }
    });
  }

  protected barHeight(value: number): number { return Math.max(value ? 10 : 2, Math.round((value / this.monthlyMax()) * 100)); }
  protected fullName(): string {
    const user = this.currentUser();
    const name = user ? `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() : '';
    return name || 'Administrateur';
  }

  private count(status: LeaveRequestResponse['status']): number { return this.requestList().filter((request) => request.status === status).length; }
  private roleCount(role: UserResponse['role']): number { return this.userList().filter((user) => user.role === role).length; }
  private toPercent(items: Omit<ChartItem, 'percent'>[]): ChartItem[] {
    const total = items.reduce((sum, item) => sum + item.value, 0);
    return items.map((item) => ({ ...item, percent: total ? Math.round((item.value / total) * 100) : 0 }));
  }
  private today(): string { return this.localIso(new Date()); }
  private localIso(date: Date): string { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`; }
}
