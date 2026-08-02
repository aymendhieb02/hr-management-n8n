import { Component, input } from '@angular/core';

@Component({
  selector: 'app-status-chart',
  template: `
    <section class="state" aria-label="Leave request count by status">
      <h2>Leave Request Status</h2>
      @for (item of data(); track item.label) {
        <p>{{ item.label }}: {{ item.value }}</p>
        <progress max="100" [value]="max() ? (item.value / max()) * 100 : 0"></progress>
      }
    </section>
  `,
  styleUrl: '../../../shared/resource-page.scss'
})
export class StatusChartComponent {
  readonly data = input.required<Array<{ label: string; value: number }>>();
  protected max(): number { return Math.max(...this.data().map((item) => item.value), 0); }
}
