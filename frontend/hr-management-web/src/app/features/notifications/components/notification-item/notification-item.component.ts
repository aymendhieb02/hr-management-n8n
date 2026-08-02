import { Component, EventEmitter, input, Output } from '@angular/core';
import { NotificationResponse } from '../../models/notification.model';

@Component({
  selector: 'app-notification-item',
  templateUrl: './notification-item.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class NotificationItemComponent {
  readonly notification = input.required<NotificationResponse>();
  @Output() readonly markRead = new EventEmitter<NotificationResponse>();
  @Output() readonly delete = new EventEmitter<NotificationResponse>();
}
