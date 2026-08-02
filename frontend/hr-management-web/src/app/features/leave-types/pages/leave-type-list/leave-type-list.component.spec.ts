import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveType } from '../../models/leave-type.model';
import { LeaveTypeService } from '../../services/leave-type.service';
import { LeaveTypeListComponent } from './leave-type-list.component';

describe('LeaveTypeListComponent', () => {
  const leaveTypes: LeaveType[] = [
    {
      id: 3,
      name: 'Annual Leave',
      description: 'Paid annual leave',
      maxDays: null,
      requiresMedicalCertificate: false,
      active: true,
      createdAt: '2026-01-01T00:00:00',
      updatedAt: null
    },
    {
      id: 4,
      name: 'Legacy Leave',
      description: null,
      maxDays: 5,
      requiresMedicalCertificate: true,
      active: false,
      createdAt: '2026-01-01T00:00:00',
      updatedAt: null
    }
  ];
  let fixture: ComponentFixture<LeaveTypeListComponent>;
  let leaveTypeService: {
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

  function configure(canManage = true): void {
    leaveTypeService = {
      findAll: vi.fn(() => of(leaveTypes)),
      create: vi.fn(() => of(leaveTypes[0])),
      update: vi.fn(() => of(leaveTypes[0])),
      delete: vi.fn(() => of(void 0))
    };
    authService = { hasAnyRole: vi.fn(() => canManage) };
    TestBed.configureTestingModule({
      imports: [LeaveTypeListComponent],
      providers: [
        { provide: LeaveTypeService, useValue: leaveTypeService },
        { provide: AuthService, useValue: authService }
      ]
    });
    fixture = TestBed.createComponent(LeaveTypeListComponent);
    fixture.detectChanges();
  }

  it('loads leave types with badges and active filtering', () => {
    configure();

    expect(leaveTypeService.findAll).toHaveBeenCalled();
    expect(text()).toContain('Annual Leave');
    expect(text()).toContain('Active');
    expect(text()).toContain('Required');

    const activeOnly = fixture.nativeElement.querySelector('.checkbox-field input') as HTMLInputElement;
    activeOnly.checked = true;
    activeOnly.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(text()).toContain('Annual Leave');
    expect(text()).not.toContain('Legacy Leave');
  });

  it.each([
    ['HR', true],
    ['ADMIN', true],
    ['MANAGER', false],
    ['EMPLOYEE', false]
  ])('sets CRUD visibility for %s', (_role, canManage) => {
    configure(canManage);

    expect(text().includes('Add Leave Type')).toBe(canManage);
    expect(text().includes('Edit')).toBe(canManage);
    expect(text().includes('Delete')).toBe(canManage);
  });

  it('creates and updates a leave type from the form', () => {
    configure();

    buttonByText('Add Leave Type').click();
    fixture.detectChanges();
    let inputs = fixture.nativeElement.querySelectorAll('app-leave-type-form input') as NodeListOf<HTMLInputElement>;
    inputs[0].value = '  Sick Leave  ';
    inputs[0].dispatchEvent(new Event('input'));
    inputs[1].value = '10';
    inputs[1].dispatchEvent(new Event('input'));
    inputs[2].checked = true;
    inputs[2].dispatchEvent(new Event('change'));
    fixture.nativeElement.querySelector('app-leave-type-form form').dispatchEvent(new Event('submit'));
    expect(leaveTypeService.create).toHaveBeenCalledWith({
      name: 'Sick Leave',
      description: null,
      maxDays: 10,
      requiresMedicalCertificate: true,
      active: true
    });

    fixture.detectChanges();
    buttonByText('Edit').click();
    fixture.detectChanges();
    inputs = fixture.nativeElement.querySelectorAll('app-leave-type-form input') as NodeListOf<HTMLInputElement>;
    inputs[0].value = 'Annual Leave Updated';
    inputs[0].dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('app-leave-type-form form').dispatchEvent(new Event('submit'));
    expect(leaveTypeService.update).toHaveBeenCalledWith(3, {
      name: 'Annual Leave Updated',
      description: 'Paid annual leave',
      maxDays: null,
      requiresMedicalCertificate: false,
      active: true
    });
  });

  it('deletes a leave type', () => {
    configure();

    buttonByText('Delete').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    expect(leaveTypeService.delete).toHaveBeenCalledWith(3);
  });

  it('handles delete conflict safely', () => {
    configure();

    leaveTypeService.delete.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    buttonByText('Delete').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    fixture.detectChanges();

    expect(text()).toContain('This item cannot be deleted or saved because it is referenced elsewhere.');
  });
});
