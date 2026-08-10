import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

interface HistoryRow { id:number;demandeId:number;dateAction:string;action:string;commentaire:string|null;ancienStatut:string|null;nouveauStatut:string|null;employeId:number;employeNomComplet:string;typeDemande:string;typeConge:string;dateDebut:string;dateFin:string;heureDebut:string|null;heureFin:string|null;statutActuel:string; }

@Component({selector:'app-leave-history',imports:[FormsModule,DatePipe],templateUrl:'./leave-history.component.html',styleUrls:['../shared/resource-page.scss','./leave-history.component.scss']})
export class LeaveHistoryComponent implements OnInit {
  private readonly http=inject(HttpClient);private readonly url=`${environment.apiUrl}/conge-demande-historiques`;
  protected readonly rows=signal<HistoryRow[]>([]);protected readonly loading=signal(false);protected readonly error=signal<string|null>(null);protected readonly search=signal('');protected readonly editing=signal<HistoryRow|null>(null);protected readonly deleting=signal<HistoryRow|null>(null);
  protected readonly filtered=computed(()=>{const q=this.search().trim().toLowerCase();return this.rows().filter(r=>!q||[r.employeNomComplet,r.action,r.typeConge,r.statutActuel,String(r.demandeId)].some(v=>v.toLowerCase().includes(q)));});
  ngOnInit(){this.load();}
  protected load(){this.loading.set(true);this.http.get<HistoryRow[]>(this.url).subscribe({next:r=>{this.rows.set(r);this.loading.set(false);},error:()=>{this.error.set("L'historique n'a pas pu être chargé.");this.loading.set(false);}});}
  protected save(){const r=this.editing();if(!r)return;this.http.put<HistoryRow>(`${this.url}/${r.id}`,{action:r.action,commentaire:r.commentaire,ancienStatut:r.ancienStatut,nouveauStatut:r.nouveauStatut}).subscribe({next:()=>{this.editing.set(null);this.load();},error:()=>this.error.set("La modification n'a pas pu être enregistrée.")});}
  protected remove(){const r=this.deleting();if(!r)return;this.http.delete(`${this.url}/${r.id}`).subscribe({next:()=>{this.deleting.set(null);this.load();},error:()=>this.error.set("La ligne n'a pas pu être supprimée.")});}
  protected clone(r:HistoryRow):HistoryRow{return {...r};} protected nature(v:string){return v==='AUTORISATION_ABSENCE'?"Autorisation d'absence":'Congé';}
}
