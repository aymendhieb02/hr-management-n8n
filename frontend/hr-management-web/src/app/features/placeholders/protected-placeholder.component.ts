import { Component, computed, input } from '@angular/core';
import { AuthenticatedUser } from '../../core/models/auth.model';

@Component({
  selector: 'app-protected-placeholder',
  templateUrl: './protected-placeholder.component.html',
  styleUrl: './protected-placeholder.component.scss'
})
export class ProtectedPlaceholderComponent {
  readonly title = input.required<string>();
  readonly user = input.required<AuthenticatedUser | null>();
  protected readonly fullName = computed(() => {
    const currentUser = this.user();

    return currentUser ? `${currentUser.firstName} ${currentUser.lastName}` : 'Unknown user';
  });
}
