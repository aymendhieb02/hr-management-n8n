import { Component, input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  template: `<section class="state"><h2>{{ label() }}</h2><p class="stat-value">{{ value() }}</p></section>`,
  styles: [`.stat-value{font-size:2rem;font-weight:800;margin:0;color:#0f766e}`],
  styleUrl: '../../../shared/resource-page.scss'
})
export class StatCardComponent {
  readonly label = input.required<string>();
  readonly value = input.required<number>();
}
