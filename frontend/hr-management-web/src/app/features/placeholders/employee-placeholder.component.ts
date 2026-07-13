import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ProtectedPlaceholderComponent } from './protected-placeholder.component';

@Component({
  selector: 'app-employee-placeholder',
  imports: [ProtectedPlaceholderComponent],
  template: '<app-protected-placeholder title="Employee Area" [user]="authService.currentUser()" />'
})
export class EmployeePlaceholderComponent {
  protected readonly authService = inject(AuthService);
}
