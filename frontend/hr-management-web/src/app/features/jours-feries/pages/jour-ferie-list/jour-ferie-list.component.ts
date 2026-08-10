import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { PaginatedTableDirective } from '../../../shared/paginated-table.directive';
import { JourFerieResponse, JourFerieRequest } from '../../models/jour-ferie.model';
import { JourFerieService } from '../../services/jour-ferie.service';

@Component({
  selector: 'app-jour-ferie-list',
  imports: [ReactiveFormsModule, PaginatedTableDirective],
  templateUrl: './jour-ferie-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class JourFerieListComponent implements OnInit {
  private readonly service = inject(JourFerieService);
  protected readonly holidays = signal<JourFerieResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly editing = signal<JourFerieResponse | null>(null);
  protected readonly formOpen = signal(false);

  protected readonly form = new FormBuilder().nonNullable.group({
    nom: ['', [Validators.required, Validators.maxLength(150)]],
    date: ['', [Validators.required]],
    description: ['', [Validators.maxLength(255)]],
    actif: true
  });

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (v) => {
        this.holidays.set(v);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(safeApiMessage(e, 'Impossible de charger les jours fériés.'));
        this.loading.set(false);
      }
    });
  }

  protected open(holiday: JourFerieResponse | null): void {
    this.editing.set(holiday);
    this.formErrors.set({});
    this.form.reset({
      nom: holiday?.nom ?? '',
      date: holiday?.date ?? '',
      description: holiday?.description ?? '',
      actif: holiday?.actif ?? true
    });
    this.formOpen.set(true);
  }

  protected save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue() as JourFerieRequest;
    const current = this.editing();
    this.saving.set(true);
    (current ? this.service.update(current.id, value) : this.service.create(value)).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (e: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(e));
        this.error.set(safeApiMessage(e, "Impossible d'enregistrer le jour férié."));
        this.saving.set(false);
      }
    });
  }

  protected remove(holiday: JourFerieResponse): void {
    if (!confirm(`Supprimer le jour férié « ${holiday.nom} » ?`)) return;
    this.service.delete(holiday.id).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(safeApiMessage(e, 'Impossible de supprimer ce jour férié.'))
    });
  }
}
