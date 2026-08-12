import { Component, input } from '@angular/core';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';

@Component({
  selector: 'app-report-table',
  imports: [PaginatedTableDirective],
  template: `<section class="table-card"><table appPaginatedTable><thead><tr>@for (header of headers(); track header) { <th>{{ header }}</th> }</tr></thead><tbody>@for (row of rows(); track $index) { <tr>@for (header of headers(); track header) { <td>{{ row[header] }}</td> }</tr> }</tbody></table></section>`,
  styleUrl: '../../../shared/resource-page.scss'
})
export class ReportTableComponent {
  readonly rows = input.required<Record<string, unknown>[]>();
  protected headers(): string[] { return Object.keys(this.rows()[0] ?? {}); }
}
