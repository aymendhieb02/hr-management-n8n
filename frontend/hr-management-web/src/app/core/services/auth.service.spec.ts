import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, LoginResponse } from '../models/auth.model';
import { AuthService } from './auth.service';

describe('AuthService cookie authentication', () => {
  const user: AuthenticatedUser = { id: 1, username: 'admin', email: 'admin@xtensus.local', firstName: 'Admin', lastName: 'User', role: 'ADMIN' };
  const response: LoginResponse = { expiresIn: 3600000, user };
  let service: AuthService; let httpMock: HttpTestingController; let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])] });
    service = TestBed.inject(AuthService); httpMock = TestBed.inject(HttpTestingController); router = TestBed.inject(Router);
  });
  afterEach(() => httpMock.verify());

  it('authenticates without storing a JWT in browser storage', () => {
    service.login({ usernameOrEmail: 'admin', password: 'secret' }).subscribe();
    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.method).toBe('POST'); request.flush(response);
    expect(service.getCurrentUser()).toEqual(user);
    expect(sessionStorage.length).toBe(0); expect(localStorage.length).toBe(0);
  });

  it('restores the user from the HttpOnly cookie through auth/me', () => {
    service.loadCurrentUser().subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/me`).flush(user);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('logs out on the server and clears in-memory identity', () => {
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    service.login({ usernameOrEmail: 'admin', password: 'secret' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(response);
    service.logout();
    const logout = httpMock.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(logout.request.method).toBe('POST'); logout.flush(null);
    expect(service.getCurrentUser()).toBeNull(); expect(navigate).toHaveBeenCalledWith('/login');
  });
});
