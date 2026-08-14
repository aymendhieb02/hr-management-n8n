import { AfterViewInit, Directive, ElementRef, Input, OnDestroy, Renderer2 } from '@angular/core';

@Directive({ selector: 'table[appPaginatedTable]', standalone: true })
export class PaginatedTableDirective implements AfterViewInit, OnDestroy {
  @Input() pageSize = 8;
  private page = 1;
  private observer?: MutationObserver;
  private pager?: HTMLElement;

  constructor(
    private readonly element: ElementRef<HTMLTableElement>,
    private readonly renderer: Renderer2
  ) {}

  ngAfterViewInit(): void {
    this.createPager();
    const body = this.element.nativeElement.tBodies.item(0);
    this.observer = new MutationObserver(() => {
      this.page = 1;
      this.update();
    });
    if (body) this.observer.observe(body, { childList: true });
    queueMicrotask(() => this.update());
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    this.pager?.remove();
  }

  private rows(): HTMLTableRowElement[] {
    return Array.from(this.element.nativeElement.tBodies.item(0)?.rows ?? []);
  }

  private createPager(): void {
    const pager = this.renderer.createElement('div') as HTMLElement;
    this.renderer.addClass(pager, 'table-pagination');
    const table = this.element.nativeElement;
    const container = table.parentElement;
    if (container?.classList.contains('users-table-card') && container.parentElement) {
      this.renderer.addClass(pager, 'users-table-pagination');
      this.renderer.insertBefore(container.parentElement, pager, container.nextSibling);
    } else {
      this.renderer.insertBefore(container, pager, table.nextSibling);
    }
    this.pager = pager;
  }

  private update(): void {
    const rows = this.rows();
    const pages = Math.max(1, Math.ceil(rows.length / this.pageSize));
    this.page = Math.min(Math.max(this.page, 1), pages);
    rows.forEach((row, index) => this.renderer.setStyle(
      row,
      'display',
      index >= (this.page - 1) * this.pageSize && index < this.page * this.pageSize ? '' : 'none'
    ));

    if (!this.pager) return;
    this.pager.replaceChildren();
    if (rows.length === 0) {
      this.renderer.setStyle(this.pager, 'display', 'none');
      return;
    }
    this.renderer.removeStyle(this.pager, 'display');

    const start = (this.page - 1) * this.pageSize + 1;
    const end = Math.min(this.page * this.pageSize, rows.length);
    const info = document.createElement('div');
    info.className = 'pagination-info';
    info.innerHTML = `<span class="pagination-label">Affichage</span><strong>${start}–${end}</strong><span>sur ${rows.length} éléments</span>`;
    this.pager.append(info);

    const controls = document.createElement('div');
    controls.className = 'pagination-controls';
    const sizeLabel = document.createElement('label');
    sizeLabel.className = 'pagination-size';
    sizeLabel.append(document.createTextNode('Lignes par page'));
    const sizeSelect = document.createElement('select');
    sizeSelect.ariaLabel = 'Nombre de lignes par page';
    [5, 8, 10, 20].forEach((size) => {
      const option = document.createElement('option');
      option.value = String(size);
      option.textContent = String(size);
      option.selected = size === this.pageSize;
      sizeSelect.append(option);
    });
    sizeSelect.addEventListener('change', () => {
      this.pageSize = Number(sizeSelect.value);
      this.page = 1;
      this.update();
    });
    sizeLabel.append(sizeSelect);
    controls.append(sizeLabel);

    const navigation = document.createElement('div');
    navigation.className = 'pagination-navigation';
    navigation.append(this.button('«', 'Première page', this.page === 1, () => { this.page = 1; this.update(); }, 'pagination-arrow pagination-edge'));
    navigation.append(this.button('‹', 'Page précédente', this.page === 1, () => { this.page--; this.update(); }, 'pagination-arrow'));
    this.visiblePages(pages).forEach((value) => {
      if (value === '…') {
        const dots = document.createElement('span');
        dots.className = 'pagination-dots';
        dots.textContent = value;
        navigation.append(dots);
        return;
      }
      navigation.append(this.button(String(value), `Page ${value}`, false, () => {
        this.page = Number(value);
        this.update();
      }, Number(value) === this.page ? 'pagination-page active' : 'pagination-page'));
    });
    navigation.append(this.button('›', 'Page suivante', this.page === pages, () => { this.page++; this.update(); }, 'pagination-arrow'));
    navigation.append(this.button('»', 'Dernière page', this.page === pages, () => { this.page = pages; this.update(); }, 'pagination-arrow pagination-edge'));
    controls.append(navigation);
    this.pager.append(controls);
  }

  private visiblePages(total: number): (number | '…')[] {
    if (total <= 5) return Array.from({ length: total }, (_, index) => index + 1);
    if (this.page <= 3) return [1, 2, 3, 4, '…', total];
    if (this.page >= total - 2) return [1, '…', total - 3, total - 2, total - 1, total];
    return [1, '…', this.page - 1, this.page, this.page + 1, '…', total];
  }

  private button(label: string, ariaLabel: string, disabled: boolean, action: () => void, className: string): HTMLButtonElement {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = label;
    button.className = className;
    button.ariaLabel = ariaLabel;
    button.disabled = disabled;
    button.addEventListener('click', action);
    return button;
  }
}
