import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { roleGuard } from './role.guard';

describe('auth and role guards', () => {
  const authService = {
    isAuthenticated: vi.fn(),
    hasAnyRole: vi.fn()
  };
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService }
      ]
    });
    router = TestBed.inject(Router);
    vi.clearAllMocks();
  });

  it('blocks unauthenticated users', () => {
    authService.isAuthenticated.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });

  it('allows a valid role', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.hasAnyRole.mockReturnValue(true);
    const route = { data: { roles: ['HR', 'ADMIN'] } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => roleGuard(route, {} as RouterStateSnapshot));

    expect(result).toBe(true);
    expect(authService.hasAnyRole).toHaveBeenCalledWith('HR', 'ADMIN');
  });

  it('redirects an invalid role to forbidden', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.hasAnyRole.mockReturnValue(false);
    const route = { data: { roles: ['HR'] } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => roleGuard(route, {} as RouterStateSnapshot));

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree)).toBe('/forbidden');
  });
});
