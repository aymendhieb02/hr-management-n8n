import { Component, computed, effect, EventEmitter, inject, input, Output, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { MedicalDocumentUploadComponent } from '../../../medical-documents/components/medical-document-upload/medical-document-upload.component';
import { LeaveRequestResponse } from '../../models/leave-request.model';
import { MedicalDocumentMetadataResponse, MedicalDocumentResponse } from '../../../medical-documents/models/medical-document.model';
import { MedicalDocumentService } from '../../../medical-documents/services/medical-document.service';
import { safeApiMessage } from '../../../shared/api-error.util';

@Component({
  selector: 'app-leave-details',
  imports: [MedicalDocumentUploadComponent],
  templateUrl: './leave-details.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveDetailsComponent {
  private readonly authService = inject(AuthService);
  private readonly medicalDocumentService = inject(MedicalDocumentService);
  readonly request = input.required<LeaveRequestResponse>();
  @Output() readonly close = new EventEmitter<void>();
  @Output() readonly certificateAdded = new EventEmitter<number>();
  protected readonly certificate = signal<MedicalDocumentMetadataResponse | null>(null);
  protected readonly certificateLoading = signal(false);
  protected readonly certificateError = signal<string | null>(null);
  protected readonly isSickLeave = computed(() => {
    const reason = this.normalize(this.request().reason ?? '');
    return this.request().nature === 'CONGE' && (reason.includes('maladie') || reason.includes('medical'));
  });

  constructor() {
    effect(() => {
      const request = this.request();
      if (!this.isSickLeave()) {
        this.certificate.set(null);
        return;
      }
      this.loadCertificate(request.id);
    });
  }

  protected certificateUploaded(document: MedicalDocumentResponse): void {
    this.certificate.set(document);
    this.certificateError.set(null);
    this.certificateAdded.emit(document.leaveRequestId);
  }

  protected viewCertificate(): void {
    const certificate = this.certificate();
    if (!certificate) return;
    this.medicalDocumentService.download(certificate.id).subscribe({
      next: (response) => this.medicalDocumentService.openDocument(response),
      error: (error) => this.certificateError.set(safeApiMessage(error, "Impossible d'ouvrir le certificat médical."))
    });
  }

  protected downloadCertificate(): void {
    const certificate = this.certificate();
    if (!certificate) return;
    this.medicalDocumentService.download(certificate.id).subscribe({
      next: (response) => this.medicalDocumentService.triggerDownload(response, certificate.originalFilename),
      error: (error) => this.certificateError.set(safeApiMessage(error, 'Impossible de télécharger le certificat médical.'))
    });
  }

  private loadCertificate(requestId: number): void {
    this.certificateLoading.set(true);
    this.certificateError.set(null);
    this.medicalDocumentService.findByLeaveRequest(requestId).subscribe({
      next: (certificate) => { this.certificate.set(certificate); this.certificateLoading.set(false); },
      error: () => {
        this.certificate.set(null);
        this.certificateError.set(this.canUploadMedicalDocument() ? null : 'Impossible de vérifier la présence du certificat médical.');
        this.certificateLoading.set(false);
      }
    });
  }
  protected readonly canUploadMedicalDocument = computed(() => {
    const currentUser = this.authService.getCurrentUser();
    return Boolean(currentUser
      && currentUser.id === this.request().requester.id
      && this.isSickLeave()
      && this.request().status !== 'REJECTED'
      && this.request().status !== 'CANCELLED');
  });

  protected statusLabel(): string {
    return { PENDING: 'En attente', APPROVED: 'Approuvee', REJECTED: 'Refusee', CANCELLED: 'Annulee' }[this.request().status];
  }

  private normalize(value: string): string {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }
}
