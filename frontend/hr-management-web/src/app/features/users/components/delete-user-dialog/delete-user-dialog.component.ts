import { Component, EventEmitter, input, Output } from '@angular/core';
import { UserResponse } from '../../models/user.model';

@Component({
  selector: 'app-delete-user-dialog',
  templateUrl: './delete-user-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DeleteUserDialogComponent {
  readonly user = input.required<UserResponse>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly confirmDelete = new EventEmitter<void>();
  @Output() readonly cancel = new EventEmitter<void>();
}
