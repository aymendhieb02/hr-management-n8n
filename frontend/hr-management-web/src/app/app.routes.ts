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
        canActivate: [roleGuard],
        data: { title: 'Accueil', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/home/home-summary.component').then((m) => m.HomeSummaryComponent)
      },
      {
        path: 'profile',
        canActivate: [roleGuard],
        data: { title: 'Mon profil', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/profile/profile.component').then((m) => m.ProfileComponent)
      },
      {
        path: 'notifications',
        canActivate: [roleGuard],
        data: { title: 'Notifications', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/notifications/pages/notification-list/notification-list.component')
          .then((m) => m.NotificationListComponent)
      },
      {
        path: 'my-leave-requests',
        canActivate: [roleGuard],
        data: { title: 'Mes demandes de conge', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/leaves/pages/leave-request-list/leave-request-list.component')
          .then((m) => m.LeaveRequestListComponent)
      },
      {
        path: 'request-leave',
        canActivate: [roleGuard],
        data: { title: 'Demander un conge', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/leaves/pages/leave-request-list/leave-request-list.component')
          .then((m) => m.LeaveRequestListComponent)
      },
      {
        path: 'my-balance',
        canActivate: [roleGuard],
        data: { title: 'Mon solde de conges', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/leaves/pages/leave-balance-list/leave-balance-list.component')
          .then((m) => m.LeaveBalanceListComponent)
      },
      {
        path: 'my-calendar',
        canActivate: [roleGuard],
        data: { title: 'Mon calendrier', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/calendar/pages/calendar/calendar.component').then((m) => m.CalendarComponent)
      },
      {
        path: 'team-requests',
        canActivate: [roleGuard],
        data: { title: 'Demandes de l equipe', roles: ['MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/leaves/pages/leave-request-list/leave-request-list.component')
          .then((m) => m.LeaveRequestListComponent)
      },
      {
        path: 'team-members',
        canActivate: [roleGuard],
        data: { title: 'Membres de l equipe', roles: ['MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/users/pages/user-list/user-list.component')
          .then((m) => m.UserListComponent)
      },
      {
        path: 'team-calendar',
        canActivate: [roleGuard],
        data: { title: 'Calendrier de l equipe', roles: ['MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/calendar/pages/calendar/calendar.component').then((m) => m.CalendarComponent)
      },
      {
        path: 'team-availability',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Disponibilite de l equipe', roles: ['MANAGER'] }
      },
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { title: 'Tableau de bord', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/dashboard/pages/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'employees',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Employes', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'positions',
        canActivate: [roleGuard],
        data: { title: 'Postes', roles: ['MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/positions/pages/position-list/position-list.component')
          .then((m) => m.PositionListComponent)
      },
      {
        path: 'leave-types',
        canActivate: [roleGuard],
        data: { title: 'Types de conge', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/leave-types/pages/leave-type-list/leave-type-list.component')
          .then((m) => m.LeaveTypeListComponent)
      },
      {
        path: 'leave-requests',
        canActivate: [roleGuard],
        data: { title: 'Demandes de conge', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/leaves/pages/leave-request-list/leave-request-list.component')
          .then((m) => m.LeaveRequestListComponent)
      },
      {
        path: 'leave-balances',
        canActivate: [roleGuard],
        data: { title: 'Soldes de conges', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/leaves/pages/leave-balance-list/leave-balance-list.component')
          .then((m) => m.LeaveBalanceListComponent)
      },
      {
        path: 'medical-documents',
        canActivate: [roleGuard],
        data: { title: 'Documents medicaux', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/medical-documents/pages/medical-document-admin/medical-document-admin.component')
          .then((m) => m.MedicalDocumentAdminComponent)
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { title: 'Rapports', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/reports/pages/reports/reports.component').then((m) => m.ReportsComponent)
      },
      {
        path: 'calendar',
        canActivate: [roleGuard],
        data: { title: 'Calendrier', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/calendar/pages/calendar/calendar.component').then((m) => m.CalendarComponent)
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: { title: 'Utilisateurs', roles: ['HR', 'ADMIN'] },
        loadComponent: () => import('./features/users/pages/user-list/user-list.component')
          .then((m) => m.UserListComponent)
      },
      {
        path: 'leave-request-statuses',
        canActivate: [roleGuard],
        data: { title: 'Statuts des demandes', roles: ['ADMIN'] },
        loadComponent: () => import('./features/leave-request-statuses/leave-request-statuses.component')
          .then((m) => m.LeaveRequestStatusesComponent)
      },
      {
        path: 'reasons',
        canActivate: [roleGuard],
        data: { title: 'Raisons des demandes', roles: ['ADMIN'] },
        loadComponent: () => import('./features/reasons/reason-admin.component').then((m) => m.ReasonAdminComponent)
      },
      {
        path: 'jours-feries',
        canActivate: [roleGuard],
        data: { title: 'Jours fériés', roles: ['MANAGER', 'HR', 'ADMIN'] },
        loadComponent: () => import('./features/jours-feries/pages/jour-ferie-list/jour-ferie-list.component').then((m) => m.JourFerieListComponent)
      },
      {
        path: 'system-configuration',
        component: PlaceholderPageComponent,
        canActivate: [roleGuard],
        data: { title: 'Configuration systeme', roles: ['ADMIN'] }
      }
      ,{
        path: 'leave-history', canActivate: [roleGuard],
        data: { title: 'Historique des demandes', roles: ['MANAGER', 'ADMIN'] },
        loadComponent: () => import('./features/leave-history/leave-history.component').then((m) => m.LeaveHistoryComponent)
      }
    ]
  },
  {
    path: '**',
    component: HomeRedirectComponent
  }
];
