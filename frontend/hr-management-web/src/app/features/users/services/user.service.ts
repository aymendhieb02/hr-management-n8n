import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, of } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PasswordUpdateRequest, RoleType, UserCreateRequest, UserResponse, UserUpdateRequest } from '../models/user.model';

interface EmployeApiResponse { id: number; nom: string; prenom: string; email: string; telephone: string | null; adresse: string | null; dateNaissance: string | null; dateEmbauche: string | null; sexe: string | null; actif: boolean; role?: RoleType; statut?: string; poste: { id: number; intitule: string } | null; typeContrat: { id: number; libelle: string } | null; manager: { id: number; nom: string; prenom: string; email: string } | null; createdAt: string; updatedAt: string | null; }
interface EmployeApiRequest { nom: string; prenom: string; email: string; telephone: string | null; adresse: string | null; dateNaissance: string | null; dateEmbauche: string | null; sexe: string | null; actif: boolean; role: RoleType; posteId: number | null; typeContratId: number | null; managerId: number | null; }

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/employes`;

  findAll(): Observable<UserResponse[]> { return this.http.get<EmployeApiResponse[]>(this.baseUrl).pipe(map((items) => items.map(toUser))); }
  findById(id: number): Observable<UserResponse> { return this.http.get<EmployeApiResponse>(`${this.baseUrl}/${id}`).pipe(map(toUser)); }
  findByRole(role: RoleType): Observable<UserResponse[]> { return this.findAll().pipe(map((users) => users.filter((user) => user.role === role))); }
  findByDepartment(_departmentId: number): Observable<UserResponse[]> { return of([]); }
  findTeamMembers(managerId: number): Observable<UserResponse[]> { return this.http.get<EmployeApiResponse[]>(`${this.baseUrl}/${managerId}/equipe`).pipe(map((items) => items.map(toUser))); }
  findMe(): Observable<UserResponse> { return this.http.get<EmployeApiResponse>(`${this.baseUrl}/me`).pipe(map(toUser)); }
  create(request: UserCreateRequest): Observable<UserResponse> { return this.http.post<EmployeApiResponse>(this.baseUrl, toEmployeRequest(request)).pipe(map(toUser)); }
  update(id: number, request: UserUpdateRequest): Observable<UserResponse> { return this.http.put<EmployeApiResponse>(`${this.baseUrl}/${id}`, toEmployeRequest(request)).pipe(map(toUser)); }
  updatePassword(_id: number, request: PasswordUpdateRequest): Observable<void> { return this.http.put<void>(`${this.baseUrl}/me/password`, { motDePasseActuel: request.currentPassword, nouveauMotDePasse: request.newPassword }); }
  setActive(id: number, active: boolean): Observable<UserResponse> { return this.http.patch<EmployeApiResponse>(`${this.baseUrl}/${id}/active/${active}`, {}).pipe(map(toUser)); }
  resetPasswordToDefault(id: number): Observable<void> { return this.http.patch<void>(`${this.baseUrl}/${id}/password/default`, {}); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${id}`); }
}

function toUser(response: EmployeApiResponse): UserResponse {
  return { id: response.id, username: response.email, email: response.email, firstName: response.prenom, lastName: response.nom, phone: response.telephone, address: response.adresse, birthDate: response.dateNaissance, sex: response.sexe, hireDate: response.dateEmbauche, role: response.role ?? 'EMPLOYEE', status: response.actif ? 'ACTIVE' : 'INACTIVE', enabled: response.actif, manager: response.manager ? { id: response.manager.id, firstName: response.manager.prenom, lastName: response.manager.nom, email: response.manager.email } : null, department: null, position: response.poste ? { id: response.poste.id, name: response.poste.intitule } : null, typeContract: response.typeContrat ? { id: response.typeContrat.id, name: response.typeContrat.libelle } : null, createdAt: response.createdAt, updatedAt: response.updatedAt };
}
function toEmployeRequest(request: UserCreateRequest | UserUpdateRequest): EmployeApiRequest {
  return { nom: request.lastName.trim(), prenom: request.firstName.trim(), email: request.email.trim(), telephone: request.phone, adresse: request.address ?? null, dateNaissance: request.birthDate ?? null, dateEmbauche: request.hireDate, sexe: request.sex ?? null, actif: request.enabled && request.status === 'ACTIVE', role: request.role, posteId: request.positionId ?? null, typeContratId: request.typeContractId ?? null, managerId: request.managerId };
}

