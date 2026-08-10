import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { safeApiMessage, validationErrors } from '../shared/api-error.util';
import { PaginatedTableDirective } from '../shared/paginated-table.directive';

interface LeaveRequestStatus {
  id: number;
  libelle: string;
  description: string | null;
  actif: boolean;
}

interface LeaveRequestStatusRequest {
  libelle: string;
  description: string | null;
  actif: boolean;
}

@Component({
  selector: 'app-leave-request-statuses',
  imports: [FormsModule, ReactiveFormsModule, PaginatedTableDirective],
  templateUrl: './leave-request-statuses.component.html',
  styleUrl: '../shared/resource-page.scss'
})
export class LeaveRequestStatusesComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/conge-demande-statuts`;

  protected readonly statuses = signal<LeaveRequestStatus[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly searchTerm = signal('');
  protected readonly activeOnly = signal(false);
  protected readonly formOpen = signal(false);
  protected readonly editing = signal<LeaveRequestStatus | null>(null);
  protected readonly pendingDelete = signal<LeaveRequestStatus | null>(null);
  protected readonly filtered = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();
    return this.statuses()
      .filter((status) => !this.activeOnly() || status.actif)
      .filter((status) => !search || status.libelle.toLowerCase().includes(search));
  });

  protected readonly form = new FormBuilder().nonNullable.group({
    libelle: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(255)],
    actif: true
  });

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<LeaveRequestStatus[]>(this.baseUrl).subscribe({
      next: (items) => {
        this.statuses.set(items);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(safeApiMessage(error, 'Impossible de charger les statuts.'));
        this.loading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.editing.set(null);
    this.formErrors.set({});
    this.form.reset({ libelle: '', description: '', actif: true });
    this.formOpen.set(true);
  }

  protected openEdit(status: LeaveRequestStatus): void {
    this.editing.set(status);
    this.formErrors.set({});
    this.form.reset({ libelle: status.libelle, description: status.description ?? '', actif: status.actif });
    this.formOpen.set(true);
  }

  protected save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving()) return;

    const value = this.form.getRawValue();
    const request: LeaveRequestStatusRequest = {
      libelle: value.libelle.trim(),
      description: value.description.trim() || null,
      actif: value.actif
    };
    const current = this.editing();
    const operation = current
      ? this.http.put(`${this.baseUrl}/${current.id}`, request)
      : this.http.post(this.baseUrl, request);

    this.saving.set(true);
    this.formErrors.set({});
    this.error.set(null);
    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.error.set(safeApiMessage(error, "Impossible d'enregistrer le statut."));
        this.saving.set(false);
      }
    });
  }

  protected confirmDelete(): void {
    const status = this.pendingDelete();
    if (!status) return;

    this.deleting.set(true);
    this.deleteError.set(null);
    this.http.delete<void>(`${this.baseUrl}/${status.id}`).subscribe({
      next: () => {
        this.deleting.set(false);
        this.pendingDelete.set(null);
        this.load();
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, 'Impossible de supprimer ce statut.'));
        this.deleting.set(false);
      }
    });
  }

  protected fieldError(field: 'libelle' | 'description'): string | null {
    const backendError = this.formErrors()[field];
    if (backendError) return backendError;

    const control = this.form.controls[field];
    if (!(control.touched || control.dirty)) return null;
    if (control.hasError('required')) return 'Le libelle est obligatoire.';
    if (control.hasError('maxlength')) {
      return field === 'libelle' ? 'Maximum 100 caracteres.' : 'Maximum 255 caracteres.';
    }
    return null;
  }
}
