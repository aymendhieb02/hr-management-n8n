import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Position, PositionRequest } from '../models/position.model';

interface PosteApiResponse {
  id: number;
  intitule: string;
  description: string | null;
  niveauPoste: string | null;
  actif: boolean;
  dateCreation: string;
  dateModification: string | null;
}

interface PosteApiRequest {
  intitule: string;
  description: string | null;
  niveauPoste: string | null;
  actif: boolean;
}

@Injectable({ providedIn: 'root' })
export class PositionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/postes`;

  findAll(): Observable<Position[]> {
    return this.http.get<PosteApiResponse[]>(this.baseUrl).pipe(map((items) => items.map(toPosition)));
  }

  findById(id: number): Observable<Position> {
    return this.http.get<PosteApiResponse>(`${this.baseUrl}/${id}`).pipe(map(toPosition));
  }

  create(request: PositionRequest): Observable<Position> {
    return this.http.post<PosteApiResponse>(this.baseUrl, toPosteRequest(request)).pipe(map(toPosition));
  }

  update(id: number, request: PositionRequest): Observable<Position> {
    return this.http.put<PosteApiResponse>(`${this.baseUrl}/${id}`, toPosteRequest(request)).pipe(map(toPosition));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

function toPosition(poste: PosteApiResponse): Position {
  return {
    id: poste.id,
    title: poste.intitule,
    description: poste.description,
    level: poste.niveauPoste,
    active: poste.actif,
    createdAt: poste.dateCreation,
    updatedAt: poste.dateModification
  };
}

function toPosteRequest(request: PositionRequest): PosteApiRequest {
  return {
    intitule: request.title,
    description: request.description,
    niveauPoste: request.level ?? null,
    actif: request.active ?? true
  };
}
