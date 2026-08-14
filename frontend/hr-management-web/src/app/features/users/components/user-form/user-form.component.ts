import { Component, computed, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RoleType, UserCreateRequest, UserReferenceSummary, UserResponse, UserStatus, UserUpdateRequest } from '../../models/user.model';
import { Position } from '../../../positions/models/position.model';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['../../../shared/resource-page.scss', './user-form.component.scss']
})
export class UserFormComponent implements OnChanges {
  private photoFile: File | null = null;
  readonly user = input<UserResponse | null>(null);
  readonly users = input<UserResponse[]>([]);
  readonly typeContracts = input<UserReferenceSummary[]>([]);
  readonly positions = input<Position[]>([]);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  readonly generalError = input<string | null>(null);
  readonly managerMode = input(false);
  readonly roleEditable = input(false);
  @Output() readonly save = new EventEmitter<UserCreateRequest | UserUpdateRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly roles: RoleType[] = ['EMPLOYEE', 'DG', 'DT', 'HR', 'ADMIN'];
  protected readonly statuses: UserStatus[] = ['ACTIVE', 'INACTIVE'];
  protected readonly roleLabels: Record<RoleType, string> = { EMPLOYEE: 'Employé', MANAGER: 'Directeur général', DG: 'Directeur général', DT: 'Directeur technique', HR: 'Ressources humaines', ADMIN: 'Administrateur' };
  protected readonly form = new FormBuilder().nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(100)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    phone: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    address: ['', Validators.maxLength(255)],
    birthDate: ['', [Validators.required, ageMinimum20]],
    sex: ['HOMME', Validators.required],
    hireDate: [''],
    role: ['EMPLOYEE' as RoleType, Validators.required],
    status: ['ACTIVE' as UserStatus, Validators.required],
    enabled: true,
    managerId: new FormBuilder().control<number | null>(null),
    positionId: new FormBuilder().control<number | null>(null, Validators.required),
    typeContractId: new FormBuilder().control<number | null>(null, Validators.required)
  });
  protected readonly title = computed(() => this.user() ? "Modifier l'employé" : 'Ajouter un employé');
  protected readonly eligibleManagers = computed(() => {
    const editedId = this.user()?.id;
    return this.users().filter((candidate) =>
      ['DG', 'DT', 'HR', 'ADMIN'].includes(candidate.role) && candidate.id !== editedId
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
        hireDate: user?.hireDate ?? this.today(),
        role: user?.role ?? 'EMPLOYEE',
        status: user?.status ?? 'ACTIVE',
        enabled: user?.enabled ?? true,
        managerId: user?.manager?.id ?? null,
        positionId: user?.position?.id ?? null,
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
      status: this.user()?.status ?? 'ACTIVE',
      enabled: this.user()?.enabled ?? true,
      managerId: this.managerMode() ? null : value.managerId || null,
      departmentId: null,
      positionId: value.positionId || null,
      typeContractId: value.typeContractId || null
    };

    if (this.user()) {
      this.save.emit(base);
      return;
    }

    this.save.emit({ ...base, password: '', photoFile: this.photoFile });
  }

  protected photoName = '';
  protected photoError: string | null = null;
  protected selectPhoto(event: Event): void {
    const input=event.target as HTMLInputElement; const file=input.files?.[0]??null;
    this.photoError=null;
    if(file && file.size>10*1024*1024){this.photoFile=null;this.photoName='';this.photoError='La photo ne doit pas dépasser 10 Mo.';input.value='';return;}
    this.photoFile=file;this.photoName=file?.name??'';
  }
  protected maxBirthDate():string{const d=new Date();d.setFullYear(d.getFullYear()-20);return this.localDate(d);}
  private localDate(d:Date):string{return new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,10);}

  private today(): string {
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60_000;
    return new Date(now.getTime() - offset).toISOString().slice(0, 10);
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
      return 'Ce champ est obligatoire.';
    }
    if (control.hasError('email')) {
      return 'Saisissez une adresse email valide.';
    }
    if (control.hasError('minlength')) {
      return 'Utilisez au moins 8 caractères.';
    }
    if (control.hasError('pattern') && field === 'phone') return 'Le téléphone doit contenir exactement 8 chiffres.';
    if (control.hasError('ageMinimum')) return "L'employé doit avoir au moins 20 ans.";
    if (control.hasError('maxlength')) {
      return 'Cette valeur est trop longue.';
    }
    return null;
  }
}

function ageMinimum20(control:AbstractControl):ValidationErrors|null{
  if(!control.value)return null; const naissance=new Date(`${control.value}T12:00:00`); const limite=new Date();limite.setFullYear(limite.getFullYear()-20);
  return naissance>limite?{ageMinimum:true}:null;
}
