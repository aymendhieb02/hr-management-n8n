import { Component, EventEmitter, input, Output } from '@angular/core';
import { LeaveType } from '../../models/leave-type.model';

@Component({
  selector: 'app-delete-leave-type-dialog',
  templateUrl: './delete-leave-type-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DeleteLeaveTypeDialogComponent {
  readonly leaveType = input.required<LeaveType>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly confirmDelete = new EventEmitter<void>();
  @Output() readonly cancel = new EventEmitter<void>();
}
