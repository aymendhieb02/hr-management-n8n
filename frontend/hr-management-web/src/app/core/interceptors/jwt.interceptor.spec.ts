import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { jwtInterceptor } from './jwt.interceptor';
import { TokenStorageService } from '../services/token-storage.service';

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let tokenStorage: TokenStorageService;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('adds a Bearer token to protected requests', () => {
    tokenStorage.saveAccessToken('jwt-token');

    http.get(`${environment.apiUrl}/auth/me`).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/me`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush({});
  });

  it('does not add a token to login requests', () => {
    tokenStorage.saveAccessToken('jwt-token');

    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('clears auth data and redirects on protected 401 responses', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    tokenStorage.saveAccessToken('jwt-token');

    http.get(`${environment.apiUrl}/auth/me`).subscribe({ error: () => undefined });
    httpMock.expectOne(`${environment.apiUrl}/auth/me`).flush(
      { message: 'Unauthorized' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith('/login');
  });
});
