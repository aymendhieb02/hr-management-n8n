import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PositionFormComponent } from './position-form.component';

describe('PositionFormComponent', () => {
  let fixture: ComponentFixture<PositionFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PositionFormComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(PositionFormComponent);
    fixture.detectChanges();
  });

  it('requires a title', () => {
    const button = fixture.nativeElement.querySelector('.primary-button') as HTMLButtonElement;

    expect(button.disabled).toBe(true);
  });

  it('trims values before save', () => {
    const saveSpy = vi.spyOn(fixture.componentInstance.save, 'emit');
    const inputs = fixture.nativeElement.querySelectorAll('input') as NodeListOf<HTMLInputElement>;
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;

    inputs[0].value = '  Engineer  ';
    inputs[0].dispatchEvent(new Event('input'));
    textarea.value = '  Builds systems  ';
    textarea.dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));

    expect(saveSpy).toHaveBeenCalledWith({ title: 'Engineer', description: 'Builds systems' });
  });
});
