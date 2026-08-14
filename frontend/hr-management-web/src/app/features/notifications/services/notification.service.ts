import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { catchError, finalize, forkJoin, map, Observable, of, shareReplay, tap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationResponse } from '../models/notification.model';

interface NotificationApiResponse { id: number; employeId: number; employeNom: string; typeLibelle: string; titre: string; contenu: string; lu: boolean; dateCreation: string; dateLecture: string | null; priorite: string | null; }

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly baseUrl = `${environment.apiUrl}/notifications-v2`;
  private readonly notificationsSignal = signal<NotificationResponse[]>([]);
  private readonly loadingSignal = signal(false);
  private loadedUserId: number | null = null;
  private activeLoad$: Observable<NotificationResponse[]> | null = null;
  private socket: WebSocket | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;

  readonly notifications = this.notificationsSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly unreadCount = computed(() => this.notificationsSignal().filter((item) => !item.read).length);
  readonly manageableUnreadCount = computed(() => {
    const userId = this.authService.getCurrentUser()?.id;
    return this.notificationsSignal().filter((item) => !item.read && item.recipient.id === userId).length;
  });
  readonly canMarkAll = computed(() => ['DG','DT'].includes(this.authService.getCurrentUser()?.role ?? ''));

  canManage(notification: NotificationResponse): boolean {
    return notification.recipient.id === this.authService.getCurrentUser()?.id;
  }

  canDelete(notification: NotificationResponse): boolean {
    return ['DG','DT'].includes(this.authService.getCurrentUser()?.role ?? '') && this.canManage(notification);
  }

  findAll(): Observable<NotificationResponse[]> { return of([]); }
  findById(id: number): Observable<NotificationResponse> { return this.http.get<NotificationApiResponse>(`${this.baseUrl}/${id}`).pipe(map(toNotification)); }
  findByUser(_userId?: number): Observable<NotificationResponse[]> { return this.http.get<NotificationApiResponse[]>(`${this.baseUrl}/me`).pipe(map((items) => items.map(toNotification))); }
  findUnread(_userId?: number): Observable<NotificationResponse[]> { return this.http.get<NotificationApiResponse[]>(`${this.baseUrl}/me/non-lues`).pipe(map((items) => items.map(toNotification))); }

  loadCurrentUser(force = false): Observable<NotificationResponse[]> {
    this.connectRealtime();
    const user = typeof this.authService.getCurrentUser === 'function' ? this.authService.getCurrentUser() : null;
    if (!user) return of([]);
    if (!force && this.loadedUserId === user.id) return of(this.notificationsSignal());
    if (!force && this.activeLoad$) return this.activeLoad$;

    this.loadingSignal.set(true);
    const request$ = this.findByUser(user.id).pipe(
      map((items) => [...items].sort((a, b) => b.createdAt.localeCompare(a.createdAt))),
      tap((items) => { this.loadedUserId = user.id; this.notificationsSignal.set(items); }),
      catchError((error) => throwError(() => error)),
      finalize(() => { this.loadingSignal.set(false); this.activeLoad$ = null; }),
      shareReplay({ bufferSize: 1, refCount: false })
    );
    this.activeLoad$ = request$;
    return request$;
  }

  private connectRealtime(): void {
    if (!isPlatformBrowser(this.platformId) || typeof WebSocket === 'undefined' || this.socket) return;
    const socketUrl = environment.apiUrl.replace(/^http/, 'ws').replace(/\/api\/?$/, '/ws-notifications');
    this.socket = new WebSocket(socketUrl);
    this.socket.onmessage = () => this.loadCurrentUser(true).subscribe({ error: () => undefined });
    this.socket.onclose = () => {
      this.socket = null;
      if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
      this.reconnectTimer = setTimeout(() => this.connectRealtime(), 3000);
    };
    this.socket.onerror = () => this.socket?.close();
  }

  markAsRead(id: number): Observable<NotificationResponse> {
    return this.http.patch<NotificationApiResponse>(`${this.baseUrl}/${id}/lue`, {}).pipe(
      map(toNotification),
      tap((updated) => this.notificationsSignal.update((items) => items.map((item) => item.id === id ? updated : item)))
    );
  }

  markAllAsRead(): Observable<NotificationResponse[]> {
    const userId = this.authService.getCurrentUser()?.id;
    const unread = this.notificationsSignal().filter((item) => !item.read && item.recipient.id === userId);
    if (!unread.length) return of(this.notificationsSignal());
    return forkJoin(unread.map((item) => this.markAsRead(item.id))).pipe(map(() => this.notificationsSignal()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this.notificationsSignal.update((items) => items.filter((item) => item.id !== id)))
    );
  }
}
function toNotification(response: NotificationApiResponse): NotificationResponse {
  const [firstName, ...rest] = response.employeNom.trim().split(/\s+/);
  return {
    id: response.id,
    recipient: { id: response.employeId, firstName, lastName: rest.join(' '), email: '' },
    title: response.titre,
    message: response.contenu,
    read: response.lu,
    createdAt: response.dateCreation,
    readAt: response.dateLecture,
    type: response.typeLibelle,
    priority: response.priorite
  };
}
