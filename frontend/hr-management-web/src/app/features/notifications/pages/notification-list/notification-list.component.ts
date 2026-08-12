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
  protected readonly page = signal(1);
  protected readonly pageSize = signal(10);
  protected readonly deleting = signal<NotificationResponse | null>(null);
  protected readonly filteredNotifications = computed(() =>
    this.filter() === 'unread'
      ? this.notificationService.notifications().filter((item) => !item.read)
      : this.notificationService.notifications()
  );
  protected readonly totalPages = computed(() => Math.max(1, Math.ceil(this.filteredNotifications().length / this.pageSize())));
  protected readonly currentPage = computed(() => Math.min(this.page(), this.totalPages()));
  protected readonly pagedNotifications = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize();
    return this.filteredNotifications().slice(start, start + this.pageSize());
  });
  protected readonly pageStart = computed(() => this.filteredNotifications().length ? (this.currentPage() - 1) * this.pageSize() + 1 : 0);
  protected readonly pageEnd = computed(() => Math.min(this.currentPage() * this.pageSize(), this.filteredNotifications().length));

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

  protected changeFilter(value: 'all' | 'unread'): void { this.filter.set(value); this.page.set(1); }
  protected changePageSize(value: number | string): void { this.pageSize.set(Number(value)); this.page.set(1); }
  protected goToPage(value: number): void { this.page.set(Math.min(Math.max(value, 1), this.totalPages())); }
  protected visiblePages(): number[] {
    const total = this.totalPages(), current = this.currentPage();
    const start = Math.max(1, Math.min(current - 2, total - 4));
    return Array.from({ length: Math.min(5, total) }, (_, index) => start + index);
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
