import { Component, computed, input, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { CalendarEvent } from '../../models/calendar-event.model';
import { AppIconComponent } from '../../../../shared/components/app-icon/app-icon.component';

interface WeekEventSegment { event: CalendarEvent; startColumn: number; span: number; lane: number; clippedStart: boolean; clippedEnd: boolean; }

@Component({
  selector: 'app-week-planner',
  imports: [AppIconComponent, DatePipe],
  templateUrl: './week-planner.component.html',
  styleUrl: './week-planner.component.scss'
})
export class WeekPlannerComponent {
  readonly selectedDate = input.required<Date>();
  readonly events = input.required<CalendarEvent[]>();
  readonly dateSelected = output<string>();
  protected readonly selectedEvent = signal<CalendarEvent | null>(null);

  protected readonly weekDays = computed(() => {
    const selected = this.selectedDate();
    const monday = new Date(selected.getFullYear(), selected.getMonth(), selected.getDate());
    const weekday = monday.getDay() || 7;
    monday.setDate(monday.getDate() - weekday + 1);
    return Array.from({ length: 7 }, (_, index) => {
      const date = new Date(monday);
      date.setDate(monday.getDate() + index);
      return date;
    });
  });

  protected readonly monthDays = computed(() => {
    const selected = this.selectedDate();
    const first = new Date(selected.getFullYear(), selected.getMonth(), 1);
    const offset = (first.getDay() + 6) % 7;
    const start = new Date(first);
    start.setDate(first.getDate() - offset);
    return Array.from({ length: 42 }, (_, index) => {
      const date = new Date(start);
      date.setDate(start.getDate() + index);
      return date;
    });
  });

  protected readonly weekEventSegments = computed<WeekEventSegment[]>(() => {
    const days = this.weekDays();
    const weekStart = this.dateKey(days[0]);
    const weekEnd = this.dateKey(days[6]);
    const laneEnds: number[] = [];
    return this.events()
      .filter((event) => event.startDate <= weekEnd && event.endDate >= weekStart)
      .map((event) => {
        const visibleStart = event.startDate < weekStart ? weekStart : event.startDate;
        const visibleEnd = event.endDate > weekEnd ? weekEnd : event.endDate;
        return { event, startColumn: days.findIndex((day) => this.dateKey(day) === visibleStart) + 1, endColumn: days.findIndex((day) => this.dateKey(day) === visibleEnd) + 1 };
      })
      .sort((a, b) => a.startColumn - b.startColumn || b.endColumn - a.endColumn)
      .map(({ event, startColumn, endColumn }) => {
        let lane = laneEnds.findIndex((lastColumn) => lastColumn < startColumn);
        if (lane < 0) { lane = laneEnds.length; laneEnds.push(endColumn); } else { laneEnds[lane] = endColumn; }
        return { event, startColumn, span: endColumn - startColumn + 1, lane: lane + 1, clippedStart: event.startDate < weekStart, clippedEnd: event.endDate > weekEnd };
      });
  });

  protected readonly eventLaneCount = computed(() => Math.max(1, ...this.weekEventSegments().map((segment) => segment.lane)));

  protected eventsForDay(date: Date): CalendarEvent[] {
    const key = this.dateKey(date);
    return this.events().filter((event) => event.startDate <= key && event.endDate >= key);
  }

  protected eventColorsForDay(date: Date): string[] {
    return [...new Set(this.eventsForDay(date).map((event) => this.eventColor(event)))];
  }

  protected eventColor(event: CalendarEvent): string {
    if (event.kind === 'HOLIDAY') return 'green';
    if (event.kind === 'RESTORED') return 'restored';
    if (event.kind === 'HALF_DAY') return 'violet';
    return event.nature === 'AUTORISATION_ABSENCE' ? 'orange' : 'blue';
  }

  protected statusLabel(status: string): string {
    return ({ DRAFT: 'Brouillon', BROUILLON: 'Brouillon', APPROVED: 'Approuvée', PENDING: 'En attente', REJECTED: 'Refusée', CANCELLED: 'Annulée' } as Record<string, string>)[status] ?? status.replaceAll('_', ' ');
  }

  protected natureLabel(nature: CalendarEvent['nature']): string {
    return nature === 'AUTORISATION_ABSENCE' ? "Autorisation d'absence" : 'Congé';
  }

  protected isToday(date: Date): boolean { return this.dateKey(date) === this.dateKey(new Date()); }
  protected isSelected(date: Date): boolean { return this.dateKey(date) === this.dateKey(this.selectedDate()); }
  protected isCurrentMonth(date: Date): boolean { return date.getMonth() === this.selectedDate().getMonth(); }
  protected dateKey(date: Date): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
