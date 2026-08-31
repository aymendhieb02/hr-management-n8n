import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { LeaveType } from '../../../leave-types/models/leave-type.model';
import { Reason } from '../../../reasons/reason.model';
import { JourFerieResponse } from '../../../jours-feries/models/jour-ferie.model';
import { LeaveRequestResponse, LeaveRequestUpdateRequest } from '../../models/leave-request.model';

@Component({
  selector: 'app-leave-request-form',
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './leave-request-form.component.html',
  styleUrls: ['../../../shared/resource-page.scss', './leave-request-form.component.scss']
})
export class LeaveRequestFormComponent implements OnChanges {
  protected readonly today = this.localDate(new Date());
  protected readonly firstAllowedDate = this.localDate(this.tomorrow());
  protected readonly systemDateLabel = new Intl.DateTimeFormat('fr-FR').format(new Date());
  protected readonly requestTypes = [{ id: 1, label: 'Congé', nature: 'CONGE' as const }, { id: 2, label: "Autorisation d'absence", nature: 'AUTORISATION_ABSENCE' as const }];
  protected readonly startTimeOptions = this.timeOptions(8 * 60 + 30, 17 * 60 + 45);
  readonly request = input<LeaveRequestResponse | null>(null);
  readonly inline = input(false);
  readonly leaveTypes = input<LeaveType[]>([]);
  readonly reasons = input<Reason[]>([]);
  readonly holidays = input<JourFerieResponse[]>([]);
  readonly maximumLeaveDays = input<number | null>(null);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  readonly submissionError = input<string | null>(null);
  @Output() readonly save = new EventEmitter<LeaveRequestUpdateRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    nature: new FormBuilder().nonNullable.control<'CONGE' | 'AUTORISATION_ABSENCE'>('CONGE'),
    leaveTypeId: new FormBuilder().control<number | null>(null, Validators.required),
    reasonChoice: ['', Validators.required],
    otherReason: ['', Validators.maxLength(255)],
    startDate: ['', Validators.required],
    numberOfDays: [1, [Validators.required, Validators.min(0.5), Validators.max(365)]],
    endDate: [''],
    startTime: [''],
    endTime: [''],
    reason: ['', Validators.maxLength(1000)]
  });
  protected formLevelError: string | null = null;
  protected halfDay = false;
  protected halfDayPeriod: 'MATIN' | 'APRES_MIDI' = 'MATIN';

  protected selectType(typeId: number | null): void {
    const nature = typeId === 2 ? 'AUTORISATION_ABSENCE' : 'CONGE';
    this.form.controls.nature.setValue(nature);
    if (nature === 'AUTORISATION_ABSENCE') {
      this.form.controls.endDate.setValue(this.form.controls.startDate.value);
    } else {
      this.form.controls.startTime.setValue('');
      this.form.controls.endTime.setValue('');
      this.calculateEndDate();
    }
    this.formLevelError = null;
  }

  protected startDateChanged(): void {
    const start = this.form.controls.startDate.value;
    const unavailableReason = this.unavailableStartDateReason(start);
    if (unavailableReason) {
      this.form.controls.startDate.setValue('');
      this.form.controls.endDate.setValue('');
      this.formLevelError = unavailableReason;
      return;
    }
    this.formLevelError = null;
    if (this.form.controls.nature.value === 'AUTORISATION_ABSENCE') this.form.controls.endDate.setValue(start);
    else this.calculateEndDate();
  }

  protected calculateEndDate(): void {
    const start = this.form.controls.startDate.value;
    const days = this.form.controls.numberOfDays.value;
    if (!start || days <= 0) { this.form.controls.endDate.setValue(''); return; }
    if (this.halfDay) { this.form.controls.endDate.setValue(start); this.updateBalanceError(); return; }

    const activeHolidays = new Set((this.holidays() || []).filter((h) => h.actif).map((h) => h.date));
    let current = new Date(`${start}T12:00:00`);
    let workingDaysCount = 0;

    while (workingDaysCount < days) {
      const dateStr = this.localDate(current);
      const dayOfWeek = current.getDay();
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
      const isHoliday = activeHolidays.has(dateStr);

      if (!isWeekend && !isHoliday) {
        workingDaysCount++;
      }

      if (workingDaysCount < days) {
        current.setDate(current.getDate() + 1);
      }
    }

    this.form.controls.endDate.setValue(this.localDate(current));
    this.updateBalanceError();
  }

  protected updateBalanceError(): void {
    const maximum = this.maximumLeaveDays();
    const requested = Number(this.form.controls.numberOfDays.value);
    if (this.form.controls.nature.value === 'CONGE' && maximum !== null && requested > maximum) {
      this.formLevelError = `Solde insuffisant : vous pouvez demander au maximum ${maximum} jour(s), demandes en attente comprises.`;
    } else if (this.formLevelError?.startsWith('Solde insuffisant')) {
      this.formLevelError = null;
    }
  }

  protected startTimeChanged(): void {
    const end = this.form.controls.endTime.value;
    if (end && (end < this.minimumEndTime() || end > this.maximumEndTime())) this.form.controls.endTime.setValue('');
  }

  protected endTimeOptions(): string[] {
    const start = this.form.controls.startTime.value;
    return start ? this.timeOptions(this.minutes(start) + 15, Math.min(this.minutes(start) + 120, 18 * 60)) : [];
  }

  protected displayedReasons(): Reason[] {
    return [...this.reasons()].sort((a, b) => {
      const rank = (value: string) => value.toLowerCase().includes('maladie') ? 0 : value.toLowerCase().includes('famil') ? 1 : 2;
      return rank(a.commentaire) - rank(b.commentaire) || a.commentaire.localeCompare(b.commentaire, 'fr');
    });
  }

  protected reasonChanged(): void {
    const control = this.form.controls.otherReason;
    const validators = [Validators.maxLength(255)];
    if (this.form.controls.reasonChoice.value === 'OTHER') validators.push(Validators.required);
    control.setValidators(validators);
    control.updateValueAndValidity();
    if (this.form.controls.reasonChoice.value === 'OTHER') control.markAsTouched();
  }

  protected minimumEndTime(): string { return this.shiftTime(this.form.controls.startTime.value, 1); }
  protected maximumEndTime(): string { return this.shiftTime(this.form.controls.startTime.value, 120); }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['request'] || changes['reasons'] || changes['holidays']) {
      const request = this.request();
      this.form.reset({
        nature: request?.nature ?? 'CONGE',
        leaveTypeId: request?.leaveType.id ?? null,
        reasonChoice: this.reasonChoice(request?.reason ?? null),
        otherReason: this.reasonChoice(request?.reason ?? null) === 'OTHER' ? request?.reason ?? '' : '',
        startDate: request?.startDate ?? '',
        numberOfDays: request?.requestedDays ?? 1,
        endDate: request?.endDate ?? '',
        startTime: request?.startTime?.slice(0, 5) ?? '',
        endTime: request?.endTime?.slice(0, 5) ?? '',
        reason: request?.reason ?? ''
      });
      this.halfDay = request?.nature === 'CONGE' && request.requestedDays === 0.5;
      this.halfDayPeriod = request?.startTime?.startsWith('14:00') ? 'APRES_MIDI' : 'MATIN';
      this.reasonChanged();
    }
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) {
      return;
    }
    const value = this.form.getRawValue();
    this.formLevelError = this.validateConditional(value);
    if (this.formLevelError) return;
    const authorization = value.nature === 'AUTORISATION_ABSENCE';
    const precision = value.otherReason.trim();
    this.save.emit({
      leaveTypeId: Number(value.leaveTypeId),
      nature: value.nature,
      reasonId: precision ? null : Number(value.reasonChoice),
      otherReason: precision || null,
      startDate: value.startDate,
      endDate: authorization ? value.startDate : value.endDate,
      startTime: authorization ? value.startTime : (this.halfDay ? (this.halfDayPeriod === 'MATIN' ? '08:30' : '14:00') : null),
      endTime: authorization ? value.endTime : (this.halfDay ? (this.halfDayPeriod === 'MATIN' ? '13:00' : '18:00') : null),
      numberOfDays: authorization ? 1 : (this.halfDay ? 0.5 : Number(value.numberOfDays)),
      reason: null
    });
  }

  private reasonChoice(reason: string | null): string {
    if (!reason) return '';
    return String(this.reasons().find((item) => item.commentaire === reason)?.id ?? 'OTHER');
  }

  private validateConditional(value: typeof this.form.value): string | null {
    if (value.reasonChoice === 'OTHER' && !value.otherReason?.trim()) return 'Veuillez saisir votre motif.';
    if (value.startDate && value.startDate < this.firstAllowedDate) return `La demande doit être déposée au moins 24 heures à l'avance (à partir du ${new Intl.DateTimeFormat('fr-FR').format(new Date(this.firstAllowedDate + 'T12:00:00'))}).`;
    const unavailableReason = this.unavailableStartDateReason(value.startDate ?? '');
    if (unavailableReason) return unavailableReason;
    if (value.nature === 'CONGE' && (!value.numberOfDays || value.numberOfDays <= 0 || (!this.halfDay && value.numberOfDays < 1))) return 'Le nombre de jours doit etre au minimum de 1, ou une demi-journée.';
    const maximum = this.maximumLeaveDays();
    if (value.nature === 'CONGE' && maximum !== null && Number(value.numberOfDays) > maximum) {
      return `Solde insuffisant : vous pouvez demander au maximum ${maximum} jour(s), demandes en attente comprises.`;
    }
    if (value.nature === 'AUTORISATION_ABSENCE') {
      if (!value.startTime || !value.endTime) return 'Les heures de debut et de fin sont obligatoires.';
      const start = this.minutes(value.startTime); const end = this.minutes(value.endTime);
      if (start < 8 * 60 + 30 || end > 18 * 60) return "L'autorisation doit etre comprise entre 08:30 et 18:00.";
      if (end <= start) return "L'heure de fin doit etre apres l'heure de debut.";
      if (end - start > 120) return "L'autorisation d'absence ne peut pas depasser 2 heures.";
    }
    return null;
  }
  private minutes(value: string): number { const [h, m] = value.split(':').map(Number); return h * 60 + m; }
  private shiftTime(value: string, minutes: number): string {
    if (!value) return '';
    const total = Math.min(23 * 60 + 59, this.minutes(value) + minutes);
    return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
  }
  private timeOptions(start: number, end: number): string[] {
    const values: string[] = [];
    for (let total = start; total <= end; total += 15) values.push(`${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`);
    return values;
  }
  private localDate(date: Date): string {
    const offset = date.getTimezoneOffset() * 60_000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 10);
  }

  private tomorrow(): Date {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    return date;
  }

  private unavailableStartDateReason(value: string): string | null {
    if (!value) return null;
    const date = new Date(`${value}T12:00:00`);
    if (date.getDay() === 0 || date.getDay() === 6) {
      return 'La date de départ doit être un jour ouvrable. Le samedi et le dimanche ne peuvent pas être sélectionnés.';
    }
    const holiday = (this.holidays() || []).find((item) => item.actif && item.date === value);
    return holiday ? `Le ${new Intl.DateTimeFormat('fr-FR').format(date)} est un jour férié${holiday.nom ? ` (${holiday.nom})` : ''}. Choisissez un jour ouvrable.` : null;
  }

  protected fieldError(field: keyof typeof this.form.controls): string | null {
    const control = this.form.controls[field];
    const backendError = this.validationErrors()[field];
    if (backendError) return backendError;
    if (!(control.touched || control.dirty)) return null;
    if (control.hasError('required')) return 'Ce champ est obligatoire.';
    if (control.hasError('min')) return 'Le nombre de jours doit etre au minimum de 1, ou une demi-journée.';
    if (control.hasError('maxlength')) return field === 'otherReason' ? 'Utilisez au maximum 255 caracteres.' : 'Utilisez au maximum 1000 caracteres.';
    return null;
  }
}
