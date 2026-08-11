import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { safeApiMessage } from '../../../shared/api-error.util';
import { DeleteNotificationDialogComponent } from '../../components/delete-notification-dialog/delete-notification-dialog.component';
import { NotificationItemComponent } from '../../components/notification-item/notification-item.component';
import { NotificationResponse } from '../../models/notification.model';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification-list',
  imports: [DeleteNotificationDialogComponent, FormsModule, NotificationItemComponent],
  templateUrl: './notification-list.component.html',
  styleUrls: ['../../../shared/resource-page.scss', './notification-list.component.scss']
})
export class NotificationListComponent implements OnInit {
  protected readonly notificationService = inject(NotificationService);

  protected readonly isSaving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  protected readonly filter = signal<'all' | 'unread'>('all');
  protected readonly deleting = signal<NotificationResponse | null>(null);
  protected readonly filteredNotifications = computed(() =>
    this.filter() === 'unread'
      ? this.notificationService.notifications().filter((item) => !item.read)
      : this.notificationService.notifications()
  );

  ngOnInit(): void {
    this.loadNotifications();
  }

  protected loadNotifications(force = false): void {
    this.error.set(null);
    this.notificationService.loadCurrentUser(force).subscribe({
      next: () => undefined,
      error: () => {
        this.error.set('Impossible de charger vos notifications. Veuillez réessayer.');
      }
    });
  }

  protected markAsRead(notification: NotificationResponse): void {
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => undefined,
      error: (error) => this.error.set(safeApiMessage(error, 'Impossible de marquer cette notification comme lue.'))
    });
  }

  protected markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => undefined,
      error: (error) => this.error.set(safeApiMessage(error, 'Impossible de marquer toutes les notifications comme lues.'))
    });
  }

  protected deleteNotification(): void {
    const notification = this.deleting();
    if (!notification) return;
    this.isSaving.set(true);
    this.notificationService.delete(notification.id).subscribe({
      next: () => {
        this.deleting.set(null);
        this.isSaving.set(false);
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, 'Impossible de supprimer cette notification.'));
        this.isSaving.set(false);
      }
    });
  }
}
