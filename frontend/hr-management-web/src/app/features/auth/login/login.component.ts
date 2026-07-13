import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { homeUrlForUser } from '../../../core/utils/role-home.util';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly form = this.formBuilder.nonNullable.group({
    usernameOrEmail: ['', Validators.required],
    password: ['', Validators.required]
  });

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      void this.router.navigateByUrl(homeUrlForUser(this.authService.getCurrentUser()));
    }
  }

  submit(): void {
    this.errorMessage.set(null);
    this.form.markAllAsTouched();

    if (this.form.invalid || this.isLoading()) {
      return;
    }

    this.isLoading.set(true);
    this.authService.login(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        void this.router.navigateByUrl(homeUrlForUser(response.user));
      },
      error: (error: { status?: number }) => {
        this.isLoading.set(false);
        this.errorMessage.set(this.safeErrorMessage(error.status));
      }
    });
  }

  protected hasFieldError(fieldName: 'usernameOrEmail' | 'password'): boolean {
    const control = this.form.controls[fieldName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected isSubmitDisabled(): boolean {
    return this.form.invalid || this.isLoading();
  }

  private safeErrorMessage(status: number | undefined): string {
    if (status === 403) {
      return 'This account cannot sign in. Please contact HR.';
    }

    return 'Invalid username/email or password.';
  }

}
