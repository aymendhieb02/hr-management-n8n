import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LeaveRequestFormComponent } from './leave-request-form.component';

describe('LeaveRequestFormComponent', () => {
  let fixture: ComponentFixture<LeaveRequestFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [LeaveRequestFormComponent] }).compileComponents();
    fixture = TestBed.createComponent(LeaveRequestFormComponent);
    fixture.componentRef.setInput('leaveTypes', [{ id: 1, name: 'Annual', description: null, maxDays: null, requiresMedicalCertificate: false, active: true, createdAt: '', updatedAt: null }]);
    fixture.detectChanges();
  });

  it('validates and emits trimmed request values', () => {
    const save = vi.spyOn(fixture.componentInstance.save, 'emit');
    (fixture.componentInstance as any).form.setValue({ leaveTypeId: 1, startDate: '2026-08-01', endDate: '2026-08-03', reason: '  Rest  ' });
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(save).toHaveBeenCalledWith({ leaveTypeId: 1, startDate: '2026-08-01', endDate: '2026-08-03', reason: 'Rest' });
  });
});
