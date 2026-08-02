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
  protected readonly isSickLeave = computed(() => this.request().leaveType.name.toLowerCase().includes('sick'));
  protected readonly canUploadMedicalDocument = computed(() => {
    const currentUser = this.authService.getCurrentUser();
    return Boolean(currentUser && currentUser.id === this.request().requester.id && this.isSickLeave());
  });
}
