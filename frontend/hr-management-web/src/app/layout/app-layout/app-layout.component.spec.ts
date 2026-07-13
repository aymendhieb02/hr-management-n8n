import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthenticatedUser } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { authGuard } from '../../core/guards/auth.guard';
import { LayoutStateService } from '../layout-state.service';
import { AppLayoutComponent } from './app-layout.component';

@Component({ template: '' })
class EmptyComponent {}

describe('AppLayoutComponent', () => {
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
    currentUser: currentUser.asReadonly(),
    isAuthenticated: vi.fn(() => true),
    logout: vi.fn()
  };

  beforeEach(() => {
    currentUser.set(user);
    vi.clearAllMocks();
  });

  it('renders the authenticated layout for the current user', async () => {
    await TestBed.configureTestingModule({
      imports: [AppLayoutComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(AppLayoutComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('XTENSUS HR');
    expect(fixture.nativeElement.textContent).toContain('Employee User');
    expect(fixture.nativeElement.textContent).toContain('Home');
  });

  it('blocks unauthenticated users from the layout route', async () => {
    authService.isAuthenticated.mockReturnValue(false);

    await TestBed.configureTestingModule({
      imports: [AppLayoutComponent],
      providers: [
        provideRouter([
          { path: 'login', component: EmptyComponent },
          { path: 'home', component: AppLayoutComponent, canActivate: [authGuard] }
        ]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    const router = TestBed.inject(Router);
    await router.navigateByUrl('/home');

    expect(router.url).toBe('/login');
  });

  it('toggles the mobile sidebar state', async () => {
    await TestBed.configureTestingModule({
      imports: [AppLayoutComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    const fixture: ComponentFixture<AppLayoutComponent> = TestBed.createComponent(AppLayoutComponent);
    const layoutState = TestBed.inject(LayoutStateService);

    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.layout-shell')?.classList.contains('sidebar-open')).toBe(false);

    layoutState.toggleSidebar();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.layout-shell')?.classList.contains('sidebar-open')).toBe(true);
  });
});
