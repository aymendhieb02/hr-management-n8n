import { UserRole } from '../core/models/auth.model';

export interface NavigationItem {
  label: string;
  path: string;
  roles: UserRole[];
}

export const NAVIGATION_ITEMS: NavigationItem[] = [
  { label: 'Accueil', path: '/home', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Tableau de bord', path: '/dashboard', roles: ['HR', 'ADMIN'] },
  { label: 'Mon profil', path: '/profile', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },
  { label: 'Notifications', path: '/notifications', roles: ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN'] },

  { label: 'Mes demandes de congé', path: '/my-leave-requests', roles: ['EMPLOYEE', 'HR'] },
  { label: 'Demander un congé', path: '/request-leave', roles: ['EMPLOYEE', 'HR'] },
  { label: 'Mon solde de congé', path: '/my-balance', roles: ['EMPLOYEE', 'HR'] },
  { label: 'Mon calendrier', path: '/my-calendar', roles: ['EMPLOYEE', 'HR'] },

  { label: 'Demandes de l equipe', path: '/team-requests', roles: ['MANAGER', 'HR'] },
  { label: 'Membres de l equipe', path: '/team-members', roles: ['MANAGER'] },
  { label: 'Calendrier de l equipe', path: '/team-calendar', roles: ['MANAGER', 'HR'] },
  { label: "Disponibilité de l'équipe", path: '/team-availability', roles: ['MANAGER'] },
  { label: 'Transactions de congé', path: '/balance-transactions', roles: ['MANAGER', 'HR', 'ADMIN'] },

  { label: 'Employes', path: '/users', roles: ['HR'] },
  { label: 'Postes', path: '/positions', roles: ['MANAGER', 'HR', 'ADMIN'] },
  { label: 'Demandes de conge', path: '/leave-requests', roles: ['HR'] },
  { label: 'Soldes de congé', path: '/leave-balances', roles: ['HR', 'ADMIN'] },
  { label: 'Documents medicaux', path: '/medical-documents', roles: ['HR', 'ADMIN'] },
  { label: 'Rapports', path: '/reports', roles: ['HR', 'ADMIN'] },
  { label: 'Calendrier', path: '/calendar', roles: ['HR', 'ADMIN'] },
  { label: 'Utilisateurs', path: '/users', roles: ['ADMIN'] },
  { label: 'Statuts des demandes', path: '/leave-request-statuses', roles: ['ADMIN'] },
  { label: 'Types de contrat', path: '/contract-types', roles: ['ADMIN'] },
  { label: 'Types de conge', path: '/reasons', roles: ['ADMIN'] },
  { label: 'Jours fériés', path: '/jours-feries', roles: ['MANAGER', 'HR', 'ADMIN'] },
  { label: 'Historique des demandes', path: '/leave-history', roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
  { label: 'Configuration systeme', path: '/system-configuration', roles: ['ADMIN'] }
];
