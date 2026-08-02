import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-leave-decision-dialog',
  imports: [ReactiveFormsModule],
  templateUrl: './leave-decision-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveDecisionDialogComponent implements OnChanges {
  readonly mode = input.required<'approve' | 'reject'>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly save = new EventEmitter<string | null>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    comment: ['', Validators.maxLength(1000)]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['mode']) {
      const validators = this.mode() === 'reject'
        ? [Validators.required, Validators.maxLength(1000)]
        : [Validators.maxLength(1000)];
      this.form.controls.comment.setValidators(validators);
      this.form.controls.comment.updateValueAndValidity();
      this.form.reset({ comment: '' });
    }
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;
    this.save.emit(this.form.controls.comment.value.trim() || null);
  }
}
