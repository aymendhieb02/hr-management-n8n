import { ComponentFixture, TestBed } from '@angular/core/testing';
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
    service = {
      findByUser: vi.fn(() => of(items)),
      markAsRead: vi.fn(() => of({ ...unread, read: true, readAt: '2026-07-13T00:00:00' })),
      markAllAsRead: vi.fn(() => of(items.map((item) => ({ ...item, read: true })))),
      delete: vi.fn(() => of(void 0))
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
    expect(service.findByUser).toHaveBeenCalledWith(7);
    expect(text()).toContain('1 unread');
    expect(text()).toContain('Unread');
  });

  it('filters unread and marks notifications read', () => {
    setup();
    (fixture.componentInstance as any).filter.set('unread');
    fixture.detectChanges();
    expect(text()).toContain('Message 1');
    expect(text()).not.toContain('Message 2');
    button('Mark as read').click();
    expect(service.markAsRead).toHaveBeenCalledWith(1);
    (fixture.componentInstance as any).markAllAsRead();
    expect(service.markAllAsRead).toHaveBeenCalledWith(7);
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
    expect(text()).toContain('Notification could not be deleted.');
  });

  it('renders empty state', () => {
    setup([]);
    expect(text()).toContain('No notifications found.');
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
