import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveRequestService } from '../../../leaves/services/leave-request.service';
import { UserService } from '../../../users/services/user.service';
import { CalendarService } from '../../services/calendar.service';
import { JourFerieService } from '../../../jours-feries/services/jour-ferie.service';
import { CalendarComponent } from './calendar.component';

describe('CalendarComponent', () => {
  let fixture: ComponentFixture<CalendarComponent>;
  let leaveRequests: any;

  function setup(path = 'my-calendar'): void {
    leaveRequests = {
      findAll: vi.fn(() => of([request()])),
      findByApprover: vi.fn(() => of([request()])),
      findByRequester: vi.fn(() => of([request()]))
    };

    TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { routeConfig: { path } } } },
        { provide: AuthService, useValue: { getCurrentUser: vi.fn(() => ({ id: 7, role: 'MANAGER' })) } },
        { provide: LeaveRequestService, useValue: leaveRequests },
        { provide: UserService, useValue: { findAll: vi.fn(() => of([{ id: 7, department: { name: 'HR' } }])) } },
        CalendarService
        ,{ provide: JourFerieService, useValue: { getActive: vi.fn(() => of([])) } }
      ]
    });

    fixture = TestBed.createComponent(CalendarComponent);
    fixture.detectChanges();
  }

  it('loads current user requests for my calendar', () => {
    setup('my-calendar');
    expect(leaveRequests.findByRequester).toHaveBeenCalledWith(7);
    expect(fixture.nativeElement.textContent).toContain('Mon calendrier');
  });

  it('loads approver requests for team calendar', () => {
    setup('team-calendar');
    expect(leaveRequests.findByApprover).toHaveBeenCalledWith(7);
    expect(fixture.nativeElement.textContent).toContain("Calendrier de l'équipe");
  });

  it('loads all requests for global calendar', () => {
    setup('calendar');
    expect(leaveRequests.findAll).toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Calendrier global');
  });

  it('supports month navigation', () => {
    setup();
    const component = fixture.componentInstance as any;
    const initial = component.month();
    component.nextMonth();
    expect(component.month().getMonth()).toBe((initial.getMonth() + 1) % 12);
    component.previousMonth();
    expect(component.month().getMonth()).toBe(initial.getMonth());
  });

  function request(): any {
    return {
      id: 1,
      requester: { id: 7, firstName: 'Ava', lastName: 'Manager', email: 'ava@example.com' },
      leaveType: { id: 1, name: 'Annual' },
      startDate: '2026-07-10',
      endDate: '2026-07-12',
      requestedDays: 3,
      reason: null,
      status: 'APPROVED',
      submittedAt: '2026-07-01T08:00:00',
      decisionAt: null,
      decisionComment: null
    };
  }
});
