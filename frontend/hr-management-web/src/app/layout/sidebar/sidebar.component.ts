import { Component, computed, inject, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LayoutStateService } from '../layout-state.service';
import { NAVIGATION_ITEMS } from '../navigation.model';
import { NotificationService } from '../../features/notifications/services/notification.service';
import { AppIconComponent } from '../../shared/components/app-icon/app-icon.component';

@Component({
  selector: 'app-sidebar',
  imports: [AppIconComponent, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly layoutState = inject(LayoutStateService);
  protected readonly notificationService = inject(NotificationService);

  protected readonly items = computed(() => {
    const role = this.authService.currentUser()?.role;

    return role ? NAVIGATION_ITEMS.filter((item) => item.roles.includes(role)) : [];
  });

  ngOnInit(): void {
    this.notificationService.loadCurrentUser().subscribe({ error: () => undefined });
  }

  protected closeSidebar(): void {
    this.layoutState.closeSidebar();
  }
}
