import { AfterViewChecked, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { LayoutStateService } from '../layout-state.service';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-layout',
  imports: [HeaderComponent, RouterOutlet, SidebarComponent],
  templateUrl: './app-layout.component.html',
  styleUrl: './app-layout.component.scss'
})
export class AppLayoutComponent implements AfterViewChecked {
  protected readonly layoutState = inject(LayoutStateService);

  ngAfterViewChecked(): void {
    const title = document.querySelector<HTMLElement>('.main-content .page-header h1, .main-content .resource-page > header h1, .main-content .availability-hero h1');
    if (!title || title.querySelector('app-icon, .page-heading-icon')) return;
    const activeIcon = document.querySelector<SVGElement>('.sidebar a.active app-icon svg');
    if (!activeIcon) return;
    const icon = activeIcon.cloneNode(true) as SVGElement;
    icon.classList.add('page-heading-icon');
    icon.setAttribute('aria-hidden', 'true');
    title.prepend(icon);
  }
}
