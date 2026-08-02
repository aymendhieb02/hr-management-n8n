import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { safeApiMessage } from '../../../shared/api-error.util';
import { DeleteMedicalDocumentDialogComponent } from '../../components/delete-medical-document-dialog/delete-medical-document-dialog.component';
import { MedicalDocumentMetadataComponent } from '../../components/medical-document-metadata/medical-document-metadata.component';
import { MedicalDocumentMetadataResponse } from '../../models/medical-document.model';
import { MedicalDocumentService } from '../../services/medical-document.service';

@Component({
  selector: 'app-medical-document-admin',
  imports: [DeleteMedicalDocumentDialogComponent, FormsModule, MedicalDocumentMetadataComponent],
  templateUrl: './medical-document-admin.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class MedicalDocumentAdminComponent {
  private readonly service = inject(MedicalDocumentService);
  protected readonly documentId = signal<number | null>(null);
  protected readonly metadata = signal<MedicalDocumentMetadataResponse | null>(null);
  protected readonly deletingId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected lookup(): void {
    const id = this.documentId();
    if (!id) return;
    this.loading.set(true);
    this.error.set(null);
    this.service.findById(id).subscribe({
      next: (metadata) => { this.metadata.set(metadata); this.loading.set(false); },
      error: (error) => { this.error.set(safeApiMessage(error, 'Medical document could not be found.')); this.loading.set(false); }
    });
  }

  protected download(id: number): void {
    this.service.download(id).subscribe({
      next: (response) => this.service.triggerDownload(response, `medical-document-${id}`),
      error: (error) => this.error.set(safeApiMessage(error, 'Medical document could not be downloaded.'))
    });
  }

  protected deleteDocument(): void {
    const id = this.deletingId();
    if (!id) return;
    this.loading.set(true);
    this.service.delete(id).subscribe({
      next: () => { this.metadata.set(null); this.deletingId.set(null); this.loading.set(false); },
      error: (error) => { this.error.set(safeApiMessage(error, 'Medical document could not be deleted.')); this.loading.set(false); }
    });
  }
}
