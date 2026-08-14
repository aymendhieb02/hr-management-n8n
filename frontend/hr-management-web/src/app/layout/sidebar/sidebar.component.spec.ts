import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthenticatedUser } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { SidebarComponent } from './sidebar.component';

@Component({ template: '' })
class EmptyComponent {}

describe('SidebarComponent', () => {
  const user: AuthenticatedUser = {
    id: 1,
    username: 'employee',
    email: 'employee@xtensus.local',
    firstName: 'Employee',
    lastName: 'User',
    role: 'EMPLOYEE'
  };
  const currentUser = signal<AuthenticatedUser | null>(user);
  const authService = {
    currentUser: currentUser.asReadonly()
  };

  let fixture: ComponentFixture<SidebarComponent>;
  let router: Router;

  beforeEach(async () => {
    currentUser.set(user);
    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [
        provideRouter([
          { path: 'home', component: EmptyComponent },
          { path: 'profile', component: EmptyComponent },
          { path: 'medical-documents', component: EmptyComponent }
        ]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(SidebarComponent);
    fixture.detectChanges();
  });

  it('shows employee self-service menu items', () => {
    expect(text()).toContain('Mes demandes de congé');
    expect(text()).toContain('Demander un congé');
    expect(text()).toContain('Mon solde de congé');
  });

  it('does not show HR or Admin menu items to employees', () => {
    expect(text()).not.toContain('Employes');
    expect(text()).not.toContain('Utilisateurs');
    expect(text()).not.toContain('Documents medicaux');
    expect(text()).not.toContain('Configuration systeme');
  });

  it('shows manager team items and hides employee self-service items', () => {
    currentUser.set({ ...user, role: 'MANAGER' });
    fixture.detectChanges();

    expect(text()).toContain('Tableau de bord');
    expect(text()).toContain("Demandes de l'équipe");
    expect(text()).toContain("Membres de l'équipe");
    expect(text()).toContain("Disponibilité de l'équipe");
    expect(text()).not.toContain('Mes demandes de congé');
    expect(text()).not.toContain('Demander un congé');
    expect(text()).not.toContain('Mon solde de congé');
    expect(text()).not.toContain('Mon calendrier');
  });

  it('shows only applicable reference menu items to managers', () => {
    currentUser.set({ ...user, role: 'MANAGER' });
    fixture.detectChanges();

    expect(text()).not.toContain('Postes');
    expect(text()).not.toContain('Jours fériés');
    expect(text()).not.toContain('Types de conge');
  });

  it('does not show restricted medical or system configuration items to managers', () => {
    currentUser.set({ ...user, role: 'MANAGER' });
    fixture.detectChanges();

    expect(text()).not.toContain('Documents médicaux');
    expect(text()).not.toContain('Configuration système');
  });

  it('shows medical documents to HR', () => {
    currentUser.set({ ...user, role: 'HR' });
    fixture.detectChanges();

    expect(text()).toContain('Employés');
    expect(text()).toContain("Demandes de l'équipe");
    expect(text()).toContain('Mes demandes de congé');
    expect(text()).toContain('Mon solde de congé');
    expect(text()).not.toContain('Utilisateurs');
    expect(text()).toContain('Documents médicaux');
  });

  it('shows HR business modules and medical documents to Admin', () => {
    currentUser.set({ ...user, role: 'ADMIN' });
    fixture.detectChanges();

    expect(text()).toContain('Utilisateurs');
    expect(text()).not.toContain('Demandes de conge');
    expect(text()).not.toContain('Demander un congé');
    expect(text()).toContain('Soldes de congé');
    expect(text()).toContain('Documents médicaux');
    expect(text()).toContain('Rapports');
    expect(text()).toContain('Calendrier');
    expect(text()).toContain('Statuts des demandes');
    expect(text()).toContain('Postes');
    expect(text()).toContain('Jours fériés');
    expect(text()).toContain('Types de congé');
    expect(text()).toContain('Configuration système');
  });

  it('applies active styling to the current route', async () => {
    await router.navigateByUrl('/profile');
    fixture.detectChanges();
    await fixture.whenStable();

    const activeLink = fixture.nativeElement.querySelector('a.active') as HTMLAnchorElement;
    expect(activeLink.textContent?.trim()).toBe('Mon profil');
  });

  function text(): string {
    return fixture.nativeElement.textContent;
  }
});

