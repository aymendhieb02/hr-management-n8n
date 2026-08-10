import { Component, computed, EventEmitter, inject, input, Output } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { MedicalDocumentUploadComponent } from '../../../medical-documents/components/medical-document-upload/medical-document-upload.component';
import { LeaveRequestResponse } from '../../models/leave-request.model';

@Component({
  selector: 'app-leave-details',
  imports: [MedicalDocumentUploadComponent],
  templateUrl: './leave-details.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveDetailsComponent {
  private readonly authService = inject(AuthService);
  readonly request = input.required<LeaveRequestResponse>();
  @Output() readonly close = new EventEmitter<void>();
  protected readonly isSickLeave = computed(() => {
    const name = this.request().leaveType.name.toLowerCase();
    return name.includes('sick') || name.includes('maladie');
  });
  protected readonly canUploadMedicalDocument = computed(() => {
    const currentUser = this.authService.getCurrentUser();
    return Boolean(currentUser && currentUser.id === this.request().requester.id && this.isSickLeave());
  });

  protected statusLabel(): string {
    return { PENDING: 'En attente', APPROVED: 'Approuvee', REJECTED: 'Refusee', CANCELLED: 'Annulee' }[this.request().status];
  }
}
