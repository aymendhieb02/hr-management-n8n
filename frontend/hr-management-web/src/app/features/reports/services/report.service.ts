import { inject, Injectable } from '@angular/core';
import { forkJoin } from 'rxjs';
import { LeaveBalanceService } from '../../leaves/services/leave-balance.service';
import { LeaveRequestService } from '../../leaves/services/leave-request.service';
import { UserService } from '../../users/services/user.service';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly leaveRequests = inject(LeaveRequestService);
  private readonly leaveBalances = inject(LeaveBalanceService);
  private readonly users = inject(UserService);

  loadData() {
    return forkJoin({
      leaveRequests: this.leaveRequests.findAll(),
      leaveBalances: this.leaveBalances.findAll(),
      users: this.users.findAll()
    });
  }
}
