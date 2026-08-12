import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ReportService } from '../../services/report.service';
import { ReportsComponent } from './reports.component';

describe('ReportsComponent', () => {
  let fixture: ComponentFixture<ReportsComponent>;

  function setup(fail = false): void {
    TestBed.configureTestingModule({
      imports: [ReportsComponent],
      providers: [
        {
          provide: ReportService,
          useValue: {
            loadData: vi.fn(() => fail ? throwError(() => new Error()) : of({
              leaveRequests: [leaveRequest('APPROVED'), leaveRequest('PENDING')],
              leaveBalances: [leaveBalance()],
              users: [user()],
              departments: [{ id: 1, name: 'HR' }]
            }))
          }
        }
      ]
    });

    fixture = TestBed.createComponent(ReportsComponent);
    fixture.detectChanges();
  }

  it('loads report data and renders leave requests', () => {
    setup();
    expect(fixture.nativeElement.textContent).toContain('Rapports');
    expect(fixture.nativeElement.textContent).toContain('Ava Manager');
    expect((fixture.componentInstance as any).rows().length).toBe(2);
  });

  it('filters by status', () => {
    setup();
    const component = fixture.componentInstance as any;
    component.filters.set({ ...component.filters(), status: 'APPROVED' });
    expect(component.rows().length).toBe(1);
    expect(component.rows()[0]['Statut']).toBe('Approuvée');
  });

  it('exports filtered rows as CSV', () => {
    setup();
    const anchor = document.createElement('a');
    const click = vi.spyOn(anchor, 'click').mockImplementation(() => undefined);
    const createElement = vi.spyOn(document, 'createElement').mockReturnValue(anchor);
    const createObjectUrl = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:reports');
    const revokeObjectUrl = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    (fixture.componentInstance as any).exportCsv();
    expect(anchor.download).toMatch(/^leave-requests-\d{4}-\d{2}-\d{2}\.csv$/);
    expect(click).toHaveBeenCalled();
    expect(createObjectUrl).toHaveBeenCalledWith(expect.any(Blob));
    createElement.mockRestore();
    createObjectUrl.mockRestore();
    revokeObjectUrl.mockRestore();
    click.mockRestore();
  });

  it('handles API failure safely', () => {
    setup(true);
    expect(fixture.nativeElement.textContent).toContain('Impossible de charger les données des rapports.');
  });

  function leaveRequest(status: 'APPROVED' | 'PENDING'): any {
    return {
      id: Math.random(),
      requester: { id: 7, firstName: 'Ava', lastName: 'Manager', email: 'ava@example.com' },
      leaveType: { id: 1, name: 'Annual' },
      startDate: '2026-07-10',
      endDate: '2026-07-12',
      requestedDays: 3,
      reason: null,
      status,
      submittedAt: '2026-07-01T08:00:00',
      decisionAt: null,
      decisionComment: null
    };
  }

  function leaveBalance(): any {
    return {
      id: 1,
      user: { id: 7, firstName: 'Ava', lastName: 'Manager', email: 'ava@example.com' },
      leaveType: { id: 1, name: 'Annual' },
      year: 2026,
      totalDays: 20,
      usedDays: 5,
      remainingDays: 15
    };
  }

  function user(): any {
    return {
      id: 7,
      firstName: 'Ava',
      lastName: 'Manager',
      email: 'ava@example.com',
      role: 'MANAGER',
      department: { id: 1, name: 'HR' },
      position: { id: 1, name: 'Lead' },
      enabled: true
    };
  }
});
