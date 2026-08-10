import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { Position } from '../../models/position.model';
import { PositionService } from '../../services/position.service';
import { PositionListComponent } from './position-list.component';

describe('PositionListComponent', () => {
  const positions: Position[] = [
    { id: 2, title: 'Software Engineer', description: 'Builds products', createdAt: '2026-01-01T00:00:00', updatedAt: null }
  ];
  let fixture: ComponentFixture<PositionListComponent>;
  let positionService: {
    findAll: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let authService: { hasAnyRole: ReturnType<typeof vi.fn> };

  const text = () => fixture.nativeElement.textContent as string;
  const buttons = () => Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
  const buttonByText = (label: string) => buttons().find((button) => button.textContent?.trim() === label)!;
  const confirmDeleteButton = () => fixture.nativeElement.querySelector('.dialog .danger-button') as HTMLButtonElement;

  function configure(canManage = true): void {
    positionService = {
      findAll: vi.fn(() => of(positions)),
      create: vi.fn(() => of(positions[0])),
      update: vi.fn(() => of(positions[0])),
      delete: vi.fn(() => of(void 0))
    };
    authService = { hasAnyRole: vi.fn(() => canManage) };
    TestBed.configureTestingModule({
      imports: [PositionListComponent],
      providers: [
        { provide: PositionService, useValue: positionService },
        { provide: AuthService, useValue: authService }
      ]
    });
    fixture = TestBed.createComponent(PositionListComponent);
    fixture.detectChanges();
  }

  it('loads positions and filters by title', () => {
    configure();

    expect(positionService.findAll).toHaveBeenCalled();
    expect(text()).toContain('Software Engineer');

    const search = fixture.nativeElement.querySelector('.search-input') as HTMLInputElement;
    search.value = 'missing';
    search.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(text()).toContain('Aucun poste trouvé.');
  });

  it.each([
    ['HR', true],
    ['ADMIN', true],
    ['MANAGER', false],
    ['EMPLOYEE', false]
  ])('sets CRUD visibility for %s', (_role, canManage) => {
    configure(canManage);

    expect(text().includes('Ajouter un poste')).toBe(canManage);
    expect(text().includes('Modifier')).toBe(canManage);
    expect(text().includes('Supprimer')).toBe(canManage);
  });

  it('creates and updates a position from the form', () => {
    configure();

    buttonByText('Ajouter un poste').click();
    fixture.detectChanges();
    let title = fixture.nativeElement.querySelector('app-position-form input') as HTMLInputElement;
    title.value = '  QA Analyst  ';
    title.dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('app-position-form form').dispatchEvent(new Event('submit'));
    expect(positionService.create).toHaveBeenCalledWith({ title: 'QA Analyst', description: null, level: null, active: true });

    fixture.detectChanges();
    buttonByText('Modifier').click();
    fixture.detectChanges();
    title = fixture.nativeElement.querySelector('app-position-form input') as HTMLInputElement;
    title.value = 'Senior Engineer';
    title.dispatchEvent(new Event('input'));
    fixture.nativeElement.querySelector('app-position-form form').dispatchEvent(new Event('submit'));
    expect(positionService.update).toHaveBeenCalledWith(2, { title: 'Senior Engineer', description: 'Builds products', level: null, active: true });
  });

  it('deletes a position', () => {
    configure();

    buttonByText('Supprimer').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    expect(positionService.delete).toHaveBeenCalledWith(2);
  });

  it('handles delete conflict safely', () => {
    configure();

    positionService.delete.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    buttonByText('Supprimer').click();
    fixture.detectChanges();
    confirmDeleteButton().click();
    fixture.detectChanges();

    expect(text()).toContain('Cette opération est impossible car cet élément est déjà utilisé.');
  });
});
