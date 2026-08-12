import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { LeaveType } from '../../../leave-types/models/leave-type.model';
import { LeaveTypeService } from '../../../leave-types/services/leave-type.service';
import { Reason } from '../../../reasons/reason.model';
import { ReasonService } from '../../../reasons/reason.service';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';
import { JourFerieResponse } from '../../../jours-feries/models/jour-ferie.model';
import { JourFerieService } from '../../../jours-feries/services/jour-ferie.service';
import { LeaveDecisionDialogComponent } from '../../components/leave-decision-dialog/leave-decision-dialog.component';
import { LeaveDetailsComponent } from '../../components/leave-details/leave-details.component';
import { LeaveRequestFormComponent } from '../../components/leave-request-form/leave-request-form.component';
import { LeaveRequestResponse, LeaveRequestStatus, LeaveRequestUpdateRequest } from '../../models/leave-request.model';
import { LeaveRequestService } from '../../services/leave-request.service';
import { LeaveBalanceService } from '../../services/leave-balance.service';
import { MedicalDocumentService } from '../../../medical-documents/services/medical-document.service';

@Component({
  selector: 'app-leave-request-list',
  imports: [FormsModule, LeaveDecisionDialogComponent, LeaveDetailsComponent, LeaveRequestFormComponent, PaginatedTableDirective],
  templateUrl: './leave-request-list.component.html',
  styleUrls: ['../../../shared/resource-page.scss', './leave-request-list.component.scss']
})
export class LeaveRequestListComponent implements OnInit {
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly leaveTypeService = inject(LeaveTypeService);
  private readonly reasonService = inject(ReasonService);
  private readonly jourFerieService = inject(JourFerieService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly medicalDocumentService = inject(MedicalDocumentService);
  private readonly leaveBalanceService = inject(LeaveBalanceService);

  protected readonly requests = signal<LeaveRequestResponse[]>([]);
  protected readonly leaveTypes = signal<LeaveType[]>([]);
  protected readonly reasons = signal<Reason[]>([]);
  protected readonly holidays = signal<JourFerieResponse[]>([]);
  protected readonly currentBalance = signal<number | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly formSubmissionError = signal<string | null>(null);
  protected readonly certificateRequestIds = signal<Set<number>>(new Set());
  protected readonly searchTerm = signal('');
  protected readonly statusFilter = signal<LeaveRequestStatus | ''>('');
  protected readonly formOpen = signal(false);
  protected readonly editingRequest = signal<LeaveRequestResponse | null>(null);
  protected readonly detailRequest = signal<LeaveRequestResponse | null>(null);
  protected readonly decisionRequest = signal<LeaveRequestResponse | null>(null);
  protected readonly decisionMode = signal<'approve' | 'reject'>('approve');
  protected readonly statuses: LeaveRequestStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];
  protected readonly isManagerMode = computed(() => this.route.snapshot.routeConfig?.path === 'team-requests');
  protected readonly isCreateMode = computed(() => this.route.snapshot.routeConfig?.path === 'request-leave');
  protected readonly maximumLeaveDays = computed(() => {
    const balance = this.currentBalance();
    if (balance === null) return null;
    const editedId = this.editingRequest()?.id;
    const pendingDays = this.requests()
      .filter((request) => request.id !== editedId && request.status === 'PENDING' && request.nature === 'CONGE')
      .reduce((total, request) => total + request.requestedDays, 0);
    return Math.max(0, balance + 5 - pendingDays);
  });
  protected readonly title = computed(() => this.isManagerMode() ? "Demandes de l'equipe" : this.isCreateMode() ? 'Demander un conge' : 'Mes demandes de conge');
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
    this.reasonService.findAvailable().subscribe({ next: (reasons) => this.reasons.set(reasons), error: () => {} });
    this.jourFerieService.getActive().subscribe({ next: (holidays) => this.holidays.set(holidays), error: () => {} });
    this.loadRequests();
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && !this.isManagerMode()) {
      this.leaveBalanceService.findByUser(currentUser.id).subscribe({
        next: (balances) => this.currentBalance.set(balances[0]?.remainingDays ?? 0),
        error: () => this.currentBalance.set(null)
      });
    }
    if (this.isCreateMode()) this.openCreate();
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
        if (!this.isManagerMode()) this.loadEmployeeCertificates(currentUser.id);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set(this.isManagerMode() ? "Impossible de charger les demandes de l'equipe." : 'Impossible de charger les demandes de conge.');
        this.isLoading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.formErrors.set({});
    this.formSubmissionError.set(null);
    this.editingRequest.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(request: LeaveRequestResponse): void {
    this.formErrors.set({});
    this.formSubmissionError.set(null);
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
    this.formSubmissionError.set(null);
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.formOpen.set(false);
        this.success.set('Demande de conge enregistree.');
        this.loadRequests();
        if (this.isCreateMode()) void this.router.navigate(['/my-leave-requests']);
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.formSubmissionError.set(safeApiMessage(error, "Une erreur inattendue empêche l'enregistrement de la demande."));
        this.isSaving.set(false);
      }
    });
  }

  protected deleteRequest(request: LeaveRequestResponse): void {
    this.isSaving.set(true);
    this.leaveRequestService.delete(request.id).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.success.set('Demande de conge supprimee.');
        this.loadRequests();
      },
      error: (error) => {
        this.error.set(safeApiMessage(error, 'Impossible de supprimer la demande de conge.'));
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
        this.success.set(this.decisionMode() === 'approve' ? 'Demande de conge approuvee.' : 'Demande de conge refusee.');
        this.loadRequests();
      },
      error: (error) => {
        this.error.set(safeApiMessage(error, "Impossible d'enregistrer la decision."));
        this.isSaving.set(false);
      }
    });
  }

  protected statusLabel(status: LeaveRequestStatus): string {
    return { PENDING: 'En attente', APPROVED: 'Approuvee', REJECTED: 'Refusee', CANCELLED: 'Annulee' }[status];
  }

  protected countStatus(status: LeaveRequestStatus): number {
    return this.requests().filter((request) => request.status === status).length;
  }

  protected isSickLeave(request: LeaveRequestResponse): boolean {
    const reason = (request.reason ?? '').toLowerCase();
    return request.nature === 'CONGE' && (reason.includes('maladie') || reason.includes('médical'));
  }

  protected hasCertificate(requestId: number): boolean {
    return this.certificateRequestIds().has(requestId);
  }

  protected markCertificateAdded(requestId: number): void {
    this.certificateRequestIds.update((ids) => new Set(ids).add(requestId));
  }

  private loadEmployeeCertificates(employeeId: number): void {
    this.medicalDocumentService.findByEmployee(employeeId).subscribe({
      next: (certificates) => this.certificateRequestIds.set(new Set(certificates.map((certificate) => certificate.leaveRequestId))),
      error: () => this.certificateRequestIds.set(new Set())
    });
  }

  protected closeForm(): void {
    this.formOpen.set(false);
    if (this.isCreateMode()) void this.router.navigate(['/my-leave-requests']);
  }
}
