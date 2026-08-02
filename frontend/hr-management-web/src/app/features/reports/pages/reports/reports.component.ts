import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { downloadCsv, overlapsRange } from '../../../../shared/utils/report-utils';
import { LeaveBalanceResponse } from '../../../leaves/models/leave-balance.model';
import { LeaveRequestResponse } from '../../../leaves/models/leave-request.model';
import { UserResponse } from '../../../users/models/user.model';
import { ReportFilterComponent } from '../../components/report-filter/report-filter.component';
import { ReportTableComponent } from '../../components/report-table/report-table.component';
import { ReportFilters, ReportType } from '../../models/report.model';
import { ReportService } from '../../services/report.service';

@Component({
  selector: 'app-reports',
  imports: [ReportFilterComponent, ReportTableComponent],
  templateUrl: './reports.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class ReportsComponent implements OnInit {
  private readonly reportService = inject(ReportService);
  protected readonly reportType = signal<ReportType>('leave-requests');
  protected readonly filters = signal<ReportFilters>({ startDate: '', endDate: '', status: '', leaveType: '', department: '', userId: '', year: '' });
  protected readonly leaveRequests = signal<LeaveRequestResponse[]>([]);
  protected readonly leaveBalances = signal<LeaveBalanceResponse[]>([]);
  protected readonly users = signal<UserResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly leaveTypes = computed(() => [...new Set(this.leaveRequests().map((r) => r.leaveType.name))]);
  protected readonly departments = computed(() => [...new Set(this.users().map((u) => u.department?.name).filter(Boolean) as string[])]);
  protected readonly userOptions = computed(() => this.users().map((u) => ({ id: u.id, name: `${u.firstName} ${u.lastName}` })));
  protected readonly rows = computed(() => this.buildRows());

  ngOnInit(): void {
    this.loading.set(true);
    this.reportService.loadData().subscribe({
      next: ({ leaveRequests, leaveBalances, users }) => { this.leaveRequests.set(leaveRequests); this.leaveBalances.set(leaveBalances); this.users.set(users); this.loading.set(false); },
      error: () => { this.error.set('Reports data could not be loaded.'); this.loading.set(false); }
    });
  }

  protected exportCsv(): void {
    downloadCsv(this.rows(), `${this.reportType()}-${new Date().toISOString().slice(0, 10)}.csv`);
  }

  private buildRows(): Record<string, unknown>[] {
    const f = this.filters();
    if (this.reportType() === 'leave-requests') {
      return this.leaveRequests()
        .filter((r) => !f.status || r.status === f.status)
        .filter((r) => !f.leaveType || r.leaveType.name === f.leaveType)
        .filter((r) => !f.userId || r.requester.id === Number(f.userId))
        .filter((r) => overlapsRange(r.startDate, r.endDate, f.startDate || undefined, f.endDate || undefined))
        .map((r) => ({ Employee: `${r.requester.firstName} ${r.requester.lastName}`, Type: r.leaveType.name, Start: r.startDate, End: r.endDate, Status: r.status, Days: r.requestedDays }));
    }
    if (this.reportType() === 'leave-balances') {
      return this.leaveBalances().filter((b) => !f.year || b.year === Number(f.year)).filter((b) => !f.leaveType || b.leaveType.name === f.leaveType).map((b) => ({ Employee: `${b.user.firstName} ${b.user.lastName}`, Type: b.leaveType.name, Year: b.year, Total: b.totalDays, Used: b.usedDays, Remaining: b.remainingDays }));
    }
    if (this.reportType() === 'employees') {
      return this.users().filter((u) => !f.department || u.department?.name === f.department).map((u) => ({ Name: `${u.firstName} ${u.lastName}`, Email: u.email, Role: u.role, Department: u.department?.name ?? '', Position: u.position?.name ?? '' }));
    }
    const rows = new Map<string, number>();
    for (const request of this.leaveRequests().filter((r) => overlapsRange(r.startDate, r.endDate, f.startDate || undefined, f.endDate || undefined))) {
      const department = this.users().find((u) => u.id === request.requester.id)?.department?.name ?? 'No department';
      rows.set(department, (rows.get(department) ?? 0) + 1);
    }
    return [...rows.entries()].map(([Department, Requests]) => ({ Department, Requests }));
  }
}
