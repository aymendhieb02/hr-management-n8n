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
    expect(text()).toContain('My Leave Requests');
    expect(text()).toContain('Request Leave');
    expect(text()).toContain('My Leave Balance');
  });

  it('does not show HR or Admin menu items to employees', () => {
    expect(text()).not.toContain('Employees');
    expect(text()).not.toContain('Medical Documents');
    expect(text()).not.toContain('System Configuration');
  });

  it('shows manager team and self-service menu items', () => {
    currentUser.set({ ...user, role: 'MANAGER' });
    fixture.detectChanges();

    expect(text()).toContain('Team Requests');
    expect(text()).toContain('My Leave Requests');
    expect(text()).toContain('Request Leave');
  });

  it('shows medical documents to HR', () => {
    currentUser.set({ ...user, role: 'HR' });
    fixture.detectChanges();

    expect(text()).toContain('Medical Documents');
  });

  it('does not show medical documents to Admin', () => {
    currentUser.set({ ...user, role: 'ADMIN' });
    fixture.detectChanges();

    expect(text()).not.toContain('Medical Documents');
  });

  it('applies active styling to the current route', async () => {
    await router.navigateByUrl('/profile');
    fixture.detectChanges();
    await fixture.whenStable();

    const activeLink = fixture.nativeElement.querySelector('a.active') as HTMLAnchorElement;
    expect(activeLink.textContent?.trim()).toBe('My Profile');
  });

  function text(): string {
    return fixture.nativeElement.textContent;
  }
});
