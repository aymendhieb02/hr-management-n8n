import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { HomeRedirectComponent } from './shared/home-redirect.component';
import { AppLayoutComponent } from './layout/app-layout/app-layout.component';
import { PlaceholderPageComponent } from './features/placeholders/placeholder-page.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'forbidden',
    loadComponent: () => import('./features/placeholders/forbidden.component').then((m) => m.ForbiddenComponent)
  },
  {
    path: '',
    pathMatch: 'full',
    component: HomeRedirectComponent
  },
  {
    path: '',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Home', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] }
      },
      {
        path: 'profile',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'My Profile', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] }
      },
      {
        path: 'notifications',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Notifications', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] }
      },
      {
        path: 'my-leave-requests',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'My Leave Requests', roles: ['EMPLOYEE', 'MANAGER'] }
      },
      {
        path: 'request-leave',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Request Leave', roles: ['EMPLOYEE', 'MANAGER'] }
      },
      {
        path: 'my-balance',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'My Leave Balance', roles: ['EMPLOYEE', 'MANAGER'] }
      },
      {
        path: 'my-calendar',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'My Calendar', roles: ['EMPLOYEE'] }
      },
      {
        path: 'team-requests',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Team Requests', roles: ['MANAGER'] }
      },
      {
        path: 'team-calendar',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Team Calendar', roles: ['MANAGER'] }
      },
      {
        path: 'dashboard',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Dashboard', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'employees',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Employees', roles: ['HR'] }
      },
      {
        path: 'departments',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Departments', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'positions',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Positions', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'leave-types',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Leave Types', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'leave-requests',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Leave Requests', roles: ['HR'] }
      },
      {
        path: 'leave-balances',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Leave Balances', roles: ['HR'] }
      },
      {
        path: 'medical-documents',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Medical Documents', roles: ['HR'] }
      },
      {
        path: 'reports',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Reports', roles: ['HR'] }
      },
      {
        path: 'calendar',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Calendar', roles: ['HR'] }
      },
      {
        path: 'users',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Users', roles: ['ADMIN'] }
      },
      {
        path: 'system-configuration',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'System Configuration', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '**',
    component: HomeRedirectComponent
  }
];
