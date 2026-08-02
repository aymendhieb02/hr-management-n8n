import { Component, EventEmitter, input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LeaveType } from '../../../leave-types/models/leave-type.model';
import { UserResponse } from '../../../users/models/user.model';
import { LeaveBalanceRequest, LeaveBalanceResponse } from '../../models/leave-balance.model';

@Component({
  selector: 'app-leave-balance-form',
  imports: [ReactiveFormsModule],
  templateUrl: './leave-balance-form.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveBalanceFormComponent implements OnChanges {
  readonly balance = input<LeaveBalanceResponse | null>(null);
  readonly users = input<UserResponse[]>([]);
  readonly leaveTypes = input<LeaveType[]>([]);
  readonly loading = input(false);
  @Output() readonly save = new EventEmitter<LeaveBalanceRequest>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected readonly form = new FormBuilder().nonNullable.group({
    userId: new FormBuilder().control<number | null>(null, Validators.required),
    leaveTypeId: new FormBuilder().control<number | null>(null, Validators.required),
    year: [new Date().getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2100)]],
    totalDays: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['balance']) {
      const balance = this.balance();
      this.form.reset({
        userId: balance?.user.id ?? null,
        leaveTypeId: balance?.leaveType.id ?? null,
        year: balance?.year ?? new Date().getFullYear(),
        totalDays: balance?.totalDays ?? 0
      });
    }
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;
    const value = this.form.getRawValue();
    this.save.emit({
      userId: Number(value.userId),
      leaveTypeId: Number(value.leaveTypeId),
      year: Number(value.year),
      totalDays: Number(value.totalDays)
    });
  }
}
