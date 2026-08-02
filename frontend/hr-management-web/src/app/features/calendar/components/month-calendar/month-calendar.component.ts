import { Component, input } from '@angular/core';
import { CalendarEvent } from '../../models/calendar-event.model';

@Component({
  selector: 'app-month-calendar',
  template: `<section class="table-card" aria-label="Month calendar"><table><tbody><tr>@for (day of days(); track day) { <td><strong>{{ day }}</strong><br />@for (event of eventsForDay(day); track event.leaveRequestId) { <span class="badge success">{{ event.employeeName }}</span> }</td> }</tr></tbody></table></section>`,
  styleUrl: '../../../shared/resource-page.scss'
})
export class MonthCalendarComponent {
  readonly month = input.required<Date>();
  readonly events = input.required<CalendarEvent[]>();

  protected days(): number[] {
    return Array.from({ length: new Date(this.month().getFullYear(), this.month().getMonth() + 1, 0).getDate() }, (_, index) => index + 1);
  }

  protected eventsForDay(day: number): CalendarEvent[] {
    const date = new Date(this.month().getFullYear(), this.month().getMonth(), day).toISOString().slice(0, 10);
    return this.events().filter((event) => event.startDate <= date && event.endDate >= date);
  }
}
