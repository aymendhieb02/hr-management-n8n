import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LeaveTypeFormComponent } from './leave-type-form.component';

describe('LeaveTypeFormComponent', () => {
  let fixture: ComponentFixture<LeaveTypeFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LeaveTypeFormComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(LeaveTypeFormComponent);
    fixture.detectChanges();
  });

  it('requires a name and rejects zero max days', () => {
    const inputs = fixture.nativeElement.querySelectorAll('input') as NodeListOf<HTMLInputElement>;
    inputs[1].value = '0';
    inputs[1].dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.primary-button') as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });

  it('emits nullable maxDays and checkbox values', () => {
    const saveSpy = vi.spyOn(fixture.componentInstance.save, 'emit');
    const inputs = fixture.nativeElement.querySelectorAll('input') as NodeListOf<HTMLInputElement>;

    inputs[0].value = '  Annual  ';
    inputs[0].dispatchEvent(new Event('input'));
    inputs[1].value = '';
    inputs[1].dispatchEvent(new Event('input'));
    inputs[2].checked = true;
    inputs[2].dispatchEvent(new Event('change'));
    inputs[3].checked = true;
    inputs[3].dispatchEvent(new Event('change'));
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));

    expect(saveSpy).toHaveBeenCalledWith({
      name: 'Annual',
      description: null,
      maxDays: null,
      requiresMedicalCertificate: true,
      active: true
    });
  });
});
