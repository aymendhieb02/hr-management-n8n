import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LeaveBalanceRequest, LeaveBalanceResponse } from '../models/leave-balance.model';

interface CongeSoldeApiResponse {
  id: number; employeId: number; employeNom: string; employePrenom: string; congeTypeId: number; congeTypeNom: string; annee: number; droitAcquis: number; joursUtilises: number; restants: number; dateCreation: string; dateModification: string | null;
}

interface CongeSoldeApiRequest { employeId: number; congeTypeId: number; annee: number; droitAcquis: number; joursUtilises: number; restants: number; }

@Injectable({ providedIn: 'root' })
export class LeaveBalanceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/conge-soldes-v2`;

  findAll(): Observable<LeaveBalanceResponse[]> { return this.http.get<CongeSoldeApiResponse[]>(this.baseUrl).pipe(map((items) => items.map(toLeaveBalance))); }
  findByUser(userId: number): Observable<LeaveBalanceResponse[]> { return this.http.get<CongeSoldeApiResponse[]>(`${this.baseUrl}/employe/${userId}`).pipe(map((items) => items.map(toLeaveBalance))); }
  create(request: LeaveBalanceRequest): Observable<LeaveBalanceResponse> { return this.http.post<CongeSoldeApiResponse>(this.baseUrl, toApiRequest(request)).pipe(map(toLeaveBalance)); }
  update(id: number, request: LeaveBalanceRequest): Observable<LeaveBalanceResponse> { return this.http.put<CongeSoldeApiResponse>(`${this.baseUrl}/${id}`, toApiRequest(request)).pipe(map(toLeaveBalance)); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${id}`); }
}

function toLeaveBalance(response: CongeSoldeApiResponse): LeaveBalanceResponse {
  return { id: response.id, user: { id: response.employeId, firstName: response.employePrenom, lastName: response.employeNom, email: '' }, leaveType: { id: response.congeTypeId, name: response.congeTypeNom }, year: response.annee, totalDays: response.droitAcquis, usedDays: response.joursUtilises, remainingDays: response.restants, createdAt: response.dateCreation, updatedAt: response.dateModification };
}

function toApiRequest(request: LeaveBalanceRequest): CongeSoldeApiRequest {
  return { employeId: request.userId, congeTypeId: request.leaveTypeId, annee: request.year, droitAcquis: request.totalDays, joursUtilises: 0, restants: request.totalDays };
}
