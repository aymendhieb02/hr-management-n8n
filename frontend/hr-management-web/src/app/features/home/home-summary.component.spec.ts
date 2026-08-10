import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { LeaveBalanceService } from '../leaves/services/leave-balance.service';
import { LeaveRequestService } from '../leaves/services/leave-request.service';
import { NotificationService } from '../notifications/services/notification.service';
import { UserService } from '../users/services/user.service';
import { JourFerieService } from '../jours-feries/services/jour-ferie.service';
import { HomeSummaryComponent } from './home-summary.component';

describe('HomeSummaryComponent', () => {
  let fixture: ComponentFixture<HomeSummaryComponent>;
  let requests: any;
  let users: any;

  function setup(role = 'EMPLOYEE'): void {
    requests = { findByRequester: vi.fn(() => of([])), findByApprover: vi.fn(() => of([])) };
    users = { findTeamMembers: vi.fn(() => of([{ id: 2 }])) };
    TestBed.configureTestingModule({
      imports: [HomeSummaryComponent],
      providers: [
        { provide: AuthService, useValue: { getCurrentUser: vi.fn(() => ({ id: 7, role })) } },
        { provide: LeaveRequestService, useValue: requests },
        { provide: LeaveBalanceService, useValue: { findByUser: vi.fn(() => of([])) } },
        { provide: NotificationService, useValue: { findUnread: vi.fn(() => of([{}])) } },
        { provide: UserService, useValue: users }
        ,{ provide: JourFerieService, useValue: { getActive: vi.fn(() => of([])) } }
      ]
    });
    fixture = TestBed.createComponent(HomeSummaryComponent);
    fixture.detectChanges();
  }

  it('employee summary uses current user id', () => {
    setup('EMPLOYEE');
    expect(requests.findByRequester).toHaveBeenCalledWith(7);
    expect(fixture.nativeElement.textContent).toContain('Notifications non lues');
  });

  it('manager summary uses team endpoints', () => {
    setup('MANAGER');
    expect(requests.findByApprover).toHaveBeenCalledWith(7);
    expect(users.findTeamMembers).toHaveBeenCalledWith(7);
    expect(fixture.nativeElement.textContent).toContain("Membres de l'équipe");
  });
});
