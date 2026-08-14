import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LeaveDecisionRequest, LeaveRequestCreateRequest, LeaveRequestResponse, LeaveRequestUpdateRequest } from '../models/leave-request.model';

interface CongeDemandeApiResponse {
  id: number;
  employe: { id: number; nom: string; prenom: string; email: string; photoUrl?: string | null };
  decideur: { id: number; nom: string; prenom: string; email: string } | null;
  congeType: { id: number; nom: string };
  nature: LeaveRequestResponse['nature'];
  dateDebut: string;
  dateFin: string;
  heureDebut: string | null;
  heureFin: string | null;
  nombreJours: number;
  commentaireEmploye: string | null;
  raison: string | null;
  raisonId?: number|null;
  certificatMedicalRequis?: boolean;
  statut: { id: number; libelle: string };
  dateSoumission: string;
  dateDecision: string | null;
  commentaireDecision: string | null;
  samediCompte?: boolean;
  nombreJoursConsomme?: number|null;
}

interface CongeDemandeCreateApiRequest {
  employeId: number;
  congeTypeId: number;
  nature: LeaveRequestResponse['nature'];
  raisonId: number | null;
  autreMotif: string | null;
  dateDebut: string;
  dateFin: string;
  heureDebut: string | null;
  heureFin: string | null;
  nombreJours: number;
  commentaireEmploye: string | null;
}

interface CongeDemandeUpdateApiRequest {
  congeTypeId: number;
  nature: LeaveRequestResponse['nature'];
  raisonId: number | null;
  autreMotif: string | null;
  dateDebut: string;
  dateFin: string;
  heureDebut: string | null;
  heureFin: string | null;
  nombreJours: number;
  commentaireEmploye: string | null;
}

interface CongeDecisionApiRequest {
  decideurId: number;
  commentaire: string | null;
  samediCompte?: boolean;
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
    requester: { id: response.employe.id, firstName: response.employe.prenom, lastName: response.employe.nom, email: response.employe.email, photoUrl: response.employe.photoUrl },
    approver: response.decideur ? { id: response.decideur.id, firstName: response.decideur.prenom, lastName: response.decideur.nom, email: response.decideur.email } : null,
    leaveType: { id: response.congeType.id, name: response.congeType.nom },
    nature: response.nature,
    startDate: response.dateDebut,
    endDate: response.dateFin,
    startTime: response.heureDebut,
    endTime: response.heureFin,
    requestedDays: response.nombreJours,
    reason: response.raison,
    reasonId: response.raisonId ?? null,
    medicalCertificateRequired: Boolean(response.certificatMedicalRequis),
    status: toLeaveRequestStatus(response.statut.libelle),
    submittedAt: response.dateSoumission,
    decisionAt: response.dateDecision,
    decisionComment: response.commentaireDecision,
    saturdayCounts: response.samediCompte,
    consumedDays: response.nombreJoursConsomme
  };
}

function toLeaveRequestStatus(libelle: string): LeaveRequestResponse['status'] {
  switch (libelle) {
    case 'APPROUVEE':
      return 'APPROVED';
    case 'REFUSEE':
      return 'REJECTED';
    case 'ANNULEE':
      return 'CANCELLED';
    case 'EN_ATTENTE':
    default:
      return 'PENDING';
  }
}

function toCreateRequest(request: LeaveRequestCreateRequest): CongeDemandeCreateApiRequest {
  return { employeId: request.requesterId, congeTypeId: request.leaveTypeId, nature: request.nature, raisonId: request.reasonId, autreMotif: request.otherReason, dateDebut: request.startDate, dateFin: request.endDate, heureDebut: request.startTime, heureFin: request.endTime, nombreJours: request.numberOfDays, commentaireEmploye: request.reason?.trim() || null };
}

function toUpdateRequest(request: LeaveRequestUpdateRequest): CongeDemandeUpdateApiRequest {
  return { congeTypeId: request.leaveTypeId, nature: request.nature, raisonId: request.reasonId, autreMotif: request.otherReason, dateDebut: request.startDate, dateFin: request.endDate, heureDebut: request.startTime, heureFin: request.endTime, nombreJours: request.numberOfDays, commentaireEmploye: request.reason?.trim() || null };
}

function toDecisionRequest(request: LeaveDecisionRequest): CongeDecisionApiRequest {
  return { decideurId: request.approverId, commentaire: request.comment?.trim() || null, samediCompte: request.saturdayCounts };
}
