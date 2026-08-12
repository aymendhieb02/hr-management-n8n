import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { safeApiMessage, validationErrors } from '../shared/api-error.util';
import { PaginatedTableDirective } from '../shared/paginated-table.directive';
import { Reason, ReasonRequest } from './reason.model';
import { ReasonService } from './reason.service';

@Component({ selector: 'app-reason-admin', imports: [ReactiveFormsModule, PaginatedTableDirective], templateUrl: './reason-admin.component.html', styleUrl: '../shared/resource-page.scss' })
export class ReasonAdminComponent implements OnInit {
  private readonly service = inject(ReasonService);
  protected readonly reasons = signal<Reason[]>([]);
  protected readonly loading = signal(false); protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null); protected readonly formErrors = signal<Record<string,string>>({});
  protected readonly editing = signal<Reason | null>(null); protected readonly formOpen = signal(false);
  protected readonly form = new FormBuilder().nonNullable.group({ commentaire: ['', [Validators.required, Validators.maxLength(255)]], disponible: true });
  ngOnInit(): void { this.load(); }
  protected load(): void { this.loading.set(true); this.service.findAll().subscribe({ next: v => { this.reasons.set(v); this.loading.set(false); }, error: e => { this.error.set(safeApiMessage(e,'Impossible de charger les raisons.')); this.loading.set(false); } }); }
  protected open(reason: Reason | null): void { this.editing.set(reason); this.formErrors.set({}); this.form.reset({ commentaire: reason?.commentaire ?? '', disponible: reason?.disponible ?? true }); this.formOpen.set(true); }
  protected save(): void { this.form.markAllAsTouched(); if(this.form.invalid)return; const value=this.form.getRawValue() as ReasonRequest; const current=this.editing(); this.saving.set(true); (current?this.service.update(current.id,value):this.service.create(value)).subscribe({next:()=>{this.saving.set(false);this.formOpen.set(false);this.load();},error:(e:HttpErrorResponse)=>{this.formErrors.set(validationErrors(e));this.error.set(safeApiMessage(e,"Impossible d'enregistrer la raison."));this.saving.set(false);}}); }
  protected remove(reason: Reason): void { if(!confirm(`Supprimer « ${reason.commentaire} » ?`))return; this.service.delete(reason.id).subscribe({next:()=>this.load(),error:e=>this.error.set(safeApiMessage(e,'Cette raison est utilisee et ne peut pas etre supprimee.'))}); }
}
