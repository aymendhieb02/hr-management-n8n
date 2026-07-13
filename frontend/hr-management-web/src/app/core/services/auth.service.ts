import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, LoginRequest, LoginResponse, UserRole } from '../models/auth.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly currentUserSignal = signal<AuthenticatedUser | null>(
    this.tokenStorage.getAuthenticatedUser()
  );

  readonly currentUser = this.currentUserSignal.asReadonly();

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => {
        this.tokenStorage.saveAccessToken(response.accessToken);
        this.tokenStorage.saveAuthenticatedUser(response.user);
        this.currentUserSignal.set(response.user);
      })
    );
  }

  loadCurrentUser(): Observable<AuthenticatedUser> {
    return this.http.get<AuthenticatedUser>(`${environment.apiUrl}/auth/me`).pipe(
      tap((user) => {
        this.tokenStorage.saveAuthenticatedUser(user);
        this.currentUserSignal.set(user);
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.clearAuthentication();
        }

        return throwError(() => error);
      })
    );
  }

  logout(): void {
    this.clearAuthentication();
    void this.router.navigateByUrl('/login');
  }

  getCurrentUser(): AuthenticatedUser | null {
    return this.currentUserSignal();
  }

  isAuthenticated(): boolean {
    return Boolean(this.tokenStorage.getAccessToken() && this.currentUserSignal());
  }

  hasRole(role: UserRole): boolean {
    return this.currentUserSignal()?.role === role;
  }

  hasAnyRole(...roles: UserRole[]): boolean {
    const currentRole = this.currentUserSignal()?.role;

    return Boolean(currentRole && roles.includes(currentRole));
  }

  clearAuthentication(): void {
    this.tokenStorage.clearAuthData();
    this.currentUserSignal.set(null);
  }
}
