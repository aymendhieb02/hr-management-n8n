import { Component, EventEmitter, input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReportFilters, ReportType } from '../../models/report.model';

@Component({
  selector: 'app-report-filter',
  imports: [FormsModule],
  templateUrl: './report-filter.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class ReportFilterComponent {
  readonly filters = input.required<ReportFilters>();
  readonly reportType = input.required<ReportType>();
  readonly leaveTypes = input<string[]>([]);
  readonly departments = input<string[]>([]);
  readonly users = input<Array<{ id: number; name: string }>>([]);
  @Output() readonly filtersChange = new EventEmitter<ReportFilters>();
  @Output() readonly reportTypeChange = new EventEmitter<ReportType>();

  protected update(key: keyof ReportFilters, value: string): void {
    this.filtersChange.emit({ ...this.filters(), [key]: value });
  }
}
