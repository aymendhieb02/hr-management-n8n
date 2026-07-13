import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LayoutStateService } from '../layout-state.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly layoutState = inject(LayoutStateService);

  protected readonly user = this.authService.currentUser;
  protected readonly fullName = computed(() => {
    const currentUser = this.user();

    return currentUser ? `${currentUser.firstName} ${currentUser.lastName}` : '';
  });

  toggleSidebar(): void {
    this.layoutState.toggleSidebar();
  }

  logout(): void {
    this.authService.logout();
  }
}
