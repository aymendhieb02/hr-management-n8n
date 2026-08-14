import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
interface VariableSysteme{id:number;nom:string;libelle:string;description:string|null;type:string;valeur:string;secretConfigure?:boolean;}
@Component({selector:'app-system-configuration',standalone:true,imports:[FormsModule],templateUrl:'./system-configuration.component.html',styleUrl:'./system-configuration.component.scss'})
export class SystemConfigurationComponent implements OnInit{
 private readonly http=inject(HttpClient);private readonly url=`${environment.apiUrl}/variables-systeme`;
 protected readonly variables=signal<VariableSysteme[]>([]);protected readonly loading=signal(true);protected readonly saving=signal<number|null>(null);protected readonly message=signal<string|null>(null);protected readonly pending=signal<VariableSysteme|null>(null);
 protected readonly soldeNegatifActif=computed(()=>this.variables().find(v=>v.nom==='solde_negatif')?.valeur.toUpperCase()==='OUI');
 ngOnInit(){this.charger();}
 protected charger(){this.loading.set(true);this.http.get<VariableSysteme[]>(this.url).subscribe({next:v=>{this.variables.set(v);this.loading.set(false);},error:()=>{this.message.set('Impossible de charger les paramètres système.');this.loading.set(false);}});}
 protected demanderConfirmation(v:VariableSysteme){this.pending.set({...v});}
 protected confirmer(){const v=this.pending();if(!v)return;this.pending.set(null);this.saving.set(v.id);this.http.put<VariableSysteme>(`${this.url}/${v.id}`,{valeur:v.valeur}).subscribe({next:()=>{this.saving.set(null);this.message.set('Paramètre enregistré avec succès.');this.charger();},error:e=>{this.saving.set(null);this.message.set(e?.error?.message??"Impossible d'enregistrer ce paramètre.");this.charger();}});}
 protected estBooleen(v:VariableSysteme){return v.type.toUpperCase()==='BOOLEEN';}protected estLimiteNegative(v:VariableSysteme){return v.nom==='solde_negatif_max';}
 protected estSecret(v:VariableSysteme){return v.type.toUpperCase()==='SECRET';} protected estEmail(v:VariableSysteme){return v.type.toUpperCase()==='EMAIL';}
 protected changerBooleen(variable:VariableSysteme,valeur:string){this.variables.update(items=>items.map(v=>v.id===variable.id?{...v,valeur}:v));}
}
