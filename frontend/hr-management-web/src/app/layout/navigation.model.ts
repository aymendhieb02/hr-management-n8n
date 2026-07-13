import { UserRole } from '../core/models/auth.model';

export interface NavigationItem {
  label: string;
  path: string;
  roles: UserRole[];
}

export const NAVIGATION_ITEMS: NavigationItem[] = [
  { label: 'Home', path: '/home', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'My Profile', path: '/profile', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Notifications', path: '/notifications', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },

  { label: 'My Leave Requests', path: '/my-leave-requests', roles: ['EMPLOYEE', 'MANAGER'] },
  { label: 'Request Leave', path: '/request-leave', roles: ['EMPLOYEE', 'MANAGER'] },
  { label: 'My Leave Balance', path: '/my-balance', roles: ['EMPLOYEE', 'MANAGER'] },
  { label: 'My Calendar', path: '/my-calendar', roles: ['EMPLOYEE'] },

  { label: 'Team Requests', path: '/team-requests', roles: ['MANAGER'] },
  { label: 'Team Calendar', path: '/team-calendar', roles: ['MANAGER'] },

  { label: 'Dashboard', path: '/dashboard', roles: ['HR', 'ADMIN'] },
  { label: 'Employees', path: '/employees', roles: ['HR'] },
  { label: 'Departments', path: '/departments', roles: ['HR', 'ADMIN'] },
  { label: 'Positions', path: '/positions', roles: ['HR', 'ADMIN'] },
  { label: 'Leave Types', path: '/leave-types', roles: ['HR', 'ADMIN'] },
  { label: 'Leave Requests', path: '/leave-requests', roles: ['HR'] },
  { label: 'Leave Balances', path: '/leave-balances', roles: ['HR'] },
  { label: 'Medical Documents', path: '/medical-documents', roles: ['HR'] },
  { label: 'Reports', path: '/reports', roles: ['HR'] },
  { label: 'Calendar', path: '/calendar', roles: ['HR'] },
  { label: 'Users', path: '/users', roles: ['ADMIN'] },
  { label: 'System Configuration', path: '/system-configuration', roles: ['ADMIN'] }
];
