import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LeaveDecisionDialogComponent } from './leave-decision-dialog.component';

describe('LeaveDecisionDialogComponent', () => {
  let fixture: ComponentFixture<LeaveDecisionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [LeaveDecisionDialogComponent] }).compileComponents();
    fixture = TestBed.createComponent(LeaveDecisionDialogComponent);
  });

  it('allows optional approve comment', () => {
    fixture.componentRef.setInput('mode', 'approve');
    fixture.detectChanges();
    const save = vi.spyOn(fixture.componentInstance.save, 'emit');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(save).toHaveBeenCalledWith(null);
  });

  it('requires reject comment', () => {
    fixture.componentRef.setInput('mode', 'reject');
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.primary-button') as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });
});
