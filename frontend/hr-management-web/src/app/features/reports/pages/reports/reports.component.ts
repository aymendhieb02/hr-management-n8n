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
      error: () => { this.error.set('Impossible de charger les données des rapports.'); this.loading.set(false); }
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
        .map((r) => ({ Employé: `${r.requester.firstName} ${r.requester.lastName}`, Type: r.leaveType.name, Début: r.startDate, Fin: r.endDate, Statut: this.statusLabel(r.status), Jours: r.requestedDays }));
    }
    if (this.reportType() === 'leave-balances') {
      return this.leaveBalances().filter((b) => !f.year || b.year === Number(f.year)).filter((b) => !f.leaveType || b.leaveType.name === f.leaveType).map((b) => ({ Employé: `${b.user.firstName} ${b.user.lastName}`, Type: b.leaveType.name, Année: b.year, Acquis: b.totalDays, Utilisés: b.usedDays, Restants: b.remainingDays }));
    }
    if (this.reportType() === 'employees') {
      return this.users().map((u) => ({ Nom: `${u.firstName} ${u.lastName}`, Identifiant: u.username, Email: u.email, Rôle: this.roleLabel(u.role), Poste: u.position?.name ?? 'Non renseigné', Contrat: u.typeContract?.name ?? 'Non renseigné', Statut: u.enabled ? 'Actif' : 'Inactif' }));
    }
    const rows = new Map<string, number>();
    for (const request of this.leaveRequests().filter((r) => overlapsRange(r.startDate, r.endDate, f.startDate || undefined, f.endDate || undefined))) {
      const department = this.users().find((u) => u.id === request.requester.id)?.department?.name ?? 'No department';
      rows.set(department, (rows.get(department) ?? 0) + 1);
    }
    return [...rows.entries()].map(([Department, Requests]) => ({ Department, Requests }));
  }

  private statusLabel(status: LeaveRequestResponse['status']): string { return { DRAFT:'Brouillon', PENDING:'En attente', APPROVED:'Approuvée', REJECTED:'Refusée', CANCELLED:'Annulée' }[status]; }
  private roleLabel(role: UserResponse['role']): string { return { EMPLOYEE:'Employé', MANAGER:'Directeur général', DG:'Directeur général', DT:'Directeur technique', HR:'Ressources humaines', ADMIN:'Administrateur' }[role]; }
}
