import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { MedicalDocumentService } from '../../../medical-documents/services/medical-document.service';
import { LeaveRequestResponse } from '../../models/leave-request.model';
import { LeaveDetailsComponent } from './leave-details.component';

describe('LeaveDetailsComponent medical certificate section', () => {
  let fixture: ComponentFixture<LeaveDetailsComponent>;
  const request: LeaveRequestResponse = {
    id: 1,
    requester: { id: 7, firstName: 'Eli', lastName: 'Employee', email: 'eli@test.com' },
    approver: null,
    leaveType: { id: 1, name: 'Sick Leave' },
    startDate: '2026-08-01',
    endDate: '2026-08-01',
    requestedDays: 1,
    reason: null,
    status: 'PENDING',
    submittedAt: '',
    decisionAt: null,
    decisionComment: null
  };

  function setup(userId: number): void {
    TestBed.configureTestingModule({
      imports: [LeaveDetailsComponent],
      providers: [
        { provide: AuthService, useValue: { getCurrentUser: vi.fn(() => ({ id: userId, role: 'EMPLOYEE' })) } },
        { provide: MedicalDocumentService, useValue: { upload: vi.fn() } }
      ]
    });
    fixture = TestBed.createComponent(LeaveDetailsComponent);
    fixture.componentRef.setInput('request', request);
    fixture.detectChanges();
  }

  it('shows upload only for requester sick leave context', () => {
    setup(7);
    expect(fixture.nativeElement.textContent).toContain('Medical Certificate');
    expect(fixture.nativeElement.querySelector('app-medical-document-upload')).toBeTruthy();
  });

  it('hides upload for manager/non-owner context', () => {
    setup(8);
    expect(fixture.nativeElement.textContent).toContain('upload is available only to the requester');
    expect(fixture.nativeElement.querySelector('app-medical-document-upload')).toBeFalsy();
  });
});
