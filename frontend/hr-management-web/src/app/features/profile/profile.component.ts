import { Component, inject, OnInit, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { safeApiMessage } from '../shared/api-error.util';
import { PasswordUpdateDialogComponent } from '../users/components/password-update-dialog/password-update-dialog.component';
import { PasswordUpdateRequest, UserResponse } from '../users/models/user.model';
import { UserService } from '../users/services/user.service';
@Component({selector:'app-profile',imports:[PasswordUpdateDialogComponent],templateUrl:'./profile.component.html',styleUrls:['../shared/resource-page.scss','./profile.component.scss']})
export class ProfileComponent implements OnInit{
 private readonly userService=inject(UserService);private readonly authService=inject(AuthService);
 protected readonly user=signal<UserResponse|null>(null);protected readonly loading=signal(true);protected readonly saving=signal(false);protected readonly photoSaving=signal(false);protected readonly passwordOpen=signal(false);protected readonly error=signal<string|null>(null);protected readonly success=signal<string|null>(null);protected readonly pendingPhoto=signal<File|null>(null);
 ngOnInit(){this.userService.findMe().subscribe({next:u=>{this.user.set(u);const changementRequis=Boolean(u.passwordChangeRequired);this.passwordOpen.set(changementRequis);if(changementRequis)this.success.set('Pour sécuriser votre compte, vous devez remplacer le mot de passe temporaire avant de continuer.');this.loading.set(false);},error:e=>{this.error.set(safeApiMessage(e,'Impossible de charger le profil.'));this.loading.set(false);}});}
 protected updatePassword(r:PasswordUpdateRequest){this.saving.set(true);this.error.set(null);this.userService.updatePassword(this.user()!.id,r).subscribe({next:()=>{this.saving.set(false);this.passwordOpen.set(false);this.success.set('Mot de passe modifié avec succès.');this.authService.loadCurrentUser().subscribe();},error:e=>{this.saving.set(false);this.error.set(safeApiMessage(e,'Impossible de modifier le mot de passe.'));}});}
 protected photoUrl(p:UserResponse){return p.photoUrl?`${environment.apiUrl.replace('/api','')}${p.photoUrl}?v=${p.updatedAt??p.createdAt}`:'';}
 protected choosePhoto(event:Event){const input=event.target as HTMLInputElement;const file=input.files?.[0];input.value='';if(!file)return;if(file.size>10*1024*1024){this.error.set('La photo ne doit pas dépasser 10 Mo.');return;}this.pendingPhoto.set(file);}
 protected confirmPhoto(){const file=this.pendingPhoto();if(!file)return;this.pendingPhoto.set(null);this.photoSaving.set(true);this.error.set(null);this.userService.uploadOwnPhoto(file).subscribe({next:()=>{this.photoSaving.set(false);this.success.set('Photo de profil mise à jour.');this.ngOnInit();this.authService.loadCurrentUser().subscribe();},error:e=>{this.photoSaving.set(false);this.error.set(safeApiMessage(e,"Impossible d'enregistrer la photo."));}});}
}
