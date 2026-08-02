import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../../core/services/auth.service';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { DeleteDepartmentDialogComponent } from '../../components/delete-department-dialog/delete-department-dialog.component';
import { DepartmentFormComponent } from '../../components/department-form/department-form.component';
import { Department, DepartmentRequest } from '../../models/department.model';
import { DepartmentService } from '../../services/department.service';

@Component({
  selector: 'app-department-list',
  imports: [DeleteDepartmentDialogComponent, DepartmentFormComponent, FormsModule],
  templateUrl: './department-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DepartmentListComponent implements OnInit {
  private readonly departmentService = inject(DepartmentService);
  private readonly authService = inject(AuthService);

  protected readonly departments = signal<Department[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly deleteError = signal<string | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly editingDepartment = signal<Department | null>(null);
  protected readonly deletingDepartment = signal<Department | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly canManage = computed(() => this.authService.hasAnyRole('HR', 'ADMIN'));
  protected readonly filteredDepartments = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();

    return search
      ? this.departments().filter((department) => department.name.toLowerCase().includes(search))
      : this.departments();
  });

  ngOnInit(): void {
    this.loadDepartments();
  }

  protected loadDepartments(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.departmentService.findAll().subscribe({
      next: (departments) => {
        this.departments.set(departments);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Departments could not be loaded.');
        this.isLoading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.formErrors.set({});
    this.editingDepartment.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(department: Department): void {
    this.formErrors.set({});
    this.editingDepartment.set(department);
    this.formOpen.set(true);
  }

  protected saveDepartment(request: DepartmentRequest): void {
    const current = this.editingDepartment();
    const operation = current
      ? this.departmentService.update(current.id, request)
      : this.departmentService.create(request);

    this.isSaving.set(true);
    this.formErrors.set({});
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.formOpen.set(false);
        this.loadDepartments();
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.error.set(safeApiMessage(error, 'Department could not be saved.'));
        this.isSaving.set(false);
      }
    });
  }

  protected requestDelete(department: Department): void {
    this.deleteError.set(null);
    this.deletingDepartment.set(department);
  }

  protected deleteDepartment(): void {
    const department = this.deletingDepartment();

    if (!department) {
      return;
    }

    this.isDeleting.set(true);
    this.deleteError.set(null);
    this.departmentService.delete(department.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.deletingDepartment.set(null);
        this.loadDepartments();
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, 'Department could not be deleted.'));
        this.isDeleting.set(false);
      }
    });
  }
}
