import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { DepartmentService } from '../../../departments/services/department.service';
import { LeaveBalanceService } from '../../../leaves/services/leave-balance.service';
import { LeaveRequestResponse } from '../../../leaves/models/leave-request.model';
import { LeaveRequestService } from '../../../leaves/services/leave-request.service';
import { NotificationService } from '../../../notifications/services/notification.service';
import { UserResponse } from '../../../users/models/user.model';
import { UserService } from '../../../users/services/user.service';
import { isTodayWithin } from '../../../../shared/utils/report-utils';
import { RecentLeaveTableComponent } from '../../components/recent-leave-table/recent-leave-table.component';
import { StatCardComponent } from '../../components/stat-card/stat-card.component';
import { StatusChartComponent } from '../../components/status-chart/status-chart.component';

@Component({
  selector: 'app-dashboard',
  imports: [RecentLeaveTableComponent, StatCardComponent, StatusChartComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DashboardComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly requests = inject(LeaveRequestService);
  private readonly departments = inject(DepartmentService);
  private readonly balances = inject(LeaveBalanceService);
  private readonly notifications = inject(NotificationService);
  private readonly auth = inject(AuthService);

  protected readonly userList = signal<UserResponse[]>([]);
  protected readonly requestList = signal<LeaveRequestResponse[]>([]);
  protected readonly unreadCount = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly pending = computed(() => this.count('PENDING'));
  protected readonly approved = computed(() => this.count('APPROVED'));
  protected readonly rejected = computed(() => this.count('REJECTED'));
  protected readonly absentToday = computed(() => this.requestList().filter((r) => r.status === 'APPROVED' && isTodayWithin(r.startDate, r.endDate)).length);
  protected readonly recent = computed(() => [...this.requestList()].sort((a, b) => b.submittedAt.localeCompare(a.submittedAt)).slice(0, 8));
  protected readonly statusData = computed(() => ['PENDING', 'APPROVED', 'REJECTED'].map((label) => ({ label, value: this.count(label) })));
  protected readonly usersByDepartment = computed(() => {
    const map = new Map<string, number>();
    for (const user of this.userList()) map.set(user.department?.name ?? 'No department', (map.get(user.department?.name ?? 'No department') ?? 0) + 1);
    return [...map.entries()].map(([label, value]) => ({ label, value }));
  });

  ngOnInit(): void {
    const current = this.auth.getCurrentUser();
    if (!current) return;
    this.loading.set(true);
    forkJoin({
      users: this.users.findAll(),
      requests: this.requests.findAll(),
      departments: this.departments.findAll(),
      balances: this.balances.findAll(),
      unread: this.notifications.findUnread(current.id)
    }).subscribe({
      next: ({ users, requests, unread }) => {
        this.userList.set(users);
        this.requestList.set(requests);
        this.unreadCount.set(unread.length);
        this.loading.set(false);
      },
      error: () => { this.error.set('Dashboard data could not be loaded.'); this.loading.set(false); }
    });
  }

  private count(status: string): number {
    return this.requestList().filter((request) => request.status === status).length;
  }
}
