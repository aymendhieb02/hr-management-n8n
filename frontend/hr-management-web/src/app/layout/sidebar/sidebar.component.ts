import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LayoutStateService } from '../layout-state.service';
import { NAVIGATION_ITEMS } from '../navigation.model';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly layoutState = inject(LayoutStateService);

  protected readonly items = computed(() => {
    const role = this.authService.currentUser()?.role;

    return role ? NAVIGATION_ITEMS.filter((item) => item.roles.includes(role)) : [];
  });

  protected closeSidebar(): void {
    this.layoutState.closeSidebar();
  }
}
