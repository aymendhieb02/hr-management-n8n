import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ProtectedPlaceholderComponent } from './protected-placeholder.component';

@Component({
  selector: 'app-placeholder-page',
  imports: [ProtectedPlaceholderComponent],
  template: '<app-protected-placeholder [title]="title()" [user]="authService.currentUser()" />'
})
export class PlaceholderPageComponent {
  private readonly route = inject(ActivatedRoute);
  protected readonly authService = inject(AuthService);
  private readonly data = toSignal(this.route.data, { initialValue: this.route.snapshot.data });
  protected readonly title = computed(() => String(this.data()['title'] ?? 'Page'));
}
