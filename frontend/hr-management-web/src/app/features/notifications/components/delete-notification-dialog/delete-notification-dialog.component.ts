import { Component, EventEmitter, input, Output } from '@angular/core';
import { NotificationResponse } from '../../models/notification.model';

@Component({
  selector: 'app-delete-notification-dialog',
  templateUrl: './delete-notification-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DeleteNotificationDialogComponent {
  readonly notification = input.required<NotificationResponse>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly confirmDelete = new EventEmitter<void>();
  @Output() readonly cancel = new EventEmitter<void>();
}
