import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/notifications`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(NotificationService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('uses expected endpoints', () => {
    service.findAll().subscribe(); flush(base, 'GET', []);
    service.findById(1).subscribe(); flush(`${base}/1`, 'GET', {});
    service.findByUser(2).subscribe(); flush(`${base}/user/2`, 'GET', []);
    service.findUnread(2).subscribe(); flush(`${base}/user/2/unread`, 'GET', []);
    service.markAsRead(1).subscribe(); flush(`${base}/1/read`, 'PATCH', {});
    service.markAllAsRead(2).subscribe(); flush(`${base}/user/2/read-all`, 'PATCH', []);
    service.delete(1).subscribe(); flush(`${base}/1`, 'DELETE', null);
  });

  function flush(url: string, method: string, body: object | null): void {
    const req = http.expectOne(url);
    expect(req.request.method).toBe(method);
    req.flush(body);
  }
});
