import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { homeUrlForUser } from '../../../core/utils/role-home.util';

@Component({selector:'app-login',imports:[ReactiveFormsModule],templateUrl:'./login.component.html',styleUrl:'./login.component.scss'})
export class LoginComponent implements OnInit,OnDestroy {
  private readonly formBuilder=inject(FormBuilder); private readonly authService=inject(AuthService); private readonly router=inject(Router);
  protected readonly isLoading=signal(false); protected readonly errorMessage=signal<string|null>(null); protected readonly successMessage=signal<string|null>(null);
  protected readonly resetStep=signal<0|1|2>(0); protected readonly resetIdentifier=signal('');
  protected readonly codeStatus=signal<'idle'|'checking'|'valid'|'invalid'|'expired'>('idle'); protected readonly codeMessage=signal<string|null>(null);
  private verificationTimer?:ReturnType<typeof setTimeout>;
  private expirationTimer?:ReturnType<typeof setInterval>; protected readonly secondesRestantes=signal(0);
  protected readonly form=this.formBuilder.nonNullable.group({usernameOrEmail:['',Validators.required],password:['',Validators.required]});
  protected readonly resetForm=this.formBuilder.nonNullable.group({identifier:['',Validators.required],code:['',[Validators.required,Validators.pattern(/^\d{6}$/)]],newPassword:['',[Validators.required,Validators.minLength(8)]],confirmation:['',Validators.required]});

  ngOnInit():void{if(this.authService.isAuthenticated())void this.router.navigateByUrl(homeUrlForUser(this.authService.getCurrentUser()));}
  ngOnDestroy():void{clearTimeout(this.verificationTimer);clearInterval(this.expirationTimer);}
  requestReset():void{const identifier=this.resetStep()===2?this.resetIdentifier():this.resetForm.controls.identifier.value.trim();if(!identifier||this.isLoading())return;this.isLoading.set(true);this.errorMessage.set(null);this.successMessage.set(null);this.authService.requestPasswordReset(identifier).subscribe({next:r=>{this.resetIdentifier.set(identifier);this.resetStep.set(2);this.resetForm.patchValue({code:'',newPassword:'',confirmation:''});this.codeStatus.set('idle');this.codeMessage.set(null);this.demarrerExpiration(r.expireLe);this.isLoading.set(false);},error:e=>{this.errorMessage.set(e?.error?.message??"Impossible d'envoyer le code.");this.isLoading.set(false);}});}
  private demarrerExpiration(expireLe:string):void{clearInterval(this.expirationTimer);const update=()=>{const reste=Math.max(0,Math.ceil((new Date(expireLe).getTime()-Date.now())/1000));this.secondesRestantes.set(reste);if(reste===0){clearInterval(this.expirationTimer);this.codeStatus.set('expired');this.codeMessage.set('Ce code a expiré. Demandez un nouveau code.');}};update();this.expirationTimer=setInterval(update,1000);}
  protected tempsRestant():string{const s=this.secondesRestantes();return `${Math.floor(s/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`;}
  onCodeInput():void{clearTimeout(this.verificationTimer);this.codeStatus.set('idle');this.codeMessage.set(null);const code=this.resetForm.controls.code.value.replace(/\D/g,'').slice(0,6);this.resetForm.controls.code.setValue(code,{emitEvent:false});if(code.length!==6)return;this.codeStatus.set('checking');this.verificationTimer=setTimeout(()=>this.authService.verifyResetCode(this.resetIdentifier(),code).subscribe({next:r=>{const expired=r.statut==='EXPIRE'||r.statut==='BLOQUE';this.codeStatus.set(r.statut==='VALIDE'?'valid':expired?'expired':'invalid');this.codeMessage.set(r.message);},error:e=>{this.codeStatus.set('invalid');this.codeMessage.set(e?.error?.message??'Impossible de vérifier le code.');}}),250);}
  protected passwordsMatch():boolean{const c=this.resetForm.controls;return !!c.newPassword.value&&c.newPassword.value===c.confirmation.value;}
  protected canReset():boolean{return this.codeStatus()==='valid'&&this.passwordsMatch()&&this.resetForm.controls.newPassword.valid&&!this.isLoading();}
  confirmReset():void{this.resetForm.markAllAsTouched();const value=this.resetForm.getRawValue();if(!this.passwordsMatch()){this.errorMessage.set('Les deux mots de passe doivent être identiques.');return;}if(!this.canReset())return;this.isLoading.set(true);this.errorMessage.set(null);this.authService.resetPassword(this.resetIdentifier(),value.code,value.newPassword).subscribe({next:()=>{this.isLoading.set(false);this.resetStep.set(0);this.form.controls.usernameOrEmail.setValue(this.resetIdentifier());this.successMessage.set('Mot de passe modifié. Vous pouvez maintenant vous connecter.');},error:e=>{this.errorMessage.set(e?.error?.message??'Code invalide ou expiré.');this.isLoading.set(false);}});}
  submit():void{this.errorMessage.set(null);this.form.markAllAsTouched();if(this.form.invalid||this.isLoading())return;this.isLoading.set(true);this.authService.login(this.form.getRawValue()).subscribe({next:r=>{this.isLoading.set(false);void this.router.navigateByUrl(homeUrlForUser(r.user));},error:(e:{status?:number})=>{this.isLoading.set(false);this.errorMessage.set(e.status===403?'Ce compte ne peut pas se connecter. Contactez les ressources humaines.':'Identifiant, adresse email ou mot de passe incorrect.');}});}
  protected hasFieldError(fieldName:'usernameOrEmail'|'password'):boolean{const c=this.form.controls[fieldName];return c.invalid&&(c.dirty||c.touched);}
  protected isSubmitDisabled():boolean{return this.form.invalid||this.isLoading();}
}
