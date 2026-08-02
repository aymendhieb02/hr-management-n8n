import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveTypeService } from '../../../leave-types/services/leave-type.service';
import { LeaveRequestResponse } from '../../models/leave-request.model';
import { LeaveRequestService } from '../../services/leave-request.service';
import { LeaveRequestListComponent } from './leave-request-list.component';

describe('LeaveRequestListComponent', () => {
  let fixture: ComponentFixture<LeaveRequestListComponent>;
  let service: any;
  const request = leaveRequest('PENDING');

  function setup(path = 'my-leave-requests'): void {
    service = {
      findByRequester: vi.fn(() => of([request])),
      findByApprover: vi.fn(() => of([request])),
      create: vi.fn(() => of(request)),
      update: vi.fn(() => of(request)),
      delete: vi.fn(() => of(void 0)),
      approve: vi.fn(() => of({ ...request, status: 'APPROVED' })),
      reject: vi.fn(() => of({ ...request, status: 'REJECTED' }))
    };
    TestBed.configureTestingModule({
      imports: [LeaveRequestListComponent],
      providers: [
        { provide: LeaveRequestService, useValue: service },
        { provide: LeaveTypeService, useValue: { findActive: vi.fn(() => of([{ id: 1, name: 'Annual', description: null, maxDays: null, requiresMedicalCertificate: false, active: true, createdAt: '', updatedAt: null }])) } },
        { provide: AuthService, useValue: { getCurrentUser: vi.fn(() => ({ id: 7, role: 'MANAGER' })) } },
        { provide: ActivatedRoute, useValue: { snapshot: { routeConfig: { path } } } }
      ]
    });
    fixture = TestBed.createComponent(LeaveRequestListComponent);
    fixture.detectChanges();
  }

  it('loads employee requests and creates, updates, deletes pending request', () => {
    setup();
    expect(service.findByRequester).toHaveBeenCalledWith(7);
    expect(text()).toContain('Annual');
    button('Create Leave Request').click();
    fixture.detectChanges();
    (fixture.componentInstance as any).saveRequest({ leaveTypeId: 1, startDate: '2026-08-01', endDate: '2026-08-02', reason: 'Rest' });
    expect(service.create).toHaveBeenCalledWith(expect.objectContaining({ requesterId: 7 }));
    button('Edit').click();
    fixture.detectChanges();
    (fixture.componentInstance as any).saveRequest({ leaveTypeId: 1, startDate: '2026-08-01', endDate: '2026-08-02', reason: 'Rest' });
    expect(service.update).toHaveBeenCalled();
    button('Delete').click();
    expect(service.delete).toHaveBeenCalledWith(1);
  });

  it('loads manager team requests and approves/rejects', () => {
    setup('team-requests');
    expect(service.findByApprover).toHaveBeenCalledWith(7);
    expect(text()).toContain('Team Requests');
    expect(text()).toContain('Approve');
    button('Approve').click();
    fixture.detectChanges();
    (fixture.componentInstance as any).saveDecision(null);
    expect(service.approve).toHaveBeenCalledWith(1, { approverId: 7, comment: null });
    button('Reject').click();
    fixture.detectChanges();
    (fixture.componentInstance as any).saveDecision('No coverage');
    expect(service.reject).toHaveBeenCalledWith(1, { approverId: 7, comment: 'No coverage' });
  });

  function text(): string { return fixture.nativeElement.textContent; }
  function button(label: string): HTMLButtonElement {
    return Array.from(fixture.nativeElement.querySelectorAll('button')).find((el: any) => el.textContent.trim() === label) as HTMLButtonElement;
  }

  function leaveRequest(status: LeaveRequestResponse['status']): LeaveRequestResponse {
    return {
      id: 1,
      requester: { id: 7, firstName: 'Eli', lastName: 'Employee', email: 'eli@test.com' },
      approver: { id: 8, firstName: 'Mona', lastName: 'Manager', email: 'mona@test.com' },
      leaveType: { id: 1, name: 'Annual' },
      startDate: '2026-08-01',
      endDate: '2026-08-02',
      requestedDays: 2,
      reason: 'Rest',
      status,
      submittedAt: '2026-07-01T00:00:00',
      decisionAt: null,
      decisionComment: null
    };
  }
});
