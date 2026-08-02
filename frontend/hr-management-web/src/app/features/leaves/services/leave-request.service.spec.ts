import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { LeaveRequestService } from './leave-request.service';

describe('LeaveRequestService', () => {
  let service: LeaveRequestService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/conge-demandes-v2`;
  const apiRequest = { id: 3, employe: { id: 1, nom: 'Ben Ali', prenom: 'Aymen', email: 'a@example.com' }, decideur: { id: 2, nom: 'Manager', prenom: 'Maya', email: 'm@example.com' }, congeType: { id: 2, nom: 'Annuel' }, dateDebut: '2026-08-01', dateFin: '2026-08-02', nombreJours: 2, commentaireEmploye: null, statut: 'PENDING', dateSoumission: '2026-08-02T10:00:00', dateDecision: null, commentaireDecision: null };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(LeaveRequestService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('uses expected endpoints', () => {
    service.findAll().subscribe(); expectReq(base, 'GET', []);
    service.findByRequester(1).subscribe(); expectReq(`${base}/employe/1`, 'GET', []);
    service.findByApprover(2).subscribe(); expectReq(`${base}/decideur/2`, 'GET', []);
    service.create({ requesterId: 1, leaveTypeId: 2, startDate: '2026-08-01', endDate: '2026-08-02', reason: null }).subscribe();
    expectReq(base, 'POST', apiRequest, { employeId: 1, congeTypeId: 2, dateDebut: '2026-08-01', dateFin: '2026-08-02', commentaireEmploye: null });
    service.update(3, { leaveTypeId: 2, startDate: '2026-08-01', endDate: '2026-08-02', reason: null }).subscribe();
    expectReq(`${base}/3`, 'PUT', apiRequest, { congeTypeId: 2, dateDebut: '2026-08-01', dateFin: '2026-08-02', commentaireEmploye: null });
    service.approve(3, { approverId: 2, comment: null }).subscribe(); expectReq(`${base}/3/approuver`, 'POST', apiRequest, { decideurId: 2, commentaire: null });
    service.reject(3, { approverId: 2, comment: 'No' }).subscribe(); expectReq(`${base}/3/refuser`, 'POST', apiRequest, { decideurId: 2, commentaire: 'No' });
    service.delete(3).subscribe(); expectReq(`${base}/3`, 'DELETE', null);
  });

  function expectReq(url: string, method: string, response: string | number | boolean | object | null, requestBody?: object): void {
    const req = http.expectOne(url);
    expect(req.request.method).toBe(method);
    if (requestBody) expect(req.request.body).toEqual(requestBody);
    req.flush(response);
  }
});
