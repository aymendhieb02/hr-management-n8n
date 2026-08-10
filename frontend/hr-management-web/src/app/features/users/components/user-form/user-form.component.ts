import { Component, computed, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RoleType, UserCreateRequest, UserReferenceSummary, UserResponse, UserStatus, UserUpdateRequest } from '../../models/user.model';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class UserFormComponent implements OnChanges {
  readonly user = input<UserResponse | null>(null);
  readonly users = input<UserResponse[]>([]);
  readonly typeContracts = input<UserReferenceSummary[]>([]);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  readonly managerMode = input(false);
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
    address: ['', Validators.maxLength(255)],
    birthDate: [''],
    sex: ['HOMME', Validators.required],
    hireDate: [''],
    role: ['EMPLOYEE' as RoleType, Validators.required],
    status: ['ACTIVE' as UserStatus, Validators.required],
    enabled: true,
    managerId: new FormBuilder().control<number | null>(null),
    typeContractId: new FormBuilder().control<number | null>(null)
  });
  protected readonly title = computed(() => this.user() ? "Modifier l'employé" : 'Ajouter un employé');
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
      password.clearValidators();
      password.updateValueAndValidity({ emitEvent: false });
      this.form.reset({
        username: user?.username ?? '',
        email: user?.email ?? '',
        password: '',
        firstName: user?.firstName ?? '',
        lastName: user?.lastName ?? '',
        phone: user?.phone ?? '',
        address: user?.address ?? '',
        birthDate: user?.birthDate ?? '',
        sex: user?.sex ?? 'HOMME',
        hireDate: user?.hireDate ?? '',
        role: user?.role ?? 'EMPLOYEE',
        status: user?.status ?? 'ACTIVE',
        enabled: user?.enabled ?? true,
        managerId: user?.manager?.id ?? null,
        typeContractId: user?.typeContract?.id ?? null
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
      address: value.address.trim() || null,
      birthDate: value.birthDate || null,
      sex: value.sex || null,
      hireDate: value.hireDate || null,
      role: this.managerMode() ? 'EMPLOYEE' : value.role,
      status: value.status,
      enabled: value.enabled,
      managerId: this.managerMode() ? null : value.managerId || null,
      departmentId: null,
      positionId: null,
      typeContractId: value.typeContractId || null
    };

    if (this.user()) {
      this.save.emit(base);
      return;
    }

    this.save.emit({ ...base, password: '' });
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
