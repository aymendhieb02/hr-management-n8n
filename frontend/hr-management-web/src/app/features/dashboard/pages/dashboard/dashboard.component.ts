import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
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
  protected readonly pending = computed(() => this.count('PENDING'));
  protected readonly approved = computed(() => this.count('APPROVED'));
  protected readonly rejected = computed(() => this.count('REJECTED'));
  protected readonly absentToday = computed(() => this.requestList().filter((r) => r.status === 'APPROVED' && isTodayWithin(r.startDate, r.endDate)).length);
  protected readonly activeEmployees = computed(() => this.userList().filter((user) => user.enabled).length);
  protected readonly authorizations = computed(() => this.requestList().filter((request) => request.nature === 'AUTORISATION_ABSENCE').length);
  protected readonly approvalRate = computed(() => {
    const decided = this.approved() + this.rejected();
    return decided ? Math.round((this.approved() / decided) * 100) : 0;
  });
  protected readonly upcomingApproved = computed(() => this.requestList().filter((request) => request.status === 'APPROVED' && request.startDate >= new Date().toISOString().slice(0,10)).length);
  protected readonly recent = computed(() => [...this.requestList()].sort((a, b) => b.submittedAt.localeCompare(a.submittedAt)).slice(0, 8));
  protected readonly statusData = computed(() => [{label:'En attente',status:'PENDING'},{label:'Approuvées',status:'APPROVED'},{label:'Refusées',status:'REJECTED'}].map((item) => ({ label:item.label, value:this.count(item.status) })));

  ngOnInit(): void {
    const current = this.auth.getCurrentUser();
    if (!current) return;
    this.loading.set(true);
    forkJoin({
      users: this.users.findAll(),
      requests: this.requests.findAll(),
      unread: this.notifications.findUnread(current.id)
    }).subscribe({
      next: ({ users, requests, unread }) => {
        this.userList.set(users);
        this.requestList.set(requests);
        this.unreadCount.set(unread.length);
        this.loading.set(false);
      },
      error: () => { this.error.set('Impossible de charger les données du tableau de bord.'); this.loading.set(false); }
    });
  }

  private count(status: string): number {
    return this.requestList().filter((request) => request.status === status).length;
  }
}
