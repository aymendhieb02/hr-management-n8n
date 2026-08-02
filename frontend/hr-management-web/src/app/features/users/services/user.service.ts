import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PasswordUpdateRequest, RoleType, UserCreateRequest, UserResponse, UserUpdateRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users`;

  findAll(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.baseUrl);
  }

  findById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/${id}`);
  }

  findByRole(role: RoleType): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.baseUrl}/role/${role}`);
  }

  findByDepartment(departmentId: number): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.baseUrl}/department/${departmentId}`);
  }

  findTeamMembers(managerId: number): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.baseUrl}/${managerId}/team`);
  }

  create(request: UserCreateRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.baseUrl, request);
  }

  update(id: number, request: UserUpdateRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/${id}`, request);
  }

  updatePassword(id: number, request: PasswordUpdateRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/password`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
