import { Component, EventEmitter, input, Output } from '@angular/core';

@Component({
  selector: 'app-delete-medical-document-dialog',
  templateUrl: './delete-medical-document-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DeleteMedicalDocumentDialogComponent {
  readonly documentId = input.required<number>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly confirmDelete = new EventEmitter<void>();
  @Output() readonly cancel = new EventEmitter<void>();
}
