import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveType } from '../../../leave-types/models/leave-type.model';
import { LeaveTypeService } from '../../../leave-types/services/leave-type.service';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { LeaveDecisionDialogComponent } from '../../components/leave-decision-dialog/leave-decision-dialog.component';
import { LeaveDetailsComponent } from '../../components/leave-details/leave-details.component';
import { LeaveRequestFormComponent } from '../../components/leave-request-form/leave-request-form.component';
import { LeaveRequestResponse, LeaveRequestStatus, LeaveRequestUpdateRequest } from '../../models/leave-request.model';
import { LeaveRequestService } from '../../services/leave-request.service';

@Component({
  selector: 'app-leave-request-list',
  imports: [FormsModule, LeaveDecisionDialogComponent, LeaveDetailsComponent, LeaveRequestFormComponent],
  templateUrl: './leave-request-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class LeaveRequestListComponent implements OnInit {
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly leaveTypeService = inject(LeaveTypeService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly requests = signal<LeaveRequestResponse[]>([]);
  protected readonly leaveTypes = signal<LeaveType[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly searchTerm = signal('');
  protected readonly statusFilter = signal<LeaveRequestStatus | ''>('');
  protected readonly formOpen = signal(false);
  protected readonly editingRequest = signal<LeaveRequestResponse | null>(null);
  protected readonly detailRequest = signal<LeaveRequestResponse | null>(null);
  protected readonly decisionRequest = signal<LeaveRequestResponse | null>(null);
  protected readonly decisionMode = signal<'approve' | 'reject'>('approve');
  protected readonly statuses: LeaveRequestStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];
  protected readonly isManagerMode = computed(() => this.route.snapshot.routeConfig?.path === 'team-requests');
  protected readonly title = computed(() => this.isManagerMode() ? 'Team Requests' : 'My Leave Requests');
  protected readonly filteredRequests = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();
    const status = this.statusFilter();
    return this.requests()
      .filter((request) => !status || request.status === status)
      .filter((request) => !search || [
        request.leaveType.name,
        request.requester.firstName,
        request.requester.lastName,
        request.reason ?? ''
      ].some((value) => value.toLowerCase().includes(search)));
  });

  ngOnInit(): void {
    this.leaveTypeService.findActive().subscribe({ next: (types) => this.leaveTypes.set(types), error: () => {} });
    this.loadRequests();
  }

  protected loadRequests(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;
    this.isLoading.set(true);
    this.error.set(null);
    const source = this.isManagerMode()
      ? this.leaveRequestService.findByApprover(currentUser.id)
      : this.leaveRequestService.findByRequester(currentUser.id);
    source.subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set(this.isManagerMode() ? 'Team requests could not be loaded.' : 'Leave requests could not be loaded.');
        this.isLoading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.formErrors.set({});
    this.editingRequest.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(request: LeaveRequestResponse): void {
    this.formErrors.set({});
    this.editingRequest.set(request);
    this.formOpen.set(true);
  }

  protected saveRequest(request: LeaveRequestUpdateRequest): void {
    const currentUser = this.authService.getCurrentUser();
    const current = this.editingRequest();
    if (!currentUser) return;
    const operation = current
      ? this.leaveRequestService.update(current.id, request)
      : this.leaveRequestService.create({ ...request, requesterId: currentUser.id });
    this.isSaving.set(true);
    this.success.set(null);
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.formOpen.set(false);
        this.success.set('Leave request saved.');
        this.loadRequests();
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.error.set(safeApiMessage(error, 'Leave request could not be saved.'));
        this.isSaving.set(false);
      }
    });
  }

  protected deleteRequest(request: LeaveRequestResponse): void {
    this.isSaving.set(true);
    this.leaveRequestService.delete(request.id).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.success.set('Leave request deleted.');
        this.loadRequests();
      },
      error: (error) => {
        this.error.set(safeApiMessage(error, 'Leave request could not be deleted.'));
        this.isSaving.set(false);
      }
    });
  }

  protected openDecision(request: LeaveRequestResponse, mode: 'approve' | 'reject'): void {
    this.decisionRequest.set(request);
    this.decisionMode.set(mode);
  }

  protected saveDecision(comment: string | null): void {
    const currentUser = this.authService.getCurrentUser();
    const request = this.decisionRequest();
    if (!currentUser || !request) return;
    const payload = { approverId: currentUser.id, comment };
    const operation = this.decisionMode() === 'approve'
      ? this.leaveRequestService.approve(request.id, payload)
      : this.leaveRequestService.reject(request.id, payload);
    this.isSaving.set(true);
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.decisionRequest.set(null);
        this.success.set(this.decisionMode() === 'approve' ? 'Leave request approved.' : 'Leave request rejected.');
        this.loadRequests();
      },
      error: (error) => {
        this.error.set(safeApiMessage(error, 'Decision could not be saved.'));
        this.isSaving.set(false);
      }
    });
  }
}
