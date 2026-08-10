import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reason, ReasonRequest } from './reason.model';

@Injectable({ providedIn: 'root' })
export class ReasonService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/raisons`;
  findAll(): Observable<Reason[]> { return this.http.get<Reason[]>(this.baseUrl); }
  findAvailable(): Observable<Reason[]> { return this.http.get<Reason[]>(`${this.baseUrl}/disponibles`); }
  create(request: ReasonRequest): Observable<Reason> { return this.http.post<Reason>(this.baseUrl, request); }
  update(id: number, request: ReasonRequest): Observable<Reason> { return this.http.put<Reason>(`${this.baseUrl}/${id}`, request); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${id}`); }
}
