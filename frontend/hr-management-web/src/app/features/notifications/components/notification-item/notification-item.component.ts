import { Component, EventEmitter, input, Output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NotificationResponse } from '../../models/notification.model';
import { AppIconComponent } from '../../../../shared/components/app-icon/app-icon.component';

@Component({
  selector: 'app-notification-item',
  imports: [AppIconComponent, DatePipe],
  templateUrl: './notification-item.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class NotificationItemComponent {
  readonly notification = input.required<NotificationResponse>();
  readonly canManage = input(true);
  readonly canDelete = input(false);
  @Output() readonly markRead = new EventEmitter<NotificationResponse>();
  @Output() readonly delete = new EventEmitter<NotificationResponse>();
}
