import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TypeContrat { id:number; libelle:string; description:string|null; actif:boolean; dateCreation:string; dateModification:string|null; }
export interface TypeContratPayload { libelle:string; description:string|null; actif:boolean; }

@Injectable({providedIn:'root'})
export class TypeContractService {
  private readonly http=inject(HttpClient);private readonly url=`${environment.apiUrl}/type-contrats`;
  findAll():Observable<TypeContrat[]>{return this.http.get<TypeContrat[]>(this.url);}
  create(payload:TypeContratPayload):Observable<TypeContrat>{return this.http.post<TypeContrat>(this.url,payload);}
  update(id:number,payload:TypeContratPayload):Observable<TypeContrat>{return this.http.put<TypeContrat>(`${this.url}/${id}`,payload);}
  delete(id:number):Observable<void>{return this.http.delete<void>(`${this.url}/${id}`);}
}
