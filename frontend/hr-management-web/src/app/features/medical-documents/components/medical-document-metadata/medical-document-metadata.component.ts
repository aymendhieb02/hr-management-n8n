import { Component, EventEmitter, input, Output } from '@angular/core';
import { MedicalDocumentMetadataResponse } from '../../models/medical-document.model';

@Component({
  selector: 'app-medical-document-metadata',
  templateUrl: './medical-document-metadata.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class MedicalDocumentMetadataComponent {
  readonly document = input.required<MedicalDocumentMetadataResponse>();
  readonly canManage = input(false);
  @Output() readonly download = new EventEmitter<number>();
  @Output() readonly delete = new EventEmitter<number>();
}
