import { provideHttpClient } from '@angular/common/http';
import { HttpHeaders } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { MedicalDocumentService } from './medical-document.service';

describe('MedicalDocumentService', () => {
  let service: MedicalDocumentService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/certificats-medicaux-v2`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(MedicalDocumentService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('uploads multipart and downloads blob', () => {
    service.upload(9, new File(['x'], 'cert.pdf', { type: 'application/pdf' })).subscribe();
    const upload = http.expectOne(`${base}/televerser/9`);
    expect(upload.request.method).toBe('POST');
    expect(upload.request.body instanceof FormData).toBe(true);
    upload.flush({ id: 5, congeDemandeId: 9, nomFichier: 'cert.pdf', typeMime: 'application/pdf', tailleFichier: 1, dateSoumission: '2026-08-02T10:00:00' });

    service.download(5).subscribe((response) => {
      expect(response.body instanceof Blob).toBe(true);
      expect(service.filenameFromResponse(response)).toBe('cert.pdf');
    });
    const download = http.expectOne(`${base}/telecharger/5`);
    expect(download.request.responseType).toBe('blob');
    download.flush(new Blob(['x']), { headers: new HttpHeaders({ 'Content-Disposition': 'attachment; filename="cert.pdf"' }) });
  });
});
