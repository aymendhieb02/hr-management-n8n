import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/users`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('uses the expected API endpoints', () => {
    service.findAll().subscribe();
    const findAll = httpMock.expectOne(baseUrl);
    expect(findAll.request.method).toBe('GET');
    findAll.flush([]);

    service.findById(4).subscribe();
    const findById = httpMock.expectOne(`${baseUrl}/4`);
    expect(findById.request.method).toBe('GET');
    findById.flush({});

    service.findByRole('MANAGER').subscribe();
    const byRole = httpMock.expectOne(`${baseUrl}/role/MANAGER`);
    expect(byRole.request.method).toBe('GET');
    byRole.flush([]);

    service.findByDepartment(9).subscribe();
    const byDepartment = httpMock.expectOne(`${baseUrl}/department/9`);
    expect(byDepartment.request.method).toBe('GET');
    byDepartment.flush([]);

    service.findTeamMembers(2).subscribe();
    const team = httpMock.expectOne(`${baseUrl}/2/team`);
    expect(team.request.method).toBe('GET');
    team.flush([]);

    service.create({ username: 'u', email: 'u@test.com', password: 'password1', firstName: 'A', lastName: 'B', phone: null, hireDate: null, role: 'EMPLOYEE', status: 'ACTIVE', enabled: true, managerId: null, departmentId: null, positionId: null }).subscribe();
    const create = httpMock.expectOne(baseUrl);
    expect(create.request.method).toBe('POST');
    create.flush({});

    service.update(4, { username: 'u', email: 'u@test.com', firstName: 'A', lastName: 'B', phone: null, hireDate: null, role: 'EMPLOYEE', status: 'ACTIVE', enabled: true, managerId: null, departmentId: null, positionId: null }).subscribe();
    const update = httpMock.expectOne(`${baseUrl}/4`);
    expect(update.request.method).toBe('PUT');
    update.flush({});

    service.updatePassword(4, { newPassword: 'password1' }).subscribe();
    const password = httpMock.expectOne(`${baseUrl}/4/password`);
    expect(password.request.method).toBe('PATCH');
    password.flush(null);

    service.delete(4).subscribe();
    const remove = httpMock.expectOne(`${baseUrl}/4`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
