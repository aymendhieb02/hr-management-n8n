import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DepartmentFormComponent } from './department-form.component';

describe('DepartmentFormComponent', () => {
  let fixture: ComponentFixture<DepartmentFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentFormComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(DepartmentFormComponent);
    fixture.detectChanges();
  });

  it('requires a department name', () => {
    const button = fixture.nativeElement.querySelector('.primary-button') as HTMLButtonElement;

    expect(button.disabled).toBe(true);
  });

  it('trims values before save', () => {
    const saveSpy = vi.spyOn(fixture.componentInstance.save, 'emit');
    const inputs = fixture.nativeElement.querySelectorAll('input') as NodeListOf<HTMLInputElement>;
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;

    inputs[0].value = '  People  ';
    inputs[0].dispatchEvent(new Event('input'));
    textarea.value = '  HR operations  ';
    textarea.dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));

    expect(saveSpy).toHaveBeenCalledWith({ name: 'People', description: 'HR operations' });
  });
});
