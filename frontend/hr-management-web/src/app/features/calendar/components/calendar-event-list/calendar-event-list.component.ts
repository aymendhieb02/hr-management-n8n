import { Component, input } from '@angular/core';
import { CalendarEvent } from '../../models/calendar-event.model';

@Component({
  selector: 'app-calendar-event-list',
  template: `<section class="state" aria-label="Calendar event list"><h2>Events</h2>@for (event of events(); track event.leaveRequestId) { <p>{{ event.employeeName }} - {{ event.leaveTypeName }} - {{ event.startDate }} to {{ event.endDate }}</p> } @empty { <p>No events found.</p> }</section>`,
  styleUrl: '../../../shared/resource-page.scss'
})
export class CalendarEventListComponent {
  readonly events = input.required<CalendarEvent[]>();
}
