import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { PaginatedTableDirective } from '../shared/paginated-table.directive';
import { safeApiMessage } from '../shared/api-error.util';
import { TypeContrat, TypeContratPayload, TypeContractService } from './type-contract.service';

@Component({selector:'app-type-contract-list',imports:[FormsModule,PaginatedTableDirective],templateUrl:'./type-contract-list.component.html',styleUrls:['../shared/resource-page.scss','./type-contract-list.component.scss']})
export class TypeContractListComponent implements OnInit {
 private readonly service=inject(TypeContractService);
 protected readonly items=signal<TypeContrat[]>([]);protected readonly loading=signal(false);protected readonly saving=signal(false);protected readonly error=signal<string|null>(null);protected readonly search=signal('');protected readonly editing=signal<TypeContrat|null>(null);protected readonly deleting=signal<TypeContrat|null>(null);protected readonly formOpen=signal(false);protected draft:TypeContratPayload={libelle:'',description:null,actif:true};
 protected readonly filtered=computed(()=>{const q=this.search().trim().toLowerCase();return q?this.items().filter(x=>`${x.libelle} ${x.description??''}`.toLowerCase().includes(q)):this.items();});
 ngOnInit(){this.load();}protected load(){this.loading.set(true);this.error.set(null);this.service.findAll().subscribe({next:r=>{this.items.set(r);this.loading.set(false);},error:e=>{this.error.set(safeApiMessage(e,'Impossible de charger les types de contrat.'));this.loading.set(false);}});}
 protected openCreate(){this.editing.set(null);this.draft={libelle:'',description:null,actif:true};this.formOpen.set(true);}protected openEdit(item:TypeContrat){this.editing.set(item);this.draft={libelle:item.libelle,description:item.description,actif:item.actif};this.formOpen.set(true);}
 protected save(){if(!this.draft.libelle.trim()){this.error.set('Le libellé du type de contrat est obligatoire.');return;}const current=this.editing();const payload={...this.draft,libelle:this.draft.libelle.trim(),description:this.draft.description?.trim()||null};this.saving.set(true);(current?this.service.update(current.id,payload):this.service.create(payload)).subscribe({next:()=>{this.saving.set(false);this.formOpen.set(false);this.load();},error:(e:HttpErrorResponse)=>{this.error.set(safeApiMessage(e,"Impossible d'enregistrer le type de contrat."));this.saving.set(false);}});}
 protected remove(){const item=this.deleting();if(!item)return;this.saving.set(true);this.service.delete(item.id).subscribe({next:()=>{this.saving.set(false);this.deleting.set(null);this.load();},error:e=>{this.error.set(safeApiMessage(e,'Impossible de supprimer ce type de contrat. Il est peut-être utilisé par un employé.'));this.saving.set(false);}});}
}
