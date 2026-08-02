import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { DepartmentService } from './department.service';

describe('DepartmentService', () => {
  let service: DepartmentService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/departments`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DepartmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('uses the expected API URLs', () => {
    service.findAll().subscribe();
    const findAll = httpMock.expectOne(baseUrl);
    expect(findAll.request.method).toBe('GET');
    findAll.flush([]);

    service.findById(7).subscribe();
    const findById = httpMock.expectOne(`${baseUrl}/7`);
    expect(findById.request.method).toBe('GET');
    findById.flush({});

    service.create({ name: 'HR', description: null }).subscribe();
    const create = httpMock.expectOne(baseUrl);
    expect(create.request.method).toBe('POST');
    create.flush({});

    service.update(7, { name: 'People', description: 'Team' }).subscribe();
    const update = httpMock.expectOne(`${baseUrl}/7`);
    expect(update.request.method).toBe('PUT');
    update.flush({});

    service.delete(7).subscribe();
    const remove = httpMock.expectOne(`${baseUrl}/7`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
