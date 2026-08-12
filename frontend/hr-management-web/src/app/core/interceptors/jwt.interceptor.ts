import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const isLoginRequest = request.url === `${environment.apiUrl}/auth/login`;
  const authenticatedRequest = request.url.startsWith(environment.apiUrl)
    ? request.clone({ withCredentials: true })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isLoginRequest) {
        authService.clearAuthentication();

        if (router.url !== '/login') {
          void router.navigateByUrl('/login');
        }
      }

      return throwError(() => error);
    })
  );
};
