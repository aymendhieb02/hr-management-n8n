import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { MedicalDocumentService } from '../../../medical-documents/services/medical-document.service';
import { LeaveRequestResponse } from '../../models/leave-request.model';
import { LeaveDetailsComponent } from './leave-details.component';
import { of } from 'rxjs';

describe('LeaveDetailsComponent medical certificate section', () => {
  let fixture: ComponentFixture<LeaveDetailsComponent>;
  const request: LeaveRequestResponse = {
    id: 1,
    requester: { id: 7, firstName: 'Eli', lastName: 'Employee', email: 'eli@test.com' },
    approver: null,
    leaveType: { id: 1, name: 'Sick Leave' },
    nature: 'CONGE',
    startDate: '2026-08-01',
    endDate: '2026-08-01',
    startTime: null,
    endTime: null,
    requestedDays: 1,
    reason: 'Congé de maladie',
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
        { provide: MedicalDocumentService, useValue: { upload: vi.fn(), findByLeaveRequest: vi.fn(() => of(null)), download: vi.fn() } }
      ]
    });
    fixture = TestBed.createComponent(LeaveDetailsComponent);
    fixture.componentRef.setInput('request', request);
    fixture.detectChanges();
  }

  it('shows upload only for requester sick leave context', () => {
    setup(7);
    expect(fixture.nativeElement.textContent).toContain('Certificat médical');
    expect(fixture.nativeElement.querySelector('app-medical-document-upload')).toBeTruthy();
  });

  it('hides upload for manager/non-owner context', () => {
    setup(8);
    expect(fixture.nativeElement.textContent).toContain("Aucun certificat médical n'a encore été ajouté");
    expect(fixture.nativeElement.querySelector('app-medical-document-upload')).toBeFalsy();
  });
});
