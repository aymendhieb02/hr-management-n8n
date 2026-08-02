import { Component, EventEmitter, input, Output } from '@angular/core';
import { Position } from '../../models/position.model';

@Component({
  selector: 'app-delete-position-dialog',
  templateUrl: './delete-position-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DeletePositionDialogComponent {
  readonly position = input.required<Position>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly confirmDelete = new EventEmitter<void>();
  @Output() readonly cancel = new EventEmitter<void>();
}
