import { ComponentFixture, TestBed } from '@angular/core/testing';
import { computed, signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationResponse } from '../../models/notification.model';
import { NotificationService } from '../../services/notification.service';
import { NotificationListComponent } from './notification-list.component';

describe('NotificationListComponent', () => {
  const unread = notification(1, false);
  const read = notification(2, true);
  let fixture: ComponentFixture<NotificationListComponent>;
  let service: any;

  function setup(items: NotificationResponse[] = [read, unread]): void {
    const itemsSignal = signal(items);
    service = {
      notifications: itemsSignal.asReadonly(),
      unreadCount: computed(() => itemsSignal().filter((item) => !item.read).length),
      manageableUnreadCount: computed(() => itemsSignal().filter((item) => !item.read).length),
      canManage: vi.fn(() => true),
      canDelete: vi.fn(() => false),
      canMarkAll: signal(true).asReadonly(),
      loading: signal(false).asReadonly(),
      loadCurrentUser: vi.fn(() => of(items)),
      markAsRead: vi.fn((id: number) => {
        const updated = { ...itemsSignal().find((item) => item.id === id)!, read: true, readAt: '2026-07-13T00:00:00' };
        itemsSignal.update((current) => current.map((item) => item.id === id ? updated : item));
        return of(updated);
      }),
      markAllAsRead: vi.fn(() => {
        itemsSignal.update((current) => current.map((item) => ({ ...item, read: true })));
        return of(itemsSignal());
      }),
      delete: vi.fn((id: number) => {
        itemsSignal.update((current) => current.filter((item) => item.id !== id));
        return of(void 0);
      })
    };
    TestBed.configureTestingModule({
      imports: [NotificationListComponent],
      providers: [
        { provide: NotificationService, useValue: service },
        { provide: AuthService, useValue: { getCurrentUser: vi.fn(() => ({ id: 7 })) } }
      ]
    });
    fixture = TestBed.createComponent(NotificationListComponent);
    fixture.detectChanges();
  }

  it('loads current user notifications and shows unread badge', () => {
    setup();
    expect(service.loadCurrentUser).toHaveBeenCalled();
    expect(text()).toContain('1non lue');
    expect(text()).toContain('Non lue');
  });

  it('filters unread and marks notifications read', () => {
    setup();
    (fixture.componentInstance as any).filter.set('unread');
    fixture.detectChanges();
    expect(text()).toContain('Message 1');
    expect(text()).not.toContain('Message 2');
    button('Marquer comme lue').click();
    expect(service.markAsRead).toHaveBeenCalledWith(1);
    (fixture.componentInstance as any).markAllAsRead();
    expect(service.markAllAsRead).toHaveBeenCalled();
  });

  it('deletes notification and handles failure', () => {
    setup();
    (fixture.componentInstance as any).deleting.set(unread);
    fixture.detectChanges();
    dialogDelete().click();
    expect(service.delete).toHaveBeenCalledWith(1);
    service.delete.mockReturnValue(throwError(() => new Error('fail')));
    (fixture.componentInstance as any).deleting.set(read);
    fixture.detectChanges();
    dialogDelete().click();
    fixture.detectChanges();
    expect(text()).toContain('Impossible de supprimer cette notification.');
  });

  it('renders empty state', () => {
    setup([]);
    expect(text()).toContain('Aucune notification');
  });

  function text(): string { return fixture.nativeElement.textContent; }
  function button(label: string): HTMLButtonElement {
    return Array.from(fixture.nativeElement.querySelectorAll('button')).find((el: any) => el.textContent.trim() === label) as HTMLButtonElement;
  }
  function dialogDelete(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.dialog .danger-button') as HTMLButtonElement;
  }
  function notification(id: number, readFlag: boolean): NotificationResponse {
    return { id, recipient: { id: 7, firstName: 'A', lastName: 'B', email: 'a@test.com' }, title: `Title ${id}`, message: `Message ${id}`, read: readFlag, createdAt: `2026-07-1${id}T00:00:00`, readAt: readFlag ? '2026-07-13T00:00:00' : null };
  }
});
