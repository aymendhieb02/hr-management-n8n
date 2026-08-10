import { Component, EventEmitter, input, Output } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { PasswordUpdateRequest } from '../../models/user.model';

function passwordMatch(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return newPassword && confirmPassword && newPassword !== confirmPassword ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-password-update-dialog',
  imports: [ReactiveFormsModule],
  templateUrl: './password-update-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class PasswordUpdateDialogComponent {
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly save = new EventEmitter<PasswordUpdateRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: passwordMatch });

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) {
      return;
    }
    this.save.emit({ currentPassword: this.form.controls.currentPassword.value, newPassword: this.form.controls.newPassword.value });
    this.form.reset();
  }

  protected close(): void {
    this.form.reset();
    this.cancel.emit();
  }

  protected passwordError(): string | null {
    const control = this.form.controls.newPassword;
    if (!(control.touched || control.dirty)) {
      return null;
    }
    if (control.hasError('required')) {
      return 'Password is required.';
    }
    if (control.hasError('minlength')) {
      return 'Use at least 8 characters.';
    }
    if (control.hasError('maxlength')) {
      return 'Use 100 characters or fewer.';
    }
    return null;
  }

  protected confirmError(): string | null {
    const control = this.form.controls.confirmPassword;
    if (!(control.touched || control.dirty)) {
      return null;
    }
    if (control.hasError('required')) {
      return 'Confirm the password.';
    }
    if (this.form.hasError('passwordMismatch')) {
      return 'Passwords do not match.';
    }
    return null;
  }
}
