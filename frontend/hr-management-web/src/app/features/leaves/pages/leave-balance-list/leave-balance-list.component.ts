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
import { LeaveBalanceFormComponent } from '../../components/leave-balance-form/leave-balance-form.component';
import { LeaveBalanceRequest, LeaveBalanceResponse } from '../../models/leave-balance.model';
import { LeaveBalanceService } from '../../services/leave-balance.service';

@Component({
  selector: 'app-leave-balance-list',
  imports: [FormsModule, LeaveBalanceFormComponent],
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
  protected readonly isAdminMode = computed(() => this.route.snapshot.routeConfig?.path === 'leave-balances');
  protected readonly title = computed(() => this.isAdminMode() ? 'Leave Balances' : 'My Leave Balance');
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
    if (this.isAdminMode()) {
      forkJoin({ users: this.userService.findAll(), leaveTypes: this.leaveTypeService.findAll() }).subscribe({
        next: ({ users, leaveTypes }) => { this.users.set(users); this.leaveTypes.set(leaveTypes); },
        error: () => this.error.set('Balance form options could not be loaded.')
      });
    }
    this.loadBalances();
  }

  protected loadBalances(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;
    this.isLoading.set(true);
    const source = this.isAdminMode() ? this.balanceService.findAll() : this.balanceService.findByUser(currentUser.id);
    source.subscribe({
      next: (balances) => { this.balances.set(balances); this.isLoading.set(false); },
      error: () => { this.error.set('Leave balances could not be loaded.'); this.isLoading.set(false); }
    });
  }

  protected progress(balance: LeaveBalanceResponse): number {
    return balance.totalDays ? Math.min(100, Math.round((balance.usedDays / balance.totalDays) * 100)) : 0;
  }

  protected saveBalance(request: LeaveBalanceRequest): void {
    const current = this.editingBalance();
    const operation = current ? this.balanceService.update(current.id, request) : this.balanceService.create(request);
    this.isSaving.set(true);
    operation.subscribe({
      next: () => { this.isSaving.set(false); this.formOpen.set(false); this.success.set('Leave balance saved.'); this.loadBalances(); },
      error: (error) => { this.error.set(safeApiMessage(error, 'Leave balance could not be saved.')); this.isSaving.set(false); }
    });
  }

  protected deleteBalance(balance: LeaveBalanceResponse): void {
    this.isSaving.set(true);
    this.balanceService.delete(balance.id).subscribe({
      next: () => { this.isSaving.set(false); this.success.set('Leave balance deleted.'); this.loadBalances(); },
      error: (error) => { this.error.set(safeApiMessage(error, 'Leave balance could not be deleted.')); this.isSaving.set(false); }
    });
  }
}
