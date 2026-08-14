import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthenticatedUser } from '../core/models/auth.model';
import { AuthService } from '../core/services/auth.service';
import { HomeRedirectComponent } from './home-redirect.component';

describe('HomeRedirectComponent', () => {
  const user: AuthenticatedUser = {
    id: 1,
    username: 'employee',
    email: 'employee@xtensus.local',
    firstName: 'Employee',
    lastName: 'User',
    role: 'EMPLOYEE'
  };
  const authService = {
    getCurrentUser: vi.fn()
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [HomeRedirectComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();
  });

  it('redirects employees and managers to home from root', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    authService.getCurrentUser.mockReturnValue({ ...user, role: 'DG' });

    TestBed.createComponent(HomeRedirectComponent).detectChanges();

    expect(navigateSpy).toHaveBeenCalledWith('/home');
  });

  it('redirects HR and Admin users to dashboard from root', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    authService.getCurrentUser.mockReturnValue({ ...user, role: 'ADMIN' });

    TestBed.createComponent(HomeRedirectComponent).detectChanges();

    expect(navigateSpy).toHaveBeenCalledWith('/dashboard');
  });
});
