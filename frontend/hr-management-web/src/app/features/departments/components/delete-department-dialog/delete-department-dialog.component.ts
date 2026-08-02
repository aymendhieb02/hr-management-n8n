import { Component, EventEmitter, input, Output } from '@angular/core';
import { Department } from '../../models/department.model';

@Component({
  selector: 'app-delete-department-dialog',
  templateUrl: './delete-department-dialog.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class DeleteDepartmentDialogComponent {
  readonly department = input.required<Department>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  @Output() readonly confirmDelete = new EventEmitter<void>();
  @Output() readonly cancel = new EventEmitter<void>();
}
