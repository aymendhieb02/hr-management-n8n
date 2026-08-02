import { Component, computed, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Department } from '../../../departments/models/department.model';
import { Position } from '../../../positions/models/position.model';
import { RoleType, UserCreateRequest, UserResponse, UserStatus, UserUpdateRequest } from '../../models/user.model';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class UserFormComponent implements OnChanges {
  readonly user = input<UserResponse | null>(null);
  readonly users = input<UserResponse[]>([]);
  readonly departments = input<Department[]>([]);
  readonly positions = input<Position[]>([]);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  @Output() readonly save = new EventEmitter<UserCreateRequest | UserUpdateRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly roles: RoleType[] = ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'];
  protected readonly statuses: UserStatus[] = ['ACTIVE', 'INACTIVE'];
  protected readonly form = new FormBuilder().nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(100)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    phone: ['', Validators.maxLength(30)],
    hireDate: [''],
    role: ['EMPLOYEE' as RoleType, Validators.required],
    status: ['ACTIVE' as UserStatus, Validators.required],
    enabled: true,
    managerId: new FormBuilder().control<number | null>(null),
    departmentId: new FormBuilder().control<number | null>(null),
    positionId: new FormBuilder().control<number | null>(null)
  });
  protected readonly title = computed(() => this.user() ? 'Edit User' : 'Add User');
  protected readonly eligibleManagers = computed(() => {
    const editedId = this.user()?.id;
    return this.users().filter((candidate) =>
      ['MANAGER', 'HR', 'ADMIN'].includes(candidate.role) && candidate.id !== editedId
    );
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user']) {
      const user = this.user();
      const password = this.form.controls.password;
      if (user) {
        password.clearValidators();
      } else {
        password.setValidators([Validators.required, Validators.minLength(8), Validators.maxLength(100)]);
      }
      password.updateValueAndValidity({ emitEvent: false });
      this.form.reset({
        username: user?.username ?? '',
        email: user?.email ?? '',
        password: '',
        firstName: user?.firstName ?? '',
        lastName: user?.lastName ?? '',
        phone: user?.phone ?? '',
        hireDate: user?.hireDate ?? '',
        role: user?.role ?? 'EMPLOYEE',
        status: user?.status ?? 'ACTIVE',
        enabled: user?.enabled ?? true,
        managerId: user?.manager?.id ?? null,
        departmentId: user?.department?.id ?? null,
        positionId: user?.position?.id ?? null
      });
    }
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) {
      return;
    }

    const value = this.form.getRawValue();
    const base = {
      username: value.username.trim(),
      email: value.email.trim().toLowerCase(),
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
      phone: value.phone.trim() || null,
      hireDate: value.hireDate || null,
      role: value.role,
      status: value.status,
      enabled: value.enabled,
      managerId: value.managerId || null,
      departmentId: value.departmentId || null,
      positionId: value.positionId || null
    };

    if (this.user()) {
      this.save.emit(base);
      return;
    }

    this.save.emit({ ...base, password: value.password.trim() });
  }

  protected fieldError(field: keyof typeof this.form.controls): string | null {
    const control = this.form.controls[field];
    const backendError = this.validationErrors()[field];
    if (backendError) {
      return backendError;
    }
    if (!(control.touched || control.dirty)) {
      return null;
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('email')) {
      return 'Enter a valid email address.';
    }
    if (control.hasError('minlength')) {
      return 'Use at least 8 characters.';
    }
    if (control.hasError('maxlength')) {
      return 'This value is too long.';
    }
    return null;
  }
}
