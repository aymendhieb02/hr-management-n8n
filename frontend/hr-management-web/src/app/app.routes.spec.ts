import { routes } from './app.routes';

describe('app route role data', () => {
  const childRoutes = routes.find((route) => route.children)?.children ?? [];

  it('allows managers to access team and reference routes', () => {
    expect(rolesFor('team-requests')).toContain('MANAGER');
    expect(rolesFor('team-requests')).toContain('HR');
    expect(rolesFor('team-requests')).toContain('ADMIN');
    expect(rolesFor('team-members')).toContain('MANAGER');
    expect(rolesFor('team-members')).toContain('HR');
    expect(rolesFor('team-members')).toContain('ADMIN');
    expect(rolesFor('team-availability')).toContain('MANAGER');
    expect(rolesFor('positions')).toContain('MANAGER');
    expect(rolesFor('leave-types')).toContain('MANAGER');
  });

  it('allows all business roles to access their own leave pages', () => {
    expect(rolesFor('my-leave-requests')).toEqual(['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN']);
    expect(rolesFor('request-leave')).toEqual(['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN']);
    expect(rolesFor('my-balance')).toEqual(['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN']);
    expect(rolesFor('my-calendar')).toEqual(['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN']);
  });

  it('keeps dashboards and global reports restricted to HR and Admin', () => {
    expect(rolesFor('dashboard')).toEqual(['HR', 'ADMIN']);
    expect(rolesFor('reports')).toEqual(['HR', 'ADMIN']);
    expect(rolesFor('calendar')).toEqual(['HR', 'ADMIN']);
    expect(rolesFor('dashboard')).not.toContain('EMPLOYEE');
  });

  it('allows managers to access team calendar without global calendar access', () => {
    expect(rolesFor('team-calendar')).toEqual(['MANAGER', 'HR', 'ADMIN']);
    expect(rolesFor('calendar')).not.toContain('MANAGER');
  });

  it('keeps medical documents restricted to HR and Admin', () => {
    expect(rolesFor('medical-documents')).toEqual(['HR', 'ADMIN']);
    expect(rolesFor('medical-documents')).not.toContain('MANAGER');
  });

  it('keeps user administration inaccessible to employees', () => {
    expect(rolesFor('users')).toEqual(['HR', 'ADMIN']);
    expect(rolesFor('users')).not.toContain('EMPLOYEE');
    expect(rolesFor('team-members')).not.toContain('EMPLOYEE');
  });

  it('allows Admin to access HR business modules and system configuration', () => {
    expect(rolesFor('users')).toContain('ADMIN');
    expect(rolesFor('leave-requests')).toContain('ADMIN');
    expect(rolesFor('leave-balances')).toContain('ADMIN');
    expect(rolesFor('reports')).toContain('ADMIN');
    expect(rolesFor('calendar')).toContain('ADMIN');
    expect(rolesFor('leave-request-statuses')).toEqual(['ADMIN']);
    expect(rolesFor('system-configuration')).toEqual(['ADMIN']);
  });

  function rolesFor(path: string): string[] {
    const route = childRoutes.find((candidate) => candidate.path === path);

    return (route?.data?.['roles'] ?? []) as string[];
  }
});

