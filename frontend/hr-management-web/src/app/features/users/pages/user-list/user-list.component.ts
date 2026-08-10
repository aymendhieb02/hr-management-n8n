import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';
import { DeleteUserDialogComponent } from '../../components/delete-user-dialog/delete-user-dialog.component';
import { PasswordUpdateDialogComponent } from '../../components/password-update-dialog/password-update-dialog.component';
import { UserDetailsComponent } from '../../components/user-details/user-details.component';
import { UserFormComponent } from '../../components/user-form/user-form.component';
import { PasswordUpdateRequest, RoleType, UserCreateRequest, UserReferenceSummary, UserResponse, UserUpdateRequest } from '../../models/user.model';
import { TypeContractService } from '../../services/type-contract.service';
import { UserService } from '../../services/user.service';
import { Position } from '../../../positions/models/position.model';
import { PositionService } from '../../../positions/services/position.service';

@Component({
  selector: 'app-user-list',
  imports: [DeleteUserDialogComponent, FormsModule, PasswordUpdateDialogComponent, UserDetailsComponent, UserFormComponent, PaginatedTableDirective],
  templateUrl: './user-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class UserListComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly typeContractService = inject(TypeContractService);
  private readonly positionService = inject(PositionService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly users = signal<UserResponse[]>([]);
  protected readonly typeContracts = signal<UserReferenceSummary[]>([]);
  protected readonly positions = signal<Position[]>([]);
  protected readonly allUsersForManagers = signal<UserResponse[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly passwordError = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly roleFilter = signal<RoleType | ''>('');
  protected readonly typeContractFilter = signal<number | ''>('');
  protected readonly formOpen = signal(false);
  protected readonly editingUser = signal<UserResponse | null>(null);
  protected readonly passwordUser = signal<UserResponse | null>(null);
  protected readonly deletingUser = signal<UserResponse | null>(null);
  protected readonly detailUser = signal<UserResponse | null>(null);
  protected readonly resetPasswordUser = signal<UserResponse | null>(null);
  protected readonly roles: RoleType[] = ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'];
  protected readonly canManage = computed(() => this.authService.hasAnyRole('HR', 'ADMIN') || (this.authService.hasAnyRole('MANAGER') && this.isTeamMode()));
  protected readonly isTeamMode = computed(() => this.route.snapshot.routeConfig?.path === 'team-members');
  protected readonly title = computed(() => this.isTeamMode() ? "Membres de l'équipe" : 'Employés');
  protected readonly filteredUsers = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();
    const role = this.roleFilter();
    const typeContractId = this.typeContractFilter();

    return this.users()
      .filter((user) => !search || [
        user.firstName,
        user.lastName,
        `${user.firstName} ${user.lastName}`,
        user.username,
        user.email
      ].some((value) => value.toLowerCase().includes(search)))
      .filter((user) => !role || user.role === role)
      .filter((user) => !typeContractId || user.typeContract?.id === Number(typeContractId));
  });

  ngOnInit(): void {
    this.loadReferenceData();
    this.loadUsers();
  }

  protected loadUsers(): void {
    this.isLoading.set(true);
    this.error.set(null);
    const currentUser = this.authService.getCurrentUser();
    const source = this.isTeamMode() && currentUser
      ? this.userService.findTeamMembers(currentUser.id)
      : this.userService.findAll();

    source.subscribe({
      next: (users) => {
        this.users.set(users);
        if (!this.allUsersForManagers().length) {
          this.allUsersForManagers.set(users);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set(this.isTeamMode() ? "Impossible de charger les membres de l'équipe." : 'Impossible de charger les employés.');
        this.isLoading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.formErrors.set({});
    this.editingUser.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(user: UserResponse): void {
    this.formErrors.set({});
    this.editingUser.set(user);
    this.formOpen.set(true);
  }

  protected saveUser(request: UserCreateRequest | UserUpdateRequest): void {
    const current = this.editingUser();
    const operation = current
      ? this.userService.update(current.id, request as UserUpdateRequest)
      : this.userService.create(request as UserCreateRequest);

    this.isSaving.set(true);
    this.formErrors.set({});
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.formOpen.set(false);
        this.loadUsers();
        this.loadReferenceData();
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.error.set(safeApiMessage(error, "Impossible d'enregistrer l'employé."));
        this.isSaving.set(false);
      }
    });
  }

  protected openPassword(user: UserResponse): void {
    this.passwordError.set(null);
    this.passwordUser.set(user);
  }

  protected updatePassword(request: PasswordUpdateRequest): void {
    const user = this.passwordUser();
    if (!user) {
      return;
    }
    this.isSaving.set(true);
    this.passwordError.set(null);
    this.userService.updatePassword(user.id, request).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.passwordUser.set(null);
      },
      error: (error) => {
        this.passwordError.set(safeApiMessage(error, 'Impossible de modifier le mot de passe.'));
        this.isSaving.set(false);
      }
    });
  }

  protected requestDelete(user: UserResponse): void {
    this.deleteError.set(null);
    this.deletingUser.set(user);
  }

  protected toggleActive(user: UserResponse): void {
    this.userService.setActive(user.id, !user.enabled).subscribe({
      next: () => this.loadUsers(),
      error: (error) => this.error.set(safeApiMessage(error, "Le statut de l'employé n'a pas pu être modifié."))
    });
  }

  protected askResetPassword(user: UserResponse): void {
    this.resetPasswordUser.set(user);
  }

  protected resetPasswordToDefault(): void {
    const user = this.resetPasswordUser();
    if (!user) return;
    this.userService.resetPasswordToDefault(user.id).subscribe({
      next: () => { this.error.set(null); this.resetPasswordUser.set(null); },
      error: (error) => this.error.set(safeApiMessage(error, "Le mot de passe n'a pas pu être réinitialisé."))
    });
  }

  protected deleteUser(): void {
    const user = this.deletingUser();
    if (!user) {
      return;
    }
    this.isDeleting.set(true);
    this.deleteError.set(null);
    this.userService.delete(user.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.deletingUser.set(null);
        this.loadUsers();
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, "Impossible de supprimer l'employé."));
        this.isDeleting.set(false);
      }
    });
  }

  private loadReferenceData(): void {
    if (!this.authService.hasAnyRole('MANAGER', 'HR', 'ADMIN')) {
      return;
    }

    forkJoin({
      typeContracts: this.typeContractService.findAll(),
      positions: this.positionService.findAll(),
      managers: this.authService.hasAnyRole('HR', 'ADMIN') ? this.userService.findAll() : of([])
    }).subscribe({
      next: ({ typeContracts, positions, managers }) => {
        this.typeContracts.set(typeContracts);
        this.positions.set(positions.filter((position) => position.active !== false));
        this.allUsersForManagers.set(managers);
      },
      error: () => {
        this.error.set("Impossible de charger les options du formulaire employé.");
      }
    });
  }

  protected roleLabel(role: RoleType): string {
    return { EMPLOYEE: 'Employé', MANAGER: 'Manager', HR: 'Ressources humaines', ADMIN: 'Administrateur' }[role];
  }
}
