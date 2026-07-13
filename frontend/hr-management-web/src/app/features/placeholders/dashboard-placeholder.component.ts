import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ProtectedPlaceholderComponent } from './protected-placeholder.component';

@Component({
  selector: 'app-dashboard-placeholder',
  imports: [ProtectedPlaceholderComponent],
  template: '<app-protected-placeholder title="Dashboard" [user]="authService.currentUser()" />'
})
export class DashboardPlaceholderComponent {
  protected readonly authService = inject(AuthService);
}
