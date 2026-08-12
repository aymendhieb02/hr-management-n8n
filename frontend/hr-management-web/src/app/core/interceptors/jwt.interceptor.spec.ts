import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { jwtInterceptor } from './jwt.interceptor';

describe('cookie authentication interceptor', () => {
  let http: HttpClient; let httpMock: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([jwtInterceptor])), provideHttpClientTesting(), provideRouter([])] });
    http = TestBed.inject(HttpClient); httpMock = TestBed.inject(HttpTestingController);
  });
  afterEach(() => httpMock.verify());

  it('sends API requests with credentials and without an Authorization header', () => {
    http.get(`${environment.apiUrl}/auth/me`).subscribe();
    const request = httpMock.expectOne(`${environment.apiUrl}/auth/me`);
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });
});
