import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PasswordUpdateDialogComponent } from './password-update-dialog.component';

describe('PasswordUpdateDialogComponent', () => {
  let fixture: ComponentFixture<PasswordUpdateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PasswordUpdateDialogComponent] }).compileComponents();
    fixture = TestBed.createComponent(PasswordUpdateDialogComponent);
    fixture.detectChanges();
  });

  it('rejects password confirmation mismatch', () => {
    setValue(0, 'password1');
    setValue(1, 'password2');
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.primary-button') as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });

  it('emits password update and clears values on success', () => {
    const saveSpy = vi.spyOn(fixture.componentInstance.save, 'emit');
    setValue(0, 'password1');
    setValue(1, 'password1');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));

    expect(saveSpy).toHaveBeenCalledWith({ newPassword: 'password1' });
    expect((fixture.nativeElement.querySelectorAll('input')[0] as HTMLInputElement).value).toBe('');
  });

  function setValue(index: number, value: string): void {
    const input = fixture.nativeElement.querySelectorAll('input')[index] as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }
});
