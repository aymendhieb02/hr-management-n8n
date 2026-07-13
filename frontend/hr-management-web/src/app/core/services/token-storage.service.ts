import { isPlatformBrowser } from '@angular/common';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { AuthenticatedUser } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'xtensus_hr_access_token';
const AUTH_USER_KEY = 'xtensus_hr_authenticated_user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private readonly platformId = inject(PLATFORM_ID);

  saveAccessToken(token: string): void {
    this.storage?.setItem(ACCESS_TOKEN_KEY, token);
  }

  getAccessToken(): string | null {
    return this.storage?.getItem(ACCESS_TOKEN_KEY) ?? null;
  }

  removeAccessToken(): void {
    this.storage?.removeItem(ACCESS_TOKEN_KEY);
  }

  saveAuthenticatedUser(user: AuthenticatedUser): void {
    this.storage?.setItem(AUTH_USER_KEY, JSON.stringify(user));
  }

  getAuthenticatedUser(): AuthenticatedUser | null {
    const rawUser = this.storage?.getItem(AUTH_USER_KEY);

    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as AuthenticatedUser;
    } catch {
      this.clearAuthData();
      return null;
    }
  }

  clearAuthData(): void {
    this.removeAccessToken();
    this.storage?.removeItem(AUTH_USER_KEY);
  }

  private get storage(): Storage | null {
    return isPlatformBrowser(this.platformId) ? sessionStorage : null;
  }
}
