import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthenticatedUser } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  const user: AuthenticatedUser = {
    id: 1,
    username: 'hr',
    email: 'hr@xtensus.local',
    firstName: 'Hana',
    lastName: 'Rami',
    role: 'HR'
  };
  const authService = {
    currentUser: signal<AuthenticatedUser | null>(user).asReadonly(),
    logout: vi.fn()
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();
  });

  it('logs out through AuthService', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.logout-button') as HTMLButtonElement).click();

    expect(authService.logout).toHaveBeenCalled();
  });
});
