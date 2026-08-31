import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { AppIconComponent } from '../../shared/components/app-icon/app-icon.component';
interface VariableSysteme{id:number;nom:string;libelle:string;description:string|null;type:string;valeur:string;secretConfigure?:boolean;}
@Component({selector:'app-system-configuration',standalone:true,imports:[AppIconComponent,FormsModule],templateUrl:'./system-configuration.component.html',styleUrls:['./system-configuration.component.scss','./system-configuration-edit.component.scss']})
export class SystemConfigurationComponent implements OnInit{
 private readonly http=inject(HttpClient);private readonly url=`${environment.apiUrl}/variables-systeme`;
 protected readonly variables=signal<VariableSysteme[]>([]);protected readonly loading=signal(true);protected readonly saving=signal<number|null>(null);protected readonly editing=signal<number|null>(null);protected readonly message=signal<string|null>(null);protected readonly pending=signal<VariableSysteme|null>(null);
 protected readonly soldeNegatifActif=computed(()=>this.variables().find(v=>v.nom==='solde_negatif')?.valeur.toUpperCase()==='OUI');
 protected readonly testingAccrual=signal(false);
 protected readonly testApplied=signal(false);
 ngOnInit(){this.charger();}
 protected charger(){this.loading.set(true);this.http.get<VariableSysteme[]>(this.url).subscribe({next:v=>{this.variables.set(v);this.loading.set(false);},error:()=>{this.message.set('Impossible de charger les paramètres système.');this.loading.set(false);}});}
 protected modifier(v:VariableSysteme){this.message.set(null);this.editing.set(v.id);}protected annuler(){this.editing.set(null);this.charger();}
 protected demanderConfirmation(v:VariableSysteme){if(this.editing()!==v.id)return;this.pending.set({...v});}
 protected confirmer(){const v=this.pending();if(!v)return;this.pending.set(null);this.saving.set(v.id);this.http.put<VariableSysteme>(`${this.url}/${v.id}`,{valeur:v.valeur}).subscribe({next:()=>{this.saving.set(null);this.editing.set(null);this.message.set('Paramètre enregistré avec succès.');this.charger();},error:e=>{this.saving.set(null);this.message.set(e?.error?.message??"Impossible d'enregistrer ce paramètre.");}});}
 protected estBooleen(v:VariableSysteme){return v.type.toUpperCase()==='BOOLEEN';}protected estLimiteNegative(v:VariableSysteme){return v.nom==='solde_negatif_max';}
 protected estSecret(v:VariableSysteme){return v.type.toUpperCase()==='SECRET';} protected estEmail(v:VariableSysteme){return v.type.toUpperCase()==='EMAIL';}
 protected testerAcquisition(){if(this.testingAccrual())return;this.testingAccrual.set(true);this.http.post<{debut:string;fin:string;montant:number;comptesCredités:number}>(`${this.url}/solde-conge-par-mois/tester`,{}).subscribe({next:r=>{this.testingAccrual.set(false);this.testApplied.set(true);this.message.set(`Test appliqué : ${r.comptesCredités} compte(s) crédité(s) de ${r.montant} jour(s) sur la période du ${r.debut} au ${r.fin}. Vous pouvez annuler ce test avec « Retourner »; les écritures de test seront retirées.`);},error:e=>{this.testingAccrual.set(false);this.message.set(e?.error?.message??'Impossible de tester l’acquisition mensuelle.');}});}
 protected retournerAcquisition(){if(this.testingAccrual())return;this.testingAccrual.set(true);this.http.post<{comptesAnnulés:number}>(`${this.url}/solde-conge-par-mois/retourner`,{}).subscribe({next:r=>{this.testingAccrual.set(false);this.testApplied.set(false);this.message.set(`${r.comptesAnnulés} acquisition(s) de test ont été annulée(s). Les soldes sont revenus à leur valeur précédente, sans historique conservé.`);this.charger();},error:e=>{this.testingAccrual.set(false);this.message.set(e?.error?.message??'Impossible de retourner le test.');}});}
 protected changerBooleen(variable:VariableSysteme,valeur:string){this.variables.update(items=>items.map(v=>v.id===variable.id?{...v,valeur}:v));}
}
