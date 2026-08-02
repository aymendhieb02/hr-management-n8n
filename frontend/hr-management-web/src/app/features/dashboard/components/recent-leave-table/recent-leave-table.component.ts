import { Component, input } from '@angular/core';
import { LeaveRequestResponse } from '../../../leaves/models/leave-request.model';

@Component({
  selector: 'app-recent-leave-table',
  template: `
    <section class="table-card">
      <table>
        <thead><tr><th>Employee</th><th>Leave Type</th><th>Dates</th><th>Status</th></tr></thead>
        <tbody>
          @for (request of requests(); track request.id) {
            <tr><td>{{ request.requester.firstName }} {{ request.requester.lastName }}</td><td>{{ request.leaveType.name }}</td><td>{{ request.startDate }} to {{ request.endDate }}</td><td>{{ request.status }}</td></tr>
          }
        </tbody>
      </table>
    </section>
  `,
  styleUrl: '../../../shared/resource-page.scss'
})
export class RecentLeaveTableComponent {
  readonly requests = input.required<LeaveRequestResponse[]>();
}
