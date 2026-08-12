import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveTypeService } from '../../../leave-types/services/leave-type.service';
import { UserService } from '../../../users/services/user.service';
import { LeaveBalanceResponse } from '../../models/leave-balance.model';
import { LeaveBalanceService } from '../../services/leave-balance.service';
import { LeaveBalanceListComponent } from './leave-balance-list.component';

describe('LeaveBalanceListComponent', () => {
  let fixture: ComponentFixture<LeaveBalanceListComponent>;
  let service: any;
  const balance: LeaveBalanceResponse = {
    id: 1,
    user: { id: 7, firstName: 'Eli', lastName: 'Employee', email: 'eli@test.com' },
    leaveType: { id: 1, name: 'Annual' },
    year: 2026,
    totalDays: 20,
    usedDays: 5,
    remainingDays: 15,
    createdAt: '',
    updatedAt: null
  };

  function setup(path = 'my-balance'): void {
    const role = path === 'my-balance' ? 'EMPLOYEE' : 'HR';
    service = {
      findByUser: vi.fn(() => of([balance])),
      findAll: vi.fn(() => of([balance])),
      findMyTransactions: vi.fn(() => of([])),
      findAllTransactions: vi.fn(() => of([])),
      create: vi.fn(() => of(balance)),
      update: vi.fn(() => of(balance)),
      delete: vi.fn(() => of(void 0))
    };
    TestBed.configureTestingModule({
      imports: [LeaveBalanceListComponent],
      providers: [
        { provide: LeaveBalanceService, useValue: service },
        { provide: LeaveTypeService, useValue: { findAll: vi.fn(() => of([{ id: 1, name: 'Annual', description: null, maxDays: null, requiresMedicalCertificate: false, active: true, createdAt: '', updatedAt: null }])) } },
        { provide: UserService, useValue: { findAll: vi.fn(() => of([])) } },
        { provide: AuthService, useValue: {
          getCurrentUser: vi.fn(() => ({ id: 7, role })),
          hasAnyRole: vi.fn((...roles: string[]) => roles.includes(role))
        } },
        { provide: ActivatedRoute, useValue: { snapshot: { routeConfig: { path } } } }
      ]
    });
    fixture = TestBed.createComponent(LeaveBalanceListComponent);
    fixture.detectChanges();
  }

  it('loads own balance read-only with progress', () => {
    setup();
    expect(service.findByUser).toHaveBeenCalledWith(7);
    expect(text()).toContain('Annual');
    expect(text()).toContain('25 %');
    expect(text()).not.toContain('Ajouter un solde');
  });

  it('allows HR/Admin balance administration CRUD', () => {
    setup('leave-balances');
    expect(service.findAll).toHaveBeenCalled();
    expect(text()).toContain('Ajouter un solde');
    (fixture.componentInstance as any).saveBalance({ userId: 7, leaveTypeId: 1, year: 2026, totalDays: 20 });
    expect(service.create).toHaveBeenCalled();
    (fixture.componentInstance as any).editingBalance.set(balance);
    (fixture.componentInstance as any).saveBalance({ userId: 7, leaveTypeId: 1, year: 2026, totalDays: 20 });
    expect(service.update).toHaveBeenCalledWith(1, expect.anything());
    (fixture.componentInstance as any).deleteBalance(balance);
    expect(service.delete).toHaveBeenCalledWith(1);
  });

  function text(): string { return fixture.nativeElement.textContent; }
});
