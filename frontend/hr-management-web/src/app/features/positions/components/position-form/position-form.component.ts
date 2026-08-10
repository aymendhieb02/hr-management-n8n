import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Position, PositionRequest } from '../../models/position.model';

@Component({
  selector: 'app-position-form',
  imports: [ReactiveFormsModule],
  templateUrl: './position-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class PositionFormComponent implements OnChanges {
  readonly position = input<Position | null>(null);
  readonly loading = input(false);
  readonly validationErrors = input<Record<string, string>>({});
  @Output() readonly save = new EventEmitter<PositionRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(255)],
    level: ['', Validators.maxLength(100)],
    active: true
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['position']) {
      const position = this.position();
      this.form.reset({
        title: position?.title ?? '',
        description: position?.description ?? '',
        level: position?.level ?? '',
        active: position?.active ?? true
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
      title: value.title.trim(),
      description: value.description.trim() || null,
      level: value.level.trim() || null,
      active: value.active
    });
  }

  protected fieldError(field: 'title' | 'description' | 'level'): string | null {
    const control = this.form.controls[field];
    const backendError = this.validationErrors()[field];

    if (backendError) {
      return backendError;
    }

    if (!(control.touched || control.dirty)) {
      return null;
    }

    if (control.hasError('required')) {
      return "L'intitulé du poste est obligatoire.";
    }

    if (control.hasError('maxlength')) {
      if (field === 'title') return "L'intitulé ne doit pas dépasser 100 caractères.";
      if (field === 'level') return 'Le niveau ne doit pas dépasser 100 caractères.';
      return 'La description ne doit pas dépasser 255 caractères.';
    }

    return null;
  }
}
