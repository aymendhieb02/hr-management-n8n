import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Department, DepartmentRequest } from '../models/department.model';

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/departments`;

  findAll(): Observable<Department[]> {
    return this.http.get<Department[]>(this.baseUrl);
  }

  findById(id: number): Observable<Department> {
    return this.http.get<Department>(`${this.baseUrl}/${id}`);
  }

  create(request: DepartmentRequest): Observable<Department> {
    return this.http.post<Department>(this.baseUrl, request);
  }

  update(id: number, request: DepartmentRequest): Observable<Department> {
    return this.http.put<Department>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
