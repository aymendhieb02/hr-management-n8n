import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { Department } from '../../models/department.model';
import { DepartmentService } from '../../services/department.service';
import { DepartmentListComponent } from './department-list.component';

describe('DepartmentListComponent', () => {
  const departments: Department[] = [
    { id: 1, name: 'Human Resources', description: 'People operations', createdAt: '2026-01-01T00:00:00', updatedAt: null }
  ];
  let fixture: ComponentFixture<DepartmentListComponent>;
  let departmentService: {
    findAll: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let authService: { hasAnyRole: ReturnType<typeof vi.fn> };

  const text = () => fixture.nativeElement.textContent as string;
  const buttons = () => Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
  const buttonByText = (label: string) => buttons().find((button) => button.textContent?.trim() === label)!;
  const confirmDeleteButton = () => fixture.nativeElement.querySelector('.dialog .danger-button') as HTMLButtonElement;

  function configure(canManage = true, items: Department[] = departments): void {
    departmentService = {
      findAll: vi.fn(() => of(items)),
      create: vi.fn(() => of(departments[0])),
      update: vi.fn(() => of(departments[0])),
      delete: vi.fn(() => of(void 0))
    };
    authService = { hasAnyRole: vi.fn(() => canManage) };
    TestBed.configureTestingModule({
      imports: [DepartmentListComponent],
      providers: [
        { provide: DepartmentService, useValue: departmentService },
        { provide: AuthService, useValue: authService }
      ]
    });
    fixture = TestBed.createComponent(DepartmentListComponent);
    fixture.detectChanges();
  }

  it('loads and displays departments', () => {
    configure();

    expect(departmentService.findAll).toHaveBeenCalled();
    expect(text()).toContain('Human Resources');
    expect(text()).toContain('People operations');
  });

  it('shows loading and empty states', () => {
    const pending = new Subject<Department[]>();
    departmentService = {
      findAll: vi.fn(() => pending.asObservable()),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn()
    };
    authService = { hasAnyRole: vi.fn(() => true) };
    TestBed.configureTestingModule({
      imports: [DepartmentListComponent],
      providers: [
        { provide: DepartmentService, useValue: departmentService },
        { provide: AuthService, useValue: authService }
      ]
    });
    fixture = TestBed.createComponent(DepartmentListComponent);
    fixture.detectChanges();

    expect(text()).toContain('Loading departments...');

    pending.next([]);
    pending.complete();
    fixture.detectChanges();

    expect(text()).toContain('No departments found.');
  });

  it.each([
    ['HR', true],
    ['ADMIN', true],
    ['MANAGER', false],
    ['EMPLOYEE', false]
  ])('sets CRUD visibility for %s', (_role, canManage) => {
    configure(canManage);

    expect(text().includes('Add Department')).toBe(canManage);
    expect(text().includes('Edit')).toBe(canManage);
    expect(text().includes('Delete')).toBe(canManage);
  });

  it('creates a department from the form', () => {
    configure();

    buttonByText('Add Department').click();
    fixture.detectChanges();
    const name = fixture.nativeElement.querySelector('app-department-form input') as HTMLInputElement;
    name.value = '  Finance  ';
    name.dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('app-department-form form').dispatchEvent(new Event('submit'));

    expect(departmentService.create).toHaveBeenCalledWith({ name: 'Finance', description: null });
  });

  it('updates a department from the form', () => {
    configure();

    buttonByText('Edit').click();
    fixture.detectChanges();
    const name = fixture.nativeElement.querySelector('app-department-form input') as HTMLInputElement;
    name.value = 'People';
    name.dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('app-department-form form').dispatchEvent(new Event('submit'));

    expect(departmentService.update).toHaveBeenCalledWith(1, { name: 'People', description: 'People operations' });
  });

  it('deletes a department', () => {
    configure();

    buttonByText('Delete').click();
    fixture.detectChanges();
    confirmDeleteButton().click();

    expect(departmentService.delete).toHaveBeenCalledWith(1);
  });

  it('handles delete conflict safely', () => {
    configure();

    departmentService.delete.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    buttonByText('Delete').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    fixture.detectChanges();

    expect(text()).toContain('This item cannot be deleted or saved because it is referenced elsewhere.');
  });
});
