import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { LeaveTypeService } from './leave-type.service';

describe('LeaveTypeService', () => {
  let service: LeaveTypeService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/conge-types`;
  const apiLeaveType = { id: 4, nom: 'Annuel', description: null, joursMaximum: 20, certificatObligatoire: false, remunere: true, actif: true, dateCreation: '2026-08-02T10:00:00', dateModification: null };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(LeaveTypeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('uses the expected API URLs', () => {
    service.findAll().subscribe();
    const findAll = httpMock.expectOne(baseUrl);
    expect(findAll.request.method).toBe('GET');
    findAll.flush([]);

    service.findActive().subscribe();
    const findActive = httpMock.expectOne(`${baseUrl}/actifs`);
    expect(findActive.request.method).toBe('GET');
    findActive.flush([]);

    service.findById(4).subscribe();
    const findById = httpMock.expectOne(`${baseUrl}/4`);
    expect(findById.request.method).toBe('GET');
    findById.flush(apiLeaveType);

    service.create({ name: 'Annual', description: null, maxDays: null, requiresMedicalCertificate: false, active: true }).subscribe();
    const create = httpMock.expectOne(baseUrl);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({ nom: 'Annual', description: null, joursMaximum: null, certificatObligatoire: false, remunere: true, actif: true });
    create.flush(apiLeaveType);

    service.update(4, { name: 'Sick', description: null, maxDays: 10, requiresMedicalCertificate: true, active: true }).subscribe();
    const update = httpMock.expectOne(`${baseUrl}/4`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual({ nom: 'Sick', description: null, joursMaximum: 10, certificatObligatoire: true, remunere: true, actif: true });
    update.flush(apiLeaveType);

    service.delete(4).subscribe();
    const remove = httpMock.expectOne(`${baseUrl}/4`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
