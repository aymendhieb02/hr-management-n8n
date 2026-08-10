import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { UserReferenceSummary } from '../models/user.model';

interface TypeContratApiResponse { id: number; libelle: string; actif: boolean; }

@Injectable({ providedIn: 'root' })
export class TypeContractService {
  private readonly http = inject(HttpClient);

  findAll(): Observable<UserReferenceSummary[]> {
    return this.http.get<TypeContratApiResponse[]>(`${environment.apiUrl}/type-contrats`).pipe(
      map((items) => items.filter((item) => item.actif).map((item) => ({ id: item.id, name: item.libelle })))
    );
  }
}
