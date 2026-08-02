import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LeaveType, LeaveTypeRequest } from '../../models/leave-type.model';

@Component({
  selector: 'app-leave-type-form',
  imports: [ReactiveFormsModule],
  templateUrl: './leave-type-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveTypeFormComponent implements OnChanges {
  readonly leaveType = input<LeaveType | null>(null);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  @Output() readonly save = new EventEmitter<LeaveTypeRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(255)],
    maxDays: new FormBuilder().control<number | null>(null, [Validators.min(1)]),
    requiresMedicalCertificate: new FormBuilder().nonNullable.control(false),
    active: new FormBuilder().nonNullable.control(true)
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['leaveType']) {
      const leaveType = this.leaveType();
      this.form.reset({
        name: leaveType?.name ?? '',
        description: leaveType?.description ?? '',
        maxDays: leaveType?.maxDays ?? null,
        requiresMedicalCertificate: leaveType?.requiresMedicalCertificate ?? false,
        active: leaveType?.active ?? true
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
      name: value.name.trim(),
      description: value.description.trim() || null,
      maxDays: value.maxDays || null,
      requiresMedicalCertificate: value.requiresMedicalCertificate,
      active: value.active
    });
  }

  protected fieldError(field: 'name' | 'description' | 'maxDays'): string | null {
    const control = this.form.controls[field];
    const backendError = this.validationErrors()[field];

    if (backendError) {
      return backendError;
    }

    if (!(control.touched || control.dirty)) {
      return null;
    }

    if (control.hasError('required')) {
      return 'Name is required.';
    }

    if (control.hasError('maxlength')) {
      return field === 'name' ? 'Name must be 100 characters or fewer.' : 'Description must be 255 characters or fewer.';
    }

    if (control.hasError('min')) {
      return 'Max days must be greater than zero.';
    }

    return null;
  }
}
