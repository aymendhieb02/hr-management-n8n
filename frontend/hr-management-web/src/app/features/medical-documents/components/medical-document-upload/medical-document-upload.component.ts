import { Component, EventEmitter, input, Output, signal } from '@angular/core';
import { MedicalDocumentResponse } from '../../models/medical-document.model';
import { MedicalDocumentService } from '../../services/medical-document.service';
import { inject } from '@angular/core';
import { safeApiMessage } from '../../../shared/api-error.util';

@Component({
  selector: 'app-medical-document-upload',
  templateUrl: './medical-document-upload.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class MedicalDocumentUploadComponent {
  private readonly service = inject(MedicalDocumentService);
  readonly leaveRequestId = input.required<number>();
  readonly disabled = input(false);
  @Output() readonly uploaded = new EventEmitter<MedicalDocumentResponse>();

  protected readonly selectedFile = signal<File | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly loading = signal(false);
  private readonly allowed = ['application/pdf', 'image/jpeg', 'image/png'];
  private readonly maxSize = 5 * 1024 * 1024;

  protected selectFile(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.error.set(null);
    this.success.set(null);
    if (!file) {
      this.selectedFile.set(null);
      this.error.set('Le fichier est obligatoire.');
      return;
    }
    if (file.size === 0) {
      this.selectedFile.set(null);
      this.error.set('Le fichier ne peut pas etre vide.');
      return;
    }
    if (!this.allowed.includes(file.type)) {
      this.selectedFile.set(null);
      this.error.set('Seuls les fichiers PDF, JPEG ou PNG sont autorises.');
      return;
    }
    if (file.size > this.maxSize) {
      this.selectedFile.set(null);
      this.error.set('Le fichier ne doit pas depasser 5 Mo.');
      return;
    }
    this.selectedFile.set(file);
  }

  protected upload(): void {
    const file = this.selectedFile();
    if (!file || this.disabled()) return;
    this.loading.set(true);
    this.service.upload(this.leaveRequestId(), file).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.success.set('Certificat medical televerse.');
        this.selectedFile.set(null);
        this.uploaded.emit(response);
      },
      error: (error) => {
        this.error.set(safeApiMessage(error, 'Impossible de televerser le certificat medical.'));
        this.loading.set(false);
      }
    });
  }
}
