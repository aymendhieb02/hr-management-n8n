import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { safeApiMessage } from '../../../shared/api-error.util';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';
import { DeleteMedicalDocumentDialogComponent } from '../../components/delete-medical-document-dialog/delete-medical-document-dialog.component';
import { MedicalDocumentMetadataResponse } from '../../models/medical-document.model';
import { MedicalDocumentService } from '../../services/medical-document.service';

@Component({selector:'app-medical-document-admin',imports:[DeleteMedicalDocumentDialogComponent,FormsModule,PaginatedTableDirective],templateUrl:'./medical-document-admin.component.html',styleUrl:'../../../shared/resource-page.scss'})
export class MedicalDocumentAdminComponent implements OnInit {
 private readonly service=inject(MedicalDocumentService);
 protected readonly documents=signal<MedicalDocumentMetadataResponse[]>([]);protected readonly search=signal('');protected readonly deletingId=signal<number|null>(null);protected readonly loading=signal(false);protected readonly error=signal<string|null>(null);
 protected readonly filtered=computed(()=>{const q=this.search().trim().toLowerCase();return q?this.documents().filter(d=>d.originalFilename.toLowerCase().includes(q)||String(d.leaveRequestId).includes(q)||String(d.id).includes(q)):this.documents();});
 ngOnInit(){this.load();}protected load(){this.loading.set(true);this.error.set(null);this.service.findAll().subscribe({next:r=>{this.documents.set(r);this.loading.set(false);},error:e=>{this.error.set(safeApiMessage(e,'Impossible de charger les certificats médicaux.'));this.loading.set(false);}});}
 protected download(document:MedicalDocumentMetadataResponse){this.service.download(document.id).subscribe({next:r=>this.service.triggerDownload(r,document.originalFilename),error:e=>this.error.set(safeApiMessage(e,'Impossible de télécharger le certificat médical.'))});}
 protected view(document:MedicalDocumentMetadataResponse){this.service.download(document.id).subscribe({next:r=>this.service.openDocument(r),error:e=>this.error.set(safeApiMessage(e,"Impossible d'ouvrir le certificat médical."))});}
 protected deleteDocument(){const id=this.deletingId();if(!id)return;this.loading.set(true);this.service.delete(id).subscribe({next:()=>{this.deletingId.set(null);this.load();},error:e=>{this.error.set(safeApiMessage(e,'Impossible de supprimer le certificat médical.'));this.loading.set(false);}});}
 protected formatSize(bytes:number){return bytes<1024?`${bytes} octets`:bytes<1024*1024?`${(bytes/1024).toFixed(1)} Ko`:`${(bytes/1024/1024).toFixed(1)} Mo`;}
}
