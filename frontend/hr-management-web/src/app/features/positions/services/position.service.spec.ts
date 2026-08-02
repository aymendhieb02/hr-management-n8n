import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { PositionService } from './position.service';

describe('PositionService', () => {
  let service: PositionService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/postes`;
  const apiPosition = { id: 3, intitule: 'Ingenieur', description: 'Equipe tech', niveauPoste: null, actif: true, dateCreation: '2026-08-02T10:00:00', dateModification: null };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PositionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('uses the expected API URLs', () => {
    service.findAll().subscribe();
    const findAll = httpMock.expectOne(baseUrl);
    expect(findAll.request.method).toBe('GET');
    findAll.flush([]);

    service.findById(3).subscribe();
    const findById = httpMock.expectOne(`${baseUrl}/3`);
    expect(findById.request.method).toBe('GET');
    findById.flush(apiPosition);

    service.create({ title: 'Engineer', description: null }).subscribe();
    const create = httpMock.expectOne(baseUrl);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({ intitule: 'Engineer', description: null, niveauPoste: null, actif: true });
    create.flush(apiPosition);

    service.update(3, { title: 'Lead', description: 'Team lead' }).subscribe();
    const update = httpMock.expectOne(`${baseUrl}/3`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual({ intitule: 'Lead', description: 'Team lead', niveauPoste: null, actif: true });
    update.flush(apiPosition);

    service.delete(3).subscribe();
    const remove = httpMock.expectOne(`${baseUrl}/3`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
