import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Department, DepartmentRequest } from '../../models/department.model';

@Component({
  selector: 'app-department-form',
  imports: [ReactiveFormsModule],
  templateUrl: './department-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DepartmentFormComponent implements OnChanges {
  readonly department = input<Department | null>(null);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  @Output() readonly save = new EventEmitter<DepartmentRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(255)]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['department']) {
      const department = this.department();
      this.form.reset({
        name: department?.name ?? '',
        description: department?.description ?? ''
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
      description: value.description.trim() || null
    });
  }

  protected fieldError(field: 'name' | 'description'): string | null {
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

    return null;
  }
}
