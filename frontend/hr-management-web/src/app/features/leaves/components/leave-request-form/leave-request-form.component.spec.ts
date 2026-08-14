import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LeaveRequestFormComponent } from './leave-request-form.component';

describe('LeaveRequestFormComponent', () => {
  let fixture: ComponentFixture<LeaveRequestFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [LeaveRequestFormComponent] }).compileComponents();
    fixture = TestBed.createComponent(LeaveRequestFormComponent);
    fixture.componentRef.setInput('leaveTypes', [{ id: 1, name: 'Annual', description: null, maxDays: null, requiresMedicalCertificate: false, active: true, createdAt: '', updatedAt: null }]);
    fixture.componentRef.setInput('reasons', [{ id: 5, commentaire: 'Conge de maladie', disponible: true, dateCreation: '' }]);
    fixture.detectChanges();
  });

  it('validates and emits trimmed request values', () => {
    const save = vi.spyOn(fixture.componentInstance.save, 'emit');
    (fixture.componentInstance as any).form.setValue({ nature: 'CONGE', leaveTypeId: 1, reasonChoice: '5', otherReason: '', startDate: '2027-08-16', numberOfDays: 3, endDate: '', startTime: '', endTime: '', reason: '' });
    (fixture.componentInstance as any).calculateEndDate();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(save).toHaveBeenCalledWith({ nature: 'CONGE', leaveTypeId: 1, reasonId: 5, otherReason: null, startDate: '2027-08-16', endDate: '2027-08-18', numberOfDays: 3, startTime: null, endTime: null, reason: null });
  });

  it('stores an authorization on one day and rejects durations over two hours', () => {
    const save = vi.spyOn(fixture.componentInstance.save, 'emit');
    const form = (fixture.componentInstance as any).form;
    form.setValue({ nature: 'AUTORISATION_ABSENCE', leaveTypeId: 2, reasonChoice: 'OTHER', otherReason: 'Rendez-vous administratif', startDate: '2027-08-16', numberOfDays: 1, endDate: '', startTime: '08:30', endTime: '10:31', reason: '' });
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(save).not.toHaveBeenCalled();
    form.controls.endTime.setValue('10:30');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(save).toHaveBeenCalledWith(expect.objectContaining({ nature: 'AUTORISATION_ABSENCE', otherReason: 'Rendez-vous administratif', startDate: '2027-08-16', endDate: '2027-08-16', startTime: '08:30', endTime: '10:30' }));
    expect((fixture.componentInstance as any).endTimeOptions()).toEqual(['08:45', '09:00', '09:15', '09:30', '09:45', '10:00', '10:15', '10:30']);
    form.controls.startTime.setValue('17:00');
    expect((fixture.componentInstance as any).endTimeOptions()).toEqual(['17:15', '17:30', '17:45', '18:00']);
  });

  it('restores a saved standard reason and a custom reason in edit mode', () => {
    const baseRequest = { id: 9, requester: { id: 1, firstName: 'A', lastName: 'B', email: 'a@b.com' }, approver: null, leaveType: { id: 1, name: 'Conge' }, nature: 'CONGE' as const, startDate: '2026-08-10', endDate: '2026-08-12', startTime: null, endTime: null, requestedDays: 3, status: 'PENDING' as const, submittedAt: '', decisionAt: null, decisionComment: null };
    fixture.componentRef.setInput('request', { ...baseRequest, reason: 'Conge de maladie' });
    fixture.detectChanges();
    expect((fixture.componentInstance as any).form.controls.reasonChoice.value).toBe('5');
    fixture.componentRef.setInput('request', { ...baseRequest, reason: 'Rendez-vous personnel' });
    fixture.detectChanges();
    expect((fixture.componentInstance as any).form.controls.reasonChoice.value).toBe('OTHER');
    expect((fixture.componentInstance as any).form.controls.otherReason.value).toBe('Rendez-vous personnel');
  });

  it('blocks zero days and shows the custom-motif error beside its field', () => {
    const form = (fixture.componentInstance as any).form;
    form.controls.numberOfDays.setValue(0);
    form.controls.numberOfDays.markAsDirty();
    form.controls.reasonChoice.setValue('OTHER');
    (fixture.componentInstance as any).reasonChanged();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Le nombre de jours doit etre au minimum de 1.');
    const motifField = Array.from(fixture.nativeElement.querySelectorAll('.form-field')).find((element: any) => element.textContent.includes('Précisez votre motif')) as HTMLElement;
    expect(motifField.textContent).toContain('Veuillez saisir votre motif.');
    expect((fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement).disabled).toBe(true);
  });
});
