import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LeaveType, LeaveTypeRequest } from '../models/leave-type.model';

interface CongeTypeApiResponse {
  id: number;
  nom: string;
  description: string | null;
  joursMaximum: number | null;
  certificatObligatoire: boolean;
  remunere: boolean;
  actif: boolean;
  dateCreation: string;
  dateModification: string | null;
}

interface CongeTypeApiRequest {
  nom: string;
  description: string | null;
  joursMaximum: number | null;
  certificatObligatoire: boolean;
  remunere: boolean;
  actif: boolean;
}

@Injectable({ providedIn: 'root' })
export class LeaveTypeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/conge-types`;

  findAll(): Observable<LeaveType[]> {
    return this.http.get<CongeTypeApiResponse[]>(this.baseUrl).pipe(map((items) => items.map(toLeaveType)));
  }

  findActive(): Observable<LeaveType[]> {
    return this.http.get<CongeTypeApiResponse[]>(`${this.baseUrl}/actifs`).pipe(map((items) => items.map(toLeaveType)));
  }

  findById(id: number): Observable<LeaveType> {
    return this.http.get<CongeTypeApiResponse>(`${this.baseUrl}/${id}`).pipe(map(toLeaveType));
  }

  create(request: LeaveTypeRequest): Observable<LeaveType> {
    return this.http.post<CongeTypeApiResponse>(this.baseUrl, toCongeTypeRequest(request)).pipe(map(toLeaveType));
  }

  update(id: number, request: LeaveTypeRequest): Observable<LeaveType> {
    return this.http.put<CongeTypeApiResponse>(`${this.baseUrl}/${id}`, toCongeTypeRequest(request)).pipe(map(toLeaveType));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

function toLeaveType(item: CongeTypeApiResponse): LeaveType {
  return {
    id: item.id,
    name: item.nom,
    description: item.description,
    maxDays: item.joursMaximum,
    requiresMedicalCertificate: item.certificatObligatoire,
    active: item.actif,
    createdAt: item.dateCreation,
    updatedAt: item.dateModification
  };
}

function toCongeTypeRequest(request: LeaveTypeRequest): CongeTypeApiRequest {
  return {
    nom: request.name,
    description: request.description,
    joursMaximum: request.maxDays,
    certificatObligatoire: request.requiresMedicalCertificate,
    remunere: true,
    actif: request.active
  };
}
