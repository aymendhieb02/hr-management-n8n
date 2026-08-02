import { HttpClient, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { MedicalDocumentMetadataResponse, MedicalDocumentResponse } from '../models/medical-document.model';

interface CertificatMedicalApiResponse { id: number; congeDemandeId: number; nomFichier: string; typeMime: string; tailleFichier: number; dateSoumission: string; }

@Injectable({ providedIn: 'root' })
export class MedicalDocumentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/certificats-medicaux-v2`;

  upload(leaveRequestId: number, file: File): Observable<MedicalDocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CertificatMedicalApiResponse>(`${this.baseUrl}/televerser/${leaveRequestId}`, formData).pipe(map(toMedicalDocument));
  }

  findById(id: number): Observable<MedicalDocumentMetadataResponse> { return this.http.get<CertificatMedicalApiResponse>(`${this.baseUrl}/${id}`).pipe(map(toMedicalDocumentMetadata)); }
  download(id: number): Observable<HttpResponse<Blob>> { return this.http.get(`${this.baseUrl}/telecharger/${id}`, { observe: 'response', responseType: 'blob' }); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${id}`); }

  filenameFromResponse(response: HttpResponse<Blob>, fallback = 'certificat-medical'): string {
    const disposition = response.headers.get('Content-Disposition') ?? response.headers.get('content-disposition');
    const match = disposition?.match(/filename="?([^"]+)"?/i);
    return match?.[1] ?? fallback;
  }

  triggerDownload(response: HttpResponse<Blob>, fallback?: string): void {
    if (!response.body) return;
    const url = URL.createObjectURL(response.body);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = this.filenameFromResponse(response, fallback);
    anchor.click();
    URL.revokeObjectURL(url);
  }
}

function toMedicalDocument(response: CertificatMedicalApiResponse): MedicalDocumentResponse { return { id: response.id, leaveRequestId: response.congeDemandeId, originalFilename: response.nomFichier, storedFilename: '', mimeType: response.typeMime, fileSize: response.tailleFichier, uploadedAt: response.dateSoumission }; }
function toMedicalDocumentMetadata(response: CertificatMedicalApiResponse): MedicalDocumentMetadataResponse { return { id: response.id, leaveRequestId: response.congeDemandeId, originalFilename: response.nomFichier, mimeType: response.typeMime, fileSize: response.tailleFichier, uploadedAt: response.dateSoumission }; }
