import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LeaveType } from '../../../leave-types/models/leave-type.model';
import { LeaveRequestResponse, LeaveRequestUpdateRequest } from '../../models/leave-request.model';

@Component({
  selector: 'app-leave-request-form',
  imports: [ReactiveFormsModule],
  templateUrl: './leave-request-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveRequestFormComponent implements OnChanges {
  readonly request = input<LeaveRequestResponse | null>(null);
  readonly leaveTypes = input<LeaveType[]>([]);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  @Output() readonly save = new EventEmitter<LeaveRequestUpdateRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    leaveTypeId: new FormBuilder().control<number | null>(null, Validators.required),
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    reason: ['', Validators.maxLength(1000)]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['request']) {
      const request = this.request();
      this.form.reset({
        leaveTypeId: request?.leaveType.id ?? null,
        startDate: request?.startDate ?? '',
        endDate: request?.endDate ?? '',
        reason: request?.reason ?? ''
      });
    }
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) {
      return;
    }
    const value = this.form.getRawValue();
    this.save.emit({
      leaveTypeId: Number(value.leaveTypeId),
      startDate: value.startDate,
      endDate: value.endDate,
      reason: value.reason.trim() || null
    });
  }

  protected fieldError(field: keyof typeof this.form.controls): string | null {
    const control = this.form.controls[field];
    const backendError = this.validationErrors()[field];
    if (backendError) return backendError;
    if (!(control.touched || control.dirty)) return null;
    if (control.hasError('required')) return 'This field is required.';
    if (control.hasError('maxlength')) return 'Use 1000 characters or fewer.';
    return null;
  }
}
