import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ProtectedPlaceholderComponent } from './protected-placeholder.component';

@Component({
  selector: 'app-manager-placeholder',
  imports: [ProtectedPlaceholderComponent],
  template: '<app-protected-placeholder title="Manager Area" [user]="authService.currentUser()" />'
})
export class ManagerPlaceholderComponent {
  protected readonly authService = inject(AuthService);
}
