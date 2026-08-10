import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { DepartmentService } from '../../../departments/services/department.service';
import { PositionService } from '../../../positions/services/position.service';
import { UserResponse } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { TypeContractService } from '../../services/type-contract.service';
import { UserListComponent } from './user-list.component';

describe('UserListComponent', () => {
  const users: UserResponse[] = [
    user(1, 'Alice', 'Admin', 'ADMIN', 1),
    user(2, 'Mona', 'Manager', 'MANAGER', 1),
    user(3, 'Eli', 'Employee', 'EMPLOYEE', 2)
  ];
  let fixture: ComponentFixture<UserListComponent>;
  let userService: {
    findAll: ReturnType<typeof vi.fn>;
    findTeamMembers: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    updatePassword: ReturnType<typeof vi.fn>;
    setActive: ReturnType<typeof vi.fn>;
    resetPasswordToDefault: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let authService: {
    hasAnyRole: ReturnType<typeof vi.fn>;
    getCurrentUser: ReturnType<typeof vi.fn>;
  };

  const text = () => fixture.nativeElement.textContent as string;
  const buttons = () => Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
  const buttonByText = (label: string) => buttons().find((button) => button.textContent?.trim() === label)!;
  const confirmDeleteButton = () => fixture.nativeElement.querySelector('.dialog .danger-button') as HTMLButtonElement;

  function configure(role: UserResponse['role'] = 'HR', path = 'users', source = of(users)): void {
    userService = {
      findAll: vi.fn(() => source),
      findTeamMembers: vi.fn(() => of([users[2]])),
      create: vi.fn(() => of(users[0])),
      update: vi.fn(() => of(users[0])),
      updatePassword: vi.fn(() => of(void 0)),
      setActive: vi.fn(() => of(users[0])),
      resetPasswordToDefault: vi.fn(() => of(void 0)),
      delete: vi.fn(() => of(void 0))
    };
    authService = {
      hasAnyRole: vi.fn((...roles: string[]) => roles.includes(role)),
      getCurrentUser: vi.fn(() => ({ id: 2, role }))
    };
    TestBed.configureTestingModule({
      imports: [UserListComponent],
      providers: [
        { provide: UserService, useValue: userService },
        { provide: DepartmentService, useValue: { findAll: vi.fn(() => of([{ id: 1, name: 'Engineering', description: null, createdAt: '', updatedAt: null }, { id: 2, name: 'Support', description: null, createdAt: '', updatedAt: null }])) } },
        { provide: PositionService, useValue: { findAll: vi.fn(() => of([{ id: 1, title: 'Engineer', description: null, createdAt: '', updatedAt: null }])) } },
        { provide: TypeContractService, useValue: { findAll: vi.fn(() => of([{ id: 1, name: 'CDI' }, { id: 2, name: 'CDD' }])) } },
        { provide: AuthService, useValue: authService },
        { provide: ActivatedRoute, useValue: { snapshot: { routeConfig: { path } } } }
      ]
    });
    fixture = TestBed.createComponent(UserListComponent);
    fixture.detectChanges();
  }

  it('loads all users for HR and shows administration controls for Admin', () => {
    configure('ADMIN');

    expect(userService.findAll).toHaveBeenCalled();
    expect(text()).toContain('Alice Admin');
    expect(text()).toContain('Ajouter un employé');
    expect(text()).toContain('Edit');
    expect(text()).toContain('Bloquer');
    expect(text()).toContain('Delete');
  });

  it('loads only team members and lets managers administer their team', () => {
    configure('MANAGER', 'team-members');

    expect(userService.findTeamMembers).toHaveBeenCalledWith(2);
    expect(text()).toContain('Team Members');
    expect(text()).toContain('Eli Employee');
    expect(text()).toContain('Ajouter un employé');
    expect(text()).toContain('Edit');
    expect(text()).toContain('Débloquer');
  });

  it('searches, filters by role, and filters by contract type', () => {
    configure('HR');
    const search = fixture.nativeElement.querySelector('.search-input') as HTMLInputElement;
    search.value = 'eli';
    search.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(text()).toContain('Eli Employee');
    expect(text()).not.toContain('Alice Admin');

    (fixture.componentInstance as any).searchTerm.set('');
    (fixture.componentInstance as any).roleFilter.set('MANAGER');
    fixture.detectChanges();
    expect(text()).toContain('Mona Manager');
    expect(text()).not.toContain('Eli Employee');

    (fixture.componentInstance as any).roleFilter.set('');
    (fixture.componentInstance as any).typeContractFilter.set(2);
    fixture.detectChanges();
    expect(text()).toContain('Eli Employee');
    expect(text()).not.toContain('Alice Admin');
  });

  it('renders loading, error, and empty states', () => {
    const pending = new Subject<UserResponse[]>();
    configure('HR', 'users', pending.asObservable());
    expect(text()).toContain('Loading users...');

    pending.next([]);
    pending.complete();
    fixture.detectChanges();
    expect(text()).toContain('No users found.');

    TestBed.resetTestingModule();
    configure('HR', 'users', throwError(() => new Error('boom')));
    expect(text()).toContain('Users could not be loaded.');
  });

  it('creates, updates, blocks, deletes, and handles conflict safely', () => {
    configure('HR');

    buttonByText('Ajouter un employé').click();
    fixture.detectChanges();
    fillCreateForm();
    fixture.nativeElement.querySelector('app-user-form form').dispatchEvent(new Event('submit'));
    expect(userService.create).toHaveBeenCalledWith(expect.objectContaining({ username: 'new.user', email: 'new@test.com', password: '' }));

    fixture.detectChanges();
    buttonByText('Edit').click();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('app-user-form form').dispatchEvent(new Event('submit'));
    expect(userService.update).toHaveBeenCalledWith(1, expect.not.objectContaining({ password: expect.anything() }));

    fixture.detectChanges();
    buttonByText('Bloquer').click();
    expect(userService.setActive).toHaveBeenCalledWith(1, false);

    fixture.detectChanges();
    buttonByText('Delete').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    expect(userService.delete).toHaveBeenCalledWith(1);

    userService.delete.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    buttonByText('Delete').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    fixture.detectChanges();
    expect(text()).toContain('This item cannot be deleted or saved because it is referenced elsewhere.');
  });

  it('shows details without exposing passwordHash', () => {
    configure('HR', 'users', of([{ ...users[0], passwordHash: 'secret-hash' } as UserResponse]));

    buttonByText('View Details').click();
    fixture.detectChanges();

    expect(text()).toContain('Alice Admin');
    expect(text()).not.toContain('secret-hash');
    expect(text()).not.toContain('passwordHash');
  });

  function fillCreateForm(): void {
    const inputs = fixture.nativeElement.querySelectorAll('app-user-form input') as NodeListOf<HTMLInputElement>;
    inputs[0].value = 'new.user';
    inputs[0].dispatchEvent(new Event('input'));
    inputs[1].value = 'NEW@TEST.COM';
    inputs[1].dispatchEvent(new Event('input'));
    inputs[2].value = 'New';
    inputs[2].dispatchEvent(new Event('input'));
    inputs[3].value = 'User';
    inputs[3].dispatchEvent(new Event('input'));
  }

  function user(id: number, firstName: string, lastName: string, role: UserResponse['role'], departmentId: number): UserResponse {
    return {
      id,
      username: firstName.toLowerCase(),
      email: `${firstName.toLowerCase()}@test.com`,
      firstName,
      lastName,
      phone: null,
      hireDate: null,
      role,
      status: role === 'EMPLOYEE' ? 'INACTIVE' : 'ACTIVE',
      enabled: role !== 'EMPLOYEE',
      manager: id === 3 ? { id: 2, firstName: 'Mona', lastName: 'Manager', email: 'mona@test.com' } : null,
      department: null,
      position: { id: 1, name: 'Engineer' },
      typeContract: { id: departmentId, name: departmentId === 1 ? 'CDI' : 'CDD' },
      createdAt: '',
      updatedAt: null
    };
  }
});
