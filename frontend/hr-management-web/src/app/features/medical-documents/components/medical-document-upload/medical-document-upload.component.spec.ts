import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MedicalDocumentService } from '../../services/medical-document.service';
import { MedicalDocumentUploadComponent } from './medical-document-upload.component';

describe('MedicalDocumentUploadComponent', () => {
  let fixture: ComponentFixture<MedicalDocumentUploadComponent>;
  let service: any;

  beforeEach(async () => {
    service = { upload: vi.fn(() => of({ id: 1 })) };
    await TestBed.configureTestingModule({
      imports: [MedicalDocumentUploadComponent],
      providers: [{ provide: MedicalDocumentService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(MedicalDocumentUploadComponent);
    fixture.componentRef.setInput('leaveRequestId', 4);
    fixture.detectChanges();
  });

  it.each([
    ['application/pdf', 'cert.pdf'],
    ['image/jpeg', 'cert.jpg'],
    ['image/png', 'cert.png']
  ])('uploads valid %s', (type, name) => {
    select(new File(['x'], name, { type }));
    button('Upload').click();
    expect(service.upload).toHaveBeenCalledWith(4, expect.any(File));
  });

  it('rejects invalid, oversized, and empty files', () => {
    select(new File(['x'], 'bad.txt', { type: 'text/plain' }));
    expect(text()).toContain('Only PDF, JPEG, or PNG files are allowed.');
    select(new File([''], 'empty.pdf', { type: 'application/pdf' }));
    expect(text()).toContain('File cannot be empty.');
    select(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'large.pdf', { type: 'application/pdf' }));
    expect(text()).toContain('File must be 5 MB or smaller.');
  });

  function select(file: File): void {
    (fixture.componentInstance as any).selectFile({ target: { files: [file] } });
    fixture.detectChanges();
  }
  function button(label: string): HTMLButtonElement {
    return Array.from(fixture.nativeElement.querySelectorAll('button')).find((el: any) => el.textContent.trim() === label) as HTMLButtonElement;
  }
  function text(): string { return fixture.nativeElement.textContent; }
});
