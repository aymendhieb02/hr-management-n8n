import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { NotificationResponse } from '../models/notification.model';

interface NotificationApiResponse { id: number; employeId: number; employeNom: string; typeLibelle: string; titre: string; contenu: string; lu: boolean; dateCreation: string; dateLecture: string | null; priorite: string | null; }

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notifications-v2`;
  findAll(): Observable<NotificationResponse[]> { return of([]); }
  findById(id: number): Observable<NotificationResponse> { return this.http.get<NotificationApiResponse>(`${this.baseUrl}/${id}`).pipe(map(toNotification)); }
  findByUser(userId: number): Observable<NotificationResponse[]> { return this.http.get<NotificationApiResponse[]>(`${this.baseUrl}/employe/${userId}`).pipe(map((items) => items.map(toNotification))); }
  findUnread(userId: number): Observable<NotificationResponse[]> { return this.http.get<NotificationApiResponse[]>(`${this.baseUrl}/employe/${userId}/non-lues`).pipe(map((items) => items.map(toNotification))); }
  markAsRead(id: number): Observable<NotificationResponse> { return this.http.patch<NotificationApiResponse>(`${this.baseUrl}/${id}/lue`, {}).pipe(map(toNotification)); }
  markAllAsRead(userId: number): Observable<NotificationResponse[]> { return this.findUnread(userId).pipe(switchMap((items) => items.length ? forkJoin(items.map((item) => this.markAsRead(item.id))) : of([]))); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${id}`); }
}
function toNotification(response: NotificationApiResponse): NotificationResponse {
  const [lastName, ...rest] = response.employeNom.split(' ');
  return { id: response.id, recipient: { id: response.employeId, firstName: rest.join(' '), lastName, email: '' }, title: response.titre, message: response.contenu, read: response.lu, createdAt: response.dateCreation, readAt: response.dateLecture };
}
