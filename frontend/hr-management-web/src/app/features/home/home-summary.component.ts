import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { isTodayWithin } from '../../shared/utils/report-utils';
import { LeaveBalanceResponse } from '../leaves/models/leave-balance.model';
import { LeaveRequestResponse } from '../leaves/models/leave-request.model';
import { LeaveBalanceService } from '../leaves/services/leave-balance.service';
import { LeaveRequestService } from '../leaves/services/leave-request.service';
import { NotificationService } from '../notifications/services/notification.service';
import { UserResponse } from '../users/models/user.model';
import { UserService } from '../users/services/user.service';
import { JourFerieService } from '../jours-feries/services/jour-ferie.service';
import { JourFerieResponse } from '../jours-feries/models/jour-ferie.model';

@Component({
  selector: 'app-home-summary',
  templateUrl: './home-summary.component.html',
  styleUrls: ['../shared/resource-page.scss', './home-summary.component.scss']
})
export class HomeSummaryComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly leaveRequests = inject(LeaveRequestService);
  private readonly balances = inject(LeaveBalanceService);
  private readonly notifications = inject(NotificationService);
  private readonly users = inject(UserService);
  private readonly holidaysService = inject(JourFerieService);
  protected readonly requests = signal<LeaveRequestResponse[]>([]);
  protected readonly balanceList = signal<LeaveBalanceResponse[]>([]);
  protected readonly team = signal<UserResponse[]>([]);
  protected readonly unread = signal(0);
  protected readonly availableBalance = computed(() => Math.max(0, this.balanceList().reduce((total,balance)=>total+Number(balance.remainingDays||0),0)));
  protected readonly holidays = signal<JourFerieResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly role = computed(() => this.auth.getCurrentUser()?.role ?? 'EMPLOYEE');
  protected readonly pending = computed(() => this.count('PENDING'));
  protected readonly approved = computed(() => this.count('APPROVED'));
  protected readonly rejected = computed(() => this.count('REJECTED'));
  protected readonly absentToday = computed(() => this.requests().filter((r) => r.status === 'APPROVED' && isTodayWithin(r.startDate, r.endDate)).length);
  protected readonly nextAbsence = computed(() => this.requests().filter((r) => r.status === 'APPROVED' && r.startDate >= new Date().toISOString().slice(0, 10)).sort((a, b) => a.startDate.localeCompare(b.startDate))[0]);
  protected readonly upcomingHolidays = computed(() => this.holidays()
    .filter((holiday) => holiday.date >= new Date().toISOString().slice(0, 10))
    .sort((a, b) => a.date.localeCompare(b.date)).slice(0, 4));

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (!user) return;
    this.loading.set(true);
    const manager = user.role === 'DG' || user.role === 'DT';
    forkJoin({
      requests: manager ? this.leaveRequests.findByApprover(user.id) : this.leaveRequests.findByRequester(user.id),
      balances: this.balances.findByUser(user.id),
      unread: this.notifications.findUnread(user.id),
      team: manager ? this.users.findTeamMembers(user.id) : of([]),
      holidays: this.holidaysService.getActive()
    }).subscribe(({ requests, balances, unread, team, holidays }) => {
      this.requests.set(requests);
      this.balanceList.set(balances);
      this.unread.set(unread.length);
      this.team.set(team);
      this.holidays.set(holidays);
      this.loading.set(false);
    });
  }

  private count(status: string): number {
    return this.requests().filter((request) => request.status === status).length;
  }
}
