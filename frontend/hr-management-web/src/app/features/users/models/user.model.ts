export type RoleType = 'EMPLOYEE' | 'MANAGER' | 'DG' | 'DT' | 'HR' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface UserSummary {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface UserReferenceSummary {
  id: number;
  name: string;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  address?: string | null;
  birthDate?: string | null;
  sex?: string | null;
  hireDate: string | null;
  role: RoleType;
  status: UserStatus;
  enabled: boolean;
  managerId: number | null;
  departmentId: number | null;
  positionId: number | null;
  typeContractId?: number | null;
  photoFile?: File | null;
}

export interface UserUpdateRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  address?: string | null;
  birthDate?: string | null;
  sex?: string | null;
  hireDate: string | null;
  role: RoleType;
  status: UserStatus;
  enabled: boolean;
  managerId: number | null;
  departmentId: number | null;
  positionId: number | null;
  typeContractId?: number | null;
}

export interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  address?: string | null;
  birthDate?: string | null;
  sex?: string | null;
  hireDate: string | null;
  role: RoleType;
  status: UserStatus;
  enabled: boolean;
  manager: UserSummary | null;
  department: UserReferenceSummary | null;
  position: UserReferenceSummary | null;
  typeContract?: UserReferenceSummary | null;
  createdAt: string;
  updatedAt: string | null;
  photoUrl?: string | null;
  passwordChangeRequired?: boolean;
}
