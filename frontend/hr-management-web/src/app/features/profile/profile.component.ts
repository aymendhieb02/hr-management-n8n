import { Component, inject, OnInit, signal } from '@angular/core';
import { safeApiMessage } from '../shared/api-error.util';
import { PasswordUpdateDialogComponent } from '../users/components/password-update-dialog/password-update-dialog.component';
import { PasswordUpdateRequest, UserResponse } from '../users/models/user.model';
import { UserService } from '../users/services/user.service';

@Component({
  selector: 'app-profile',
  imports: [PasswordUpdateDialogComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['../shared/resource-page.scss', './profile.component.scss']
})
export class ProfileComponent implements OnInit {
  private readonly userService = inject(UserService);
  protected readonly user = signal<UserResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly passwordOpen = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

  ngOnInit(): void { this.userService.findMe().subscribe({ next: user => { this.user.set(user); this.loading.set(false); }, error: error => { this.error.set(safeApiMessage(error, 'Profil impossible à charger.')); this.loading.set(false); } }); }

  protected updatePassword(request: PasswordUpdateRequest): void {
    this.saving.set(true); this.error.set(null); this.success.set(null);
    this.userService.updatePassword(this.user()!.id, request).subscribe({
      next: () => { this.saving.set(false); this.passwordOpen.set(false); this.success.set('Mot de passe modifié avec succès.'); },
      error: error => { this.saving.set(false); this.error.set(safeApiMessage(error, 'Le mot de passe n’a pas pu être modifié.')); }
    });
  }
}
