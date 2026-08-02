import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { safeApiMessage, validationErrors } from '../../../shared/api-error.util';
import { DeletePositionDialogComponent } from '../../components/delete-position-dialog/delete-position-dialog.component';
import { PositionFormComponent } from '../../components/position-form/position-form.component';
import { Position, PositionRequest } from '../../models/position.model';
import { PositionService } from '../../services/position.service';

@Component({
  selector: 'app-position-list',
  imports: [DeletePositionDialogComponent, FormsModule, PositionFormComponent],
  templateUrl: './position-list.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class PositionListComponent implements OnInit {
  private readonly positionService = inject(PositionService);
  private readonly authService = inject(AuthService);

  protected readonly positions = signal<Position[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formErrors = signal<Record<string, string>>({});
  protected readonly deleteError = signal<string | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly editingPosition = signal<Position | null>(null);
  protected readonly deletingPosition = signal<Position | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly canManage = computed(() => this.authService.hasAnyRole('HR', 'ADMIN'));
  protected readonly filteredPositions = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();

    return search
      ? this.positions().filter((position) => position.title.toLowerCase().includes(search))
      : this.positions();
  });

  ngOnInit(): void {
    this.loadPositions();
  }

  protected loadPositions(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.positionService.findAll().subscribe({
      next: (positions) => {
        this.positions.set(positions);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Positions could not be loaded.');
        this.isLoading.set(false);
      }
    });
  }

  protected openCreate(): void {
    this.formErrors.set({});
    this.editingPosition.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(position: Position): void {
    this.formErrors.set({});
    this.editingPosition.set(position);
    this.formOpen.set(true);
  }

  protected savePosition(request: PositionRequest): void {
    const current = this.editingPosition();
    const operation = current ? this.positionService.update(current.id, request) : this.positionService.create(request);

    this.isSaving.set(true);
    this.formErrors.set({});
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.formOpen.set(false);
        this.loadPositions();
      },
      error: (error: HttpErrorResponse) => {
        this.formErrors.set(validationErrors(error));
        this.error.set(safeApiMessage(error, 'Position could not be saved.'));
        this.isSaving.set(false);
      }
    });
  }

  protected requestDelete(position: Position): void {
    this.deleteError.set(null);
    this.deletingPosition.set(position);
  }

  protected deletePosition(): void {
    const position = this.deletingPosition();

    if (!position) {
      return;
    }

    this.isDeleting.set(true);
    this.deleteError.set(null);
    this.positionService.delete(position.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.deletingPosition.set(null);
        this.loadPositions();
      },
      error: (error) => {
        this.deleteError.set(safeApiMessage(error, 'Position could not be deleted.'));
        this.isDeleting.set(false);
      }
    });
  }
}
