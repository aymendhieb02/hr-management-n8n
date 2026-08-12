import { DatePipe } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';

interface HistoryRow { id:number;demandeId:number;dateAction:string;action:string;commentaire:string|null;ancienStatut:string|null;nouveauStatut:string|null;employeId:number;employeNomComplet:string;typeDemande:string;typeConge:string;dateDebut:string;dateFin:string;heureDebut:string|null;heureFin:string|null;statutActuel:string; }
interface HistoryEmployee { id:number; nomComplet:string; }
interface HistoryPage { contenu:HistoryRow[];totalElements:number;totalPages:number;page:number;taille:number; }

@Component({selector:'app-leave-history',imports:[FormsModule,DatePipe],templateUrl:'./leave-history.component.html',styleUrls:['../shared/resource-page.scss','./leave-history.component.scss'],host:{'[class.read-only]':'!canManage'}})
export class LeaveHistoryComponent implements OnInit, OnDestroy {
 private readonly http=inject(HttpClient);private readonly auth=inject(AuthService);private readonly url=`${environment.apiUrl}/conge-demande-historiques`;private searchTimer?:ReturnType<typeof setTimeout>;
 protected readonly canManage=this.auth.hasAnyRole('MANAGER','ADMIN');
 protected readonly rows=signal<HistoryRow[]>([]);protected readonly employees=signal<HistoryEmployee[]>([]);protected readonly loading=signal(false);protected readonly error=signal<string|null>(null);protected readonly search=signal('');protected readonly employeeFilter=signal<number|null>(null);protected readonly editing=signal<HistoryRow|null>(null);protected readonly deleting=signal<HistoryRow|null>(null);protected readonly page=signal(1);protected readonly pageSize=signal(8);protected readonly totalElements=signal(0);protected readonly totalPages=signal(1);
 protected readonly pageStart=()=>this.totalElements()?(this.page()-1)*this.pageSize()+1:0;protected readonly pageEnd=()=>Math.min(this.page()*this.pageSize(),this.totalElements());
 ngOnInit(){if(this.canManage)this.loadEmployees();this.load();}ngOnDestroy(){if(this.searchTimer)clearTimeout(this.searchTimer);}
 protected load(){this.loading.set(true);this.error.set(null);let params=new HttpParams().set('page',this.page()-1).set('taille',this.pageSize());const q=this.search().trim();if(q)params=params.set('recherche',q);const employee=this.employeeFilter();if(employee!==null)params=params.set('employeId',employee);this.http.get<HistoryPage>(`${this.url}/page`,{params}).subscribe({next:r=>{this.rows.set(r.contenu);this.totalElements.set(r.totalElements);this.totalPages.set(Math.max(1,r.totalPages));this.page.set(r.page+1);this.loading.set(false);},error:()=>{this.error.set("L'historique n'a pas pu être chargé.");this.loading.set(false);}});}
 private loadEmployees(){this.http.get<HistoryEmployee[]>(`${this.url}/employes`).subscribe({next:r=>this.employees.set(r),error:()=>this.employees.set([])});}
 protected searchChanged(value:string){this.search.set(value);this.page.set(1);if(this.searchTimer)clearTimeout(this.searchTimer);this.searchTimer=setTimeout(()=>this.load(),300);}
 protected employeeChanged(value:number|null){this.employeeFilter.set(value);this.page.set(1);this.load();}
 protected changePageSize(v:number|string){this.pageSize.set(Number(v));this.page.set(1);this.load();}protected goToPage(v:number){const target=Math.min(Math.max(v,1),this.totalPages());if(target!==this.page()){this.page.set(target);this.load();}}protected visiblePages(){const total=this.totalPages(),current=this.page(),start=Math.max(1,Math.min(current-2,total-4));return Array.from({length:Math.min(5,total)},(_,i)=>start+i);}
 protected save(){const r=this.editing();if(!r)return;this.http.put<HistoryRow>(`${this.url}/${r.id}`,{action:r.action,commentaire:r.commentaire,ancienStatut:r.ancienStatut,nouveauStatut:r.nouveauStatut}).subscribe({next:()=>{this.editing.set(null);this.load();},error:()=>this.error.set("La modification n'a pas pu être enregistrée.")});}protected remove(){const r=this.deleting();if(!r)return;this.http.delete(`${this.url}/${r.id}`).subscribe({next:()=>{this.deleting.set(null);if(this.rows().length===1&&this.page()>1)this.page.update(p=>p-1);this.load();this.loadEmployees();},error:()=>this.error.set("La ligne n'a pas pu être supprimée.")});}protected clone(r:HistoryRow){return {...r};}protected nature(v:string){return v==='AUTORISATION_ABSENCE'?"Autorisation d'absence":'Congé';}
}
