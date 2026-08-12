import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveType } from '../../../leave-types/models/leave-type.model';
import { LeaveTypeService } from '../../../leave-types/services/leave-type.service';
import { UserResponse } from '../../../users/models/user.model';
import { UserService } from '../../../users/services/user.service';
import { safeApiMessage } from '../../../shared/api-error.util';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';
import { LeaveBalanceFormComponent } from '../../components/leave-balance-form/leave-balance-form.component';
import { LeaveBalanceRequest, LeaveBalanceResponse, LeaveBalanceTransaction } from '../../models/leave-balance.model';
import { LeaveBalanceService } from '../../services/leave-balance.service';

@Component({
  selector: 'app-leave-balance-list',
  imports: [FormsModule, LeaveBalanceFormComponent, PaginatedTableDirective],
  templateUrl: './leave-balance-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveBalanceListComponent implements OnInit {
  private readonly balanceService = inject(LeaveBalanceService);
  private readonly leaveTypeService = inject(LeaveTypeService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly balances = signal<LeaveBalanceResponse[]>([]);
  protected readonly transactions = signal<LeaveBalanceTransaction[]>([]);
  protected readonly leaveTypes = signal<LeaveType[]>([]);
  protected readonly users = signal<UserResponse[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly leaveTypeFilter = signal<number | ''>('');
  protected readonly editingBalance = signal<LeaveBalanceResponse | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly isAdminMode = computed(() => this.route.snapshot.routeConfig?.path !== 'my-balance');
  protected readonly canManageBalances = computed(() => this.authService.hasAnyRole('HR', 'ADMIN'));
  protected readonly title = computed(() => this.isAdminMode() ? 'Soldes et transactions de congé' : 'Mon solde de congé');
  protected readonly filteredBalances = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();
    const type = this.leaveTypeFilter();
    return this.balances()
      .filter((balance) => !type || balance.leaveType.id === Number(type))
      .filter((balance) => !search || [
        balance.leaveType.name,
        balance.user.firstName,
        balance.user.lastName,
        String(balance.year)
      ].some((value) => value.toLowerCase().includes(search)));
  });

  ngOnInit(): void {
    if (this.canManageBalances()) {
      forkJoin({ users: this.userService.findAll(), leaveTypes: this.leaveTypeService.findAll() }).subscribe({
        next: ({ users, leaveTypes }) => { this.users.set(users); this.leaveTypes.set(leaveTypes); },
        error: () => this.error.set("Impossible de charger les options du formulaire de solde.")
      });
    }
    this.loadBalances();
    this.loadTransactions();
  }

  protected loadTransactions(): void {
    const source = this.isAdminMode() ? this.balanceService.findAllTransactions() : this.balanceService.findMyTransactions();
    source.subscribe({ next: (items) => this.transactions.set(items), error: () => this.error.set("Impossible de charger l'historique des transactions.") });
  }

  protected transactionLabel(type: string): string {
    return type === 'ACQUISITION_MENSUELLE'
      ? 'Crédit mensuel'
      : type === 'DEBIT_CONGE'
        ? 'Débit de congé approuvé'
        : 'Ajustement manuel';
  }

  protected transactionStatusLabel(status: string): string {
    const labels: Record<string, string> = { SUCCES: 'Réussie', SUCCESS: 'Réussie', ECHEC: 'Échec', ERROR: 'Échec' };
    return labels[status?.toUpperCase()] ?? status;
  }

  protected loadBalances(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;
    this.isLoading.set(true);
    const source = this.isAdminMode() ? this.balanceService.findAll() : this.balanceService.findByUser(currentUser.id);
    source.subscribe({
      next: (balances) => { this.balances.set(balances); this.isLoading.set(false); },
      error: () => { this.error.set('Impossible de charger les soldes de congé.'); this.isLoading.set(false); }
    });
  }

  protected progress(balance: LeaveBalanceResponse): number {
    return balance.totalDays ? Math.min(100, Math.round((balance.usedDays / balance.totalDays) * 100)) : 0;
  }

  protected saveBalance(request: LeaveBalanceRequest): void {
    if (!this.canManageBalances()) return;
    const current = this.editingBalance();
    const operation = current ? this.balanceService.update(current.id, request) : this.balanceService.create(request);
    this.isSaving.set(true);
    operation.subscribe({
      next: () => { this.isSaving.set(false); this.formOpen.set(false); this.success.set('Le solde de congé a été enregistré.'); this.loadBalances(); },
      error: (error) => { this.error.set(safeApiMessage(error, "Impossible d'enregistrer le solde de congé.")); this.isSaving.set(false); }
    });
  }

  protected deleteBalance(balance: LeaveBalanceResponse): void {
    if (!this.canManageBalances()) return;
    this.isSaving.set(true);
    this.balanceService.delete(balance.id).subscribe({
      next: () => { this.isSaving.set(false); this.success.set('Le solde de congé a été supprimé.'); this.loadBalances(); },
      error: (error) => { this.error.set(safeApiMessage(error, 'Impossible de supprimer le solde de congé.')); this.isSaving.set(false); }
    });
  }
}
