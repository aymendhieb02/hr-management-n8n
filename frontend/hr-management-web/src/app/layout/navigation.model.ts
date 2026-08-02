import { UserRole } from '../core/models/auth.model';

export interface NavigationItem {
  label: string;
  path: string;
  roles: UserRole[];
}

export const NAVIGATION_ITEMS: NavigationItem[] = [
  { label: 'Accueil', path: '/home', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Mon profil', path: '/profile', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Notifications', path: '/notifications', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },

  { label: 'Mes demandes de conge', path: '/my-leave-requests', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Demander un conge', path: '/request-leave', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Mon solde de conges', path: '/my-balance', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Mon calendrier', path: '/my-calendar', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },

  { label: 'Demandes de l equipe', path: '/team-requests', roles: ['MANAGER', 'HR', 'ADMIN'] },
  { label: 'Membres de l equipe', path: '/team-members', roles: ['MANAGER'] },
  { label: 'Calendrier de l equipe', path: '/team-calendar', roles: ['MANAGER', 'HR', 'ADMIN'] },
  { label: 'Disponibilite de l equipe', path: '/team-availability', roles: ['MANAGER'] },

  { label: 'Tableau de bord', path: '/dashboard', roles: ['HR', 'ADMIN'] },
  { label: 'Employes', path: '/users', roles: ['HR'] },
  { label: 'Postes', path: '/positions', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Types de conge', path: '/leave-types', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Demandes de conge', path: '/leave-requests', roles: ['HR', 'ADMIN'] },
  { label: 'Soldes de conges', path: '/leave-balances', roles: ['HR', 'ADMIN'] },
  { label: 'Documents medicaux', path: '/medical-documents', roles: ['HR', 'ADMIN'] },
  { label: 'Rapports', path: '/reports', roles: ['HR', 'ADMIN'] },
  { label: 'Calendrier', path: '/calendar', roles: ['HR', 'ADMIN'] },
  { label: 'Utilisateurs', path: '/users', roles: ['ADMIN'] },
  { label: 'Configuration systeme', path: '/system-configuration', roles: ['ADMIN'] }
];
