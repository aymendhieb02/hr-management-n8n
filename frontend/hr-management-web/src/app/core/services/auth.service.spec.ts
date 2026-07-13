import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, LoginResponse } from '../models/auth.model';
import { AuthService } from './auth.service';
import { TokenStorageService } from './token-storage.service';

describe('AuthService', () => {
  const user: AuthenticatedUser = {
    id: 1,
    username: 'admin',
    email: 'admin@xtensus.local',
    firstName: 'Admin',
    lastName: 'User',
    role: 'ADMIN'
  };
  const response: LoginResponse = {
    accessToken: 'jwt-token',
    tokenType: 'Bearer',
    expiresIn: 3600000,
    user
  };

  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenStorage: TokenStorageService;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('calls the login endpoint', () => {
    service.login({ usernameOrEmail: 'admin', password: 'secret' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ usernameOrEmail: 'admin', password: 'secret' });
    request.flush(response);
  });

  it('stores token and user after successful login', () => {
    service.login({ usernameOrEmail: 'admin', password: 'secret' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(response);

    expect(tokenStorage.getAccessToken()).toBe('jwt-token');
    expect(tokenStorage.getAuthenticatedUser()).toEqual(user);
    expect(service.getCurrentUser()).toEqual(user);
  });

  it('does not store token after failed login', () => {
    service.login({ usernameOrEmail: 'admin', password: 'bad' }).subscribe({
      error: () => undefined
    });
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(
      { message: 'Invalid username/email or password.' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(tokenStorage.getAuthenticatedUser()).toBeNull();
  });

  it('clears token and user on logout', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    tokenStorage.saveAccessToken('jwt-token');
    tokenStorage.saveAuthenticatedUser(user);

    service.logout();

    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(tokenStorage.getAuthenticatedUser()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith('/login');
  });

  it('never stores the password', () => {
    service.login({ usernameOrEmail: 'admin', password: 'top-secret-password' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(response);

    expect(JSON.stringify(sessionStorage)).not.toContain('top-secret-password');
  });
});
