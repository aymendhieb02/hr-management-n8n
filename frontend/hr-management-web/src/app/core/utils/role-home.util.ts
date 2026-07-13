import { AuthenticatedUser } from '../models/auth.model';

export function homeUrlForUser(user: AuthenticatedUser | null): string {
  if (!user) {
    return '/login';
  }

  if (user.role === 'ADMIN' || user.role === 'HR') {
    return '/dashboard';
  }

  return '/home';
}
