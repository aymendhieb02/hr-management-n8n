import { Component, input } from '@angular/core';
import { LeaveRequestResponse } from '../../../leaves/models/leave-request.model';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';

@Component({
  selector: 'app-recent-leave-table',
  imports: [PaginatedTableDirective],
  template: `
    <section class="table-card">
      <table appPaginatedTable>
        <thead><tr><th>Employé</th><th>Type</th><th>Période</th><th>Statut</th></tr></thead>
        <tbody>
          @for (request of requests(); track request.id) {
            <tr><td>{{ request.requester.firstName }} {{ request.requester.lastName }}</td><td>{{ request.leaveType.name }}</td><td>{{ request.startDate }} au {{ request.endDate }}</td><td>{{ statusLabel(request.status) }}</td></tr>
          }
        </tbody>
      </table>
    </section>
  `,
  styleUrl: '../../../shared/resource-page.scss'
})
export class RecentLeaveTableComponent {
  readonly requests = input.required<LeaveRequestResponse[]>();
  protected statusLabel(status:LeaveRequestResponse['status']):string{return{PENDING:'En attente',APPROVED:'Approuvée',REJECTED:'Refusée',CANCELLED:'Annulée'}[status];}
}
