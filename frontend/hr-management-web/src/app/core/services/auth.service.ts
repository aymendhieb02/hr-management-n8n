import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, LoginRequest, LoginResponse, UserRole } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly currentUserSignal = signal<AuthenticatedUser | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => {
        this.currentUserSignal.set(response.user);
      })
    );
  }

  requestPasswordReset(identifier: string): Observable<{expireLe:string;message:string}> {
    return this.http.post<{expireLe:string;message:string}>(`${environment.apiUrl}/auth/mot-de-passe-oublie`, { identifiant: identifier });
  }

  resetPassword(identifier: string, code: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/reinitialiser-mot-de-passe`, {
      identifiant: identifier, code, nouveauMotDePasse: newPassword
    });
  }

  verifyResetCode(identifier: string, code: string): Observable<{statut:'VALIDE'|'INVALIDE'|'EXPIRE'|'BLOQUE';message:string;tentatives:number}> {
    return this.http.post<{statut:'VALIDE'|'INVALIDE'|'EXPIRE'|'BLOQUE';message:string;tentatives:number}>(`${environment.apiUrl}/auth/verifier-code`, { identifiant: identifier, code });
  }

  loadCurrentUser(): Observable<AuthenticatedUser> {
    return this.http.get<AuthenticatedUser>(`${environment.apiUrl}/auth/me`).pipe(
      tap((user) => {
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
    this.http.post<void>(`${environment.apiUrl}/auth/logout`, {}).subscribe({
      next: () => this.finishLogout(),
      error: () => this.finishLogout()
    });
  }

  getCurrentUser(): AuthenticatedUser | null {
    return this.currentUserSignal();
  }

  isAuthenticated(): boolean {
    return Boolean(this.currentUserSignal());
  }

  hasRole(role: UserRole): boolean {
    return this.currentUserSignal()?.role === role;
  }

  hasAnyRole(...roles: UserRole[]): boolean {
    const currentRole = this.currentUserSignal()?.role;

    return Boolean(currentRole && roles.includes(currentRole));
  }

  clearAuthentication(): void {
    this.currentUserSignal.set(null);
  }

  private finishLogout(): void {
    this.clearAuthentication();
    void this.router.navigateByUrl('/login');
  }
}
