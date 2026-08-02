import { TestBed } from '@angular/core/testing';
import { LeaveRequestResponse } from '../../leaves/models/leave-request.model';
import { CalendarService } from './calendar.service';

describe('CalendarService', () => {
  it('maps only approved leave requests to events', () => {
    const service = TestBed.inject(CalendarService);
    const events = service.toEvents([request('APPROVED'), request('REJECTED'), request('PENDING')]);
    expect(events.length).toBe(1);
    expect(events[0].employeeName).toBe('Eli Employee');
  });

  function request(status: LeaveRequestResponse['status']): LeaveRequestResponse {
    return { id: Math.random(), requester: { id: 1, firstName: 'Eli', lastName: 'Employee', email: 'e@test.com' }, approver: null, leaveType: { id: 1, name: 'Annual' }, startDate: '2026-07-10', endDate: '2026-07-12', requestedDays: 3, reason: null, status, submittedAt: '', decisionAt: null, decisionComment: null };
  }
});
