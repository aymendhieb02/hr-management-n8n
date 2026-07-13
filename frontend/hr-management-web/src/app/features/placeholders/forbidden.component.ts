import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  template: `
    <main class="forbidden-shell">
      <section>
        <p>XTENSUS HR</p>
        <h1>Forbidden</h1>
        <a routerLink="/">Return home</a>
      </section>
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100dvh;
      background: #f4f7f9;
    }

    .forbidden-shell {
      align-items: center;
      display: flex;
      justify-content: center;
      min-height: 100dvh;
      padding: 24px;
    }

    section {
      background: #ffffff;
      border: 1px solid #dbe4ea;
      border-radius: 8px;
      max-width: 420px;
      padding: 28px;
      width: 100%;
    }

    p {
      color: #0f766e;
      font-size: 0.76rem;
      font-weight: 700;
      margin: 0 0 8px;
    }

    h1 {
      color: #172033;
      margin: 0 0 18px;
    }

    a {
      color: #0f766e;
      font-weight: 700;
    }
  `]
})
export class ForbiddenComponent {}
