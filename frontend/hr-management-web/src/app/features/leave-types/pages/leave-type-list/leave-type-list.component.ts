import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';
import { DeleteLeaveTypeDialogComponent } from '../../components/delete-leave-type-dialog/delete-leave-type-dialog.component';
import { LeaveTypeFormComponent } from '../../components/leave-type-form/leave-type-form.component';
import { LeaveType, LeaveTypeRequest } from '../../models/leave-type.model';
import { LeaveTypeService } from '../../services/leave-type.service';

@Component({
  selector: 'app-leave-type-list',
  imports: [DeleteLeaveTypeDialogComponent, FormsModule, LeaveTypeFormComponent, PaginatedTableDirective],
  templateUrl: './leave-type-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveTypeListComponent implements OnInit {
  private readonly leaveTypeService = inject(LeaveTypeService);
  private readonly authService = inject(AuthService);

  protected readonly leaveTypes = signal<LeaveType[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly deleteError = signal<string | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly activeOnly = signal(false);
  protected readonly editingLeaveType = signal<LeaveType | null>(null);
  protected readonly deletingLeaveType = signal<LeaveType | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly canManage = computed(() => this.authService.hasAnyRole('HR', 'ADMIN'));
  protected readonly filteredLeaveTypes = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();
    return this.leaveTypes()
      .filter((leaveType) => !this.activeOnly() || leaveType.active)
      .filter((leaveType) => !search || leaveType.name.toLowerCase().includes(search));
  });

  ngOnInit(): void {
    this.loadLeaveTypes();
  }

  protected loadLeaveTypes(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.leaveTypeService.findAll().subscribe({
      next: (leaveTypes) => {
        this.leaveTypes.set(leaveTypes);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Leave types could not be loaded.');
        this.isLoading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.formErrors.set({});
    this.editingLeaveType.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(leaveType: LeaveType): void {
    this.formErrors.set({});
    this.editingLeaveType.set(leaveType);
    this.formOpen.set(true);
  }

  protected saveLeaveType(request: LeaveTypeRequest): void {
    const current = this.editingLeaveType();
    const operation = current ? this.leaveTypeService.update(current.id, request) : this.leaveTypeService.create(request);

    this.isSaving.set(true);
    this.formErrors.set({});
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.formOpen.set(false);
        this.loadLeaveTypes();
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.error.set(safeApiMessage(error, 'Leave type could not be saved.'));
        this.isSaving.set(false);
      }
    });
  }

  protected requestDelete(leaveType: LeaveType): void {
    this.deleteError.set(null);
    this.deletingLeaveType.set(leaveType);
  }

  protected deleteLeaveType(): void {
    const leaveType = this.deletingLeaveType();

    if (!leaveType) {
      return;
    }

    this.isDeleting.set(true);
    this.deleteError.set(null);
    this.leaveTypeService.delete(leaveType.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.deletingLeaveType.set(null);
        this.loadLeaveTypes();
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, 'Leave type could not be deleted.'));
        this.isDeleting.set(false);
      }
    });
  }
}
