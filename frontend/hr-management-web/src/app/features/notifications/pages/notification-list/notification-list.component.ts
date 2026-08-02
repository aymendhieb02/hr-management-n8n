import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { safeApiMessage } from '../../../shared/api-error.util';
import { DeleteNotificationDialogComponent } from '../../components/delete-notification-dialog/delete-notification-dialog.component';
import { NotificationItemComponent } from '../../components/notification-item/notification-item.component';
import { NotificationResponse } from '../../models/notification.model';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification-list',
  imports: [DeleteNotificationDialogComponent, FormsModule, NotificationItemComponent],
  templateUrl: './notification-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class NotificationListComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly authService = inject(AuthService);

  protected readonly notifications = signal<NotificationResponse[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  protected readonly filter = signal<'all' | 'unread'>('all');
  protected readonly deleting = signal<NotificationResponse | null>(null);
  protected readonly unreadCount = computed(() => this.notifications().filter((item) => !item.read).length);
  protected readonly filteredNotifications = computed(() =>
    this.filter() === 'unread' ? this.notifications().filter((item) => !item.read) : this.notifications()
  );

  ngOnInit(): void {
    this.loadNotifications();
  }

  protected loadNotifications(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;
    this.isLoading.set(true);
    this.error.set(null);
    this.notificationService.findByUser(user.id).subscribe({
      next: (notifications) => {
        this.notifications.set([...notifications].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Notifications could not be loaded.');
        this.isLoading.set(false);
      }
    });
  }

  protected markAsRead(notification: NotificationResponse): void {
    this.notificationService.markAsRead(notification.id).subscribe({
      next: (updated) => this.notifications.update((items) => items.map((item) => item.id === updated.id ? updated : item)),
      error: (error) => this.error.set(safeApiMessage(error, 'Notification could not be updated.'))
    });
  }

  protected markAllAsRead(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;
    this.notificationService.markAllAsRead(user.id).subscribe({
      next: (updated) => this.notifications.set(updated),
      error: (error) => this.error.set(safeApiMessage(error, 'Notifications could not be updated.'))
    });
  }

  protected deleteNotification(): void {
    const notification = this.deleting();
    if (!notification) return;
    this.isSaving.set(true);
    this.notificationService.delete(notification.id).subscribe({
      next: () => {
        this.notifications.update((items) => items.filter((item) => item.id !== notification.id));
        this.deleting.set(null);
        this.isSaving.set(false);
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, 'Notification could not be deleted.'));
        this.isSaving.set(false);
      }
    });
  }
}
