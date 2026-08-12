import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { LeaveBalanceService } from './leave-balance.service';

describe('LeaveBalanceService', () => {
  let service: LeaveBalanceService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/conge-soldes-v2`;
  const apiBalance = { id: 3, employeId: 1, employeNom: 'Ben Ali', employePrenom: 'Aymen', congeTypeId: 2, congeTypeNom: 'Annuel', annee: 2026, droitAcquis: 20, joursUtilises: 4, restants: 16, dateCreation: '2026-08-02T10:00:00', dateModification: null };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(LeaveBalanceService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('uses expected endpoints', () => {
    service.findAll().subscribe(); expectReq(base, 'GET', []);
    service.findByUser(1).subscribe(); expectReq(`${base}/me`, 'GET', []);
    service.findMyTransactions().subscribe(); expectReq(`${environment.apiUrl}/conge-solde-historiques/me`, 'GET', []);
    service.findAllTransactions().subscribe(); expectReq(`${environment.apiUrl}/conge-solde-historiques`, 'GET', []);
    service.create({ userId: 1, leaveTypeId: 2, year: 2026, totalDays: 20 }).subscribe(); expectReq(base, 'POST', apiBalance, { employeId: 1, congeTypeId: 2, annee: 2026, droitAcquis: 20, joursUtilises: 0, restants: 20 });
    service.update(3, { userId: 1, leaveTypeId: 2, year: 2026, totalDays: 20 }).subscribe(); expectReq(`${base}/3`, 'PUT', apiBalance, { employeId: 1, congeTypeId: 2, annee: 2026, droitAcquis: 20, joursUtilises: 0, restants: 20 });
    service.delete(3).subscribe(); expectReq(`${base}/3`, 'DELETE', null);
  });

  function expectReq(url: string, method: string, response: string | number | boolean | object | null, requestBody?: object): void {
    const req = http.expectOne(url);
    expect(req.request.method).toBe(method);
    if (requestBody) expect(req.request.body).toEqual(requestBody);
    req.flush(response);
  }
});
