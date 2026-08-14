export type UserRole = 'EMPLOYEE' | 'MANAGER' | 'HR' | 'ADMIN';

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface AuthenticatedUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  photoUrl?: string | null;
  passwordChangeRequired?: boolean;
}

export interface LoginResponse {
  expiresIn: number;
  user: AuthenticatedUser;
}
