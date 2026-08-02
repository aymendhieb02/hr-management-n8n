import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { NotificationResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notifications`;

  findAll(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(this.baseUrl);
  }

  findById(id: number): Observable<NotificationResponse> {
    return this.http.get<NotificationResponse>(`${this.baseUrl}/${id}`);
  }

  findByUser(userId: number): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.baseUrl}/user/${userId}`);
  }

  findUnread(userId: number): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.baseUrl}/user/${userId}/unread`);
  }

  markAsRead(id: number): Observable<NotificationResponse> {
    return this.http.patch<NotificationResponse>(`${this.baseUrl}/${id}/read`, {});
  }

  markAllAsRead(userId: number): Observable<NotificationResponse[]> {
    return this.http.patch<NotificationResponse[]>(`${this.baseUrl}/user/${userId}/read-all`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
