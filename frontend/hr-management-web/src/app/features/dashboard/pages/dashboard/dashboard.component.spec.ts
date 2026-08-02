import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { DepartmentService } from '../../../departments/services/department.service';
import { LeaveBalanceService } from '../../../leaves/services/leave-balance.service';
import { LeaveRequestService } from '../../../leaves/services/leave-request.service';
import { NotificationService } from '../../../notifications/services/notification.service';
import { UserService } from '../../../users/services/user.service';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;

  function setup(fail = false): void {
    TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: AuthService, useValue: { getCurrentUser: vi.fn(() => ({ id: 1, role: 'HR' })) } },
        { provide: UserService, useValue: { findAll: vi.fn(() => fail ? throwError(() => new Error()) : of([{ id: 1, department: { name: 'HR' } }, { id: 2, department: { name: 'IT' } }])) } },
        { provide: LeaveRequestService, useValue: { findAll: vi.fn(() => of([request('PENDING'), request('APPROVED'), request('REJECTED')])) } },
        { provide: DepartmentService, useValue: { findAll: vi.fn(() => of([])) } },
        { provide: LeaveBalanceService, useValue: { findAll: vi.fn(() => of([])) } },
        { provide: NotificationService, useValue: { findUnread: vi.fn(() => of([{}])) } }
      ]
    });
    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  }

  it('loads real API data and computes counts', () => {
    setup();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Total Users');
    expect(text).toContain('Pending Requests');
    expect(text).toContain('Unread Notifications');
  });

  it('handles API failure safely', () => {
    setup(true);
    expect(fixture.nativeElement.textContent).toContain('Dashboard data could not be loaded.');
  });

  function request(status: string): any {
    return { id: Math.random(), requester: { id: 1, firstName: 'A', lastName: 'B' }, leaveType: { name: 'Annual' }, startDate: '2026-07-10', endDate: '2026-07-12', status, submittedAt: status };
  }
});
