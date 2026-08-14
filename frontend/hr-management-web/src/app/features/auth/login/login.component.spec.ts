import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { provideRouter, Router } from '@angular/router';
import { AuthenticatedUser, LoginResponse } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  const employee: AuthenticatedUser = {
    id: 1,
    username: 'employee',
    email: 'employee@xtensus.local',
    firstName: 'Employee',
    lastName: 'User',
    role: 'EMPLOYEE'
  };
  const manager: AuthenticatedUser = { ...employee, role: 'MANAGER', username: 'manager' };
  const hr: AuthenticatedUser = { ...employee, role: 'HR', username: 'hr' };
  const response = (user: AuthenticatedUser): LoginResponse => ({
    expiresIn: 3600000,
    user
  });
  const authService = {
    isAuthenticated: vi.fn(),
    getCurrentUser: vi.fn(),
    login: vi.fn(),
    requestPasswordReset: vi.fn(),
    verifyResetCode: vi.fn(),
    resetPassword: vi.fn()
  };

  let fixture: ComponentFixture<LoginComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.clearAllMocks();
    authService.isAuthenticated.mockReturnValue(false);
    authService.getCurrentUser.mockReturnValue(null);
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  });

  it('requires username/email and password', () => {
    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;

    expect(button.disabled).toBe(true);
  });

  it('redirects HR and Admin users to dashboard after login', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    authService.login.mockReturnValue(of(response(hr)));

    fillAndSubmit('hr', 'secret');

    expect(navigateSpy).toHaveBeenCalledWith('/dashboard');
  });

  it('redirects managers home after login', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    authService.login.mockReturnValue(of(response(manager)));

    fillAndSubmit('manager', 'secret');

    expect(navigateSpy).toHaveBeenCalledWith('/home');
  });

  it('redirects employees home after login', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    authService.login.mockReturnValue(of(response(employee)));

    fillAndSubmit('employee', 'secret');

    expect(navigateSpy).toHaveBeenCalledWith('/home');
  });

  it('redirects a new employee to the profile password dialog', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    authService.login.mockReturnValue(of(response({ ...employee, passwordChangeRequired: true })));

    fillAndSubmit('employee', 'temporary-code');

    expect(navigateSpy).toHaveBeenCalledWith('/profile');
  });

  it('shows a safe generic error after failed login', () => {
    authService.login.mockReturnValue(throwError(() => ({ status: 401 })));

    fillAndSubmit('employee', 'wrong');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Identifiant, adresse email ou mot de passe incorrect.');
  });

  it('explains that a still-valid reset code has already been sent', () => {
    authService.requestPasswordReset.mockReturnValue(throwError(() => ({
      status: 409,
      error: { message: 'Un code de vérification a déjà été envoyé et reste valide.' }
    })));

    openForgotPasswordAndRequestCode('employee');

    expect(fixture.nativeElement.textContent).toContain('Un code de vérification a déjà été envoyé et reste valide.');
  });

  it('marks an unused reset code as expired once its validity has elapsed', () => {
    authService.requestPasswordReset.mockReturnValue(of({
      expireLe: new Date(Date.now() - 1_000).toISOString(),
      message: 'Code envoyé.'
    }));

    openForgotPasswordAndRequestCode('employee');

    expect(fixture.nativeElement.textContent).toContain('Ce code a expiré. Demandez un nouveau code.');
    expect(fixture.nativeElement.textContent).toContain('Recevoir un nouveau code');
  });

  function fillAndSubmit(usernameOrEmail: string, password: string): void {
    const inputs = fixture.nativeElement.querySelectorAll('input') as NodeListOf<HTMLInputElement>;
    inputs[0].value = usernameOrEmail;
    inputs[0].dispatchEvent(new Event('input'));
    inputs[1].value = password;
    inputs[1].dispatchEvent(new Event('input'));

    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  function openForgotPasswordAndRequestCode(identifier: string): void {
    const forgotButton = Array.from(fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>)
      .find(button => button.textContent?.includes('Mot de passe oublié'))!;
    forgotButton.click();
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = identifier;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    const sendButton = Array.from(fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>)
      .find(button => button.textContent?.includes('Envoyer le code'))!;
    sendButton.click();
    fixture.detectChanges();
  }
});
