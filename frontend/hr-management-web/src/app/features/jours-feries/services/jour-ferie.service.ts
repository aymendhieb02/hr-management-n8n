import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { JourFerieResponse, JourFerieRequest } from '../models/jour-ferie.model';

@Injectable({ providedIn: 'root' })
export class JourFerieService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/jours-feries`;

  getAll(): Observable<JourFerieResponse[]> {
    return this.http.get<JourFerieResponse[]>(this.baseUrl);
  }

  getActive(): Observable<JourFerieResponse[]> {
    return this.http.get<JourFerieResponse[]>(`${this.baseUrl}/actifs`);
  }

  getById(id: number): Observable<JourFerieResponse> {
    return this.http.get<JourFerieResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: JourFerieRequest): Observable<JourFerieResponse> {
    return this.http.post<JourFerieResponse>(this.baseUrl, request);
  }

  update(id: number, request: JourFerieRequest): Observable<JourFerieResponse> {
    return this.http.put<JourFerieResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
