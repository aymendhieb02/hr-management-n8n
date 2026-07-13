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
    accessToken: 'jwt-token',
    tokenType: 'Bearer',
    expiresIn: 3600000,
    user
  });
  const authService = {
    isAuthenticated: vi.fn(),
    getCurrentUser: vi.fn(),
    login: vi.fn()
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

  it('shows a safe generic error after failed login', () => {
    authService.login.mockReturnValue(throwError(() => ({ status: 401 })));

    fillAndSubmit('employee', 'wrong');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Invalid username/email or password.');
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
});
