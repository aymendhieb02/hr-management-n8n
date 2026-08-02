import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LeaveDecisionRequest, LeaveRequestCreateRequest, LeaveRequestResponse, LeaveRequestUpdateRequest } from '../models/leave-request.model';

interface CongeDemandeApiResponse {
  id: number;
  employe: { id: number; nom: string; prenom: string; email: string };
  decideur: { id: number; nom: string; prenom: string; email: string } | null;
  congeType: { id: number; nom: string };
  dateDebut: string;
  dateFin: string;
  nombreJours: number;
  commentaireEmploye: string | null;
  statut: LeaveRequestResponse['status'];
  dateSoumission: string;
  dateDecision: string | null;
  commentaireDecision: string | null;
}

interface CongeDemandeCreateApiRequest {
  employeId: number;
  congeTypeId: number;
  dateDebut: string;
  dateFin: string;
  commentaireEmploye: string | null;
}

interface CongeDemandeUpdateApiRequest {
  congeTypeId: number;
  dateDebut: string;
  dateFin: string;
  commentaireEmploye: string | null;
}

interface CongeDecisionApiRequest {
  decideurId: number;
  commentaire: string | null;
}

@Injectable({ providedIn: 'root' })
export class LeaveRequestService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/conge-demandes-v2`;

  findAll(): Observable<LeaveRequestResponse[]> {
    return this.http.get<CongeDemandeApiResponse[]>(this.baseUrl).pipe(map((items) => items.map(toLeaveRequest)));
  }

  findByRequester(requesterId: number): Observable<LeaveRequestResponse[]> {
    return this.http.get<CongeDemandeApiResponse[]>(`${this.baseUrl}/employe/${requesterId}`).pipe(map((items) => items.map(toLeaveRequest)));
  }

  findByApprover(approverId: number): Observable<LeaveRequestResponse[]> {
    return this.http.get<CongeDemandeApiResponse[]>(`${this.baseUrl}/decideur/${approverId}`).pipe(map((items) => items.map(toLeaveRequest)));
  }

  create(request: LeaveRequestCreateRequest): Observable<LeaveRequestResponse> {
    return this.http.post<CongeDemandeApiResponse>(this.baseUrl, toCreateRequest(request)).pipe(map(toLeaveRequest));
  }

  update(id: number, request: LeaveRequestUpdateRequest): Observable<LeaveRequestResponse> {
    return this.http.put<CongeDemandeApiResponse>(`${this.baseUrl}/${id}`, toUpdateRequest(request)).pipe(map(toLeaveRequest));
  }

  approve(id: number, request: LeaveDecisionRequest): Observable<LeaveRequestResponse> {
    return this.http.post<CongeDemandeApiResponse>(`${this.baseUrl}/${id}/approuver`, toDecisionRequest(request)).pipe(map(toLeaveRequest));
  }

  reject(id: number, request: LeaveDecisionRequest): Observable<LeaveRequestResponse> {
    return this.http.post<CongeDemandeApiResponse>(`${this.baseUrl}/${id}/refuser`, toDecisionRequest(request)).pipe(map(toLeaveRequest));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

function toLeaveRequest(response: CongeDemandeApiResponse): LeaveRequestResponse {
  return {
    id: response.id,
    requester: { id: response.employe.id, firstName: response.employe.prenom, lastName: response.employe.nom, email: response.employe.email },
    approver: response.decideur ? { id: response.decideur.id, firstName: response.decideur.prenom, lastName: response.decideur.nom, email: response.decideur.email } : null,
    leaveType: { id: response.congeType.id, name: response.congeType.nom },
    startDate: response.dateDebut,
    endDate: response.dateFin,
    requestedDays: response.nombreJours,
    reason: response.commentaireEmploye,
    status: response.statut,
    submittedAt: response.dateSoumission,
    decisionAt: response.dateDecision,
    decisionComment: response.commentaireDecision
  };
}

function toCreateRequest(request: LeaveRequestCreateRequest): CongeDemandeCreateApiRequest {
  return { employeId: request.requesterId, congeTypeId: request.leaveTypeId, dateDebut: request.startDate, dateFin: request.endDate, commentaireEmploye: request.reason?.trim() || null };
}

function toUpdateRequest(request: LeaveRequestUpdateRequest): CongeDemandeUpdateApiRequest {
  return { congeTypeId: request.leaveTypeId, dateDebut: request.startDate, dateFin: request.endDate, commentaireEmploye: request.reason?.trim() || null };
}

function toDecisionRequest(request: LeaveDecisionRequest): CongeDecisionApiRequest {
  return { decideurId: request.approverId, commentaire: request.comment?.trim() || null };
}
