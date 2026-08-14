import { UserRole } from '../core/models/auth.model';

export interface NavigationItem {
  label: string;
  path: string;
  roles: UserRole[];
  icon: string;
}

export const NAVIGATION_ITEMS: NavigationItem[] = [
  { label: 'Accueil', path: '/home', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'], icon: 'home' },
  { label: 'Tableau de bord', path: '/dashboard', roles: ['MANAGER', 'HR', 'ADMIN'], icon: 'dashboard' },
  { label: 'Mon profil', path: '/profile', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'], icon: 'profile' },
  { label: 'Notifications', path: '/notifications', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'], icon: 'bell' },

  { label: 'Mes demandes de congé', path: '/my-leave-requests', roles: ['EMPLOYEE', 'HR'], icon: 'request' },
  { label: 'Demander un congé', path: '/request-leave', roles: ['EMPLOYEE', 'HR'], icon: 'add-calendar' },
  { label: 'Mon solde de congé', path: '/my-balance', roles: ['EMPLOYEE', 'HR'], icon: 'balance' },
  { label: 'Mon calendrier', path: '/my-calendar', roles: ['EMPLOYEE', 'HR'], icon: 'calendar' },

  { label: "Demandes de l'équipe", path: '/team-requests', roles: ['MANAGER', 'HR'], icon: 'team-request' },
  { label: "Membres de l'équipe", path: '/team-members', roles: ['MANAGER'], icon: 'team' },
  { label: "Calendrier de l'équipe", path: '/team-calendar', roles: ['MANAGER', 'HR'], icon: 'calendar' },
  { label: "Disponibilité de l'équipe", path: '/team-availability', roles: ['MANAGER'], icon: 'availability' },
  { label: 'Transactions de congé', path: '/balance-transactions', roles: ['MANAGER', 'HR', 'ADMIN'], icon: 'transactions' },

  { label: 'Employés', path: '/users', roles: ['HR'], icon: 'team' },
  { label: 'Postes', path: '/positions', roles: ['ADMIN'], icon: 'position' },
  { label: 'Demandes de congé', path: '/leave-requests', roles: ['HR'], icon: 'request' },
  { label: 'Soldes de congé', path: '/leave-balances', roles: ['HR', 'ADMIN'], icon: 'balance' },
  { label: 'Documents médicaux', path: '/medical-documents', roles: ['HR', 'ADMIN'], icon: 'medical' },
  { label: 'Rapports', path: '/reports', roles: ['HR', 'ADMIN'], icon: 'reports' },
  { label: 'Calendrier', path: '/calendar', roles: ['HR', 'ADMIN'], icon: 'calendar' },
  { label: 'Utilisateurs', path: '/users', roles: ['ADMIN'], icon: 'team' },
  { label: 'Statuts des demandes', path: '/leave-request-statuses', roles: ['ADMIN'], icon: 'status' },
  { label: 'Types de contrat', path: '/contract-types', roles: ['ADMIN'], icon: 'contract' },
  { label: 'Types de congé', path: '/reasons', roles: ['ADMIN'], icon: 'types' },
  { label: 'Jours fériés', path: '/jours-feries', roles: ['ADMIN'], icon: 'holiday' },
  { label: 'Historique des demandes', path: '/leave-history', roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'], icon: 'history' },
  { label: 'Configuration système', path: '/system-configuration', roles: ['ADMIN'], icon: 'settings' }
];
