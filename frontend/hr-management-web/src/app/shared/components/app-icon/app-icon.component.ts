import { Component, input } from '@angular/core';

@Component({
  selector: 'app-icon',
  template: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      @switch (name()) {
        @case ('home') { <path d="m3 11 9-8 9 8"/><path d="M5 10v10h14V10M9 20v-6h6v6"/> }
        @case ('dashboard') { <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/> }
        @case ('profile') { <circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/> }
        @case ('bell') { <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/> }
        @case ('request') { <path d="M6 3h9l4 4v14H6z"/><path d="M14 3v5h5M9 13h6M9 17h4"/> }
        @case ('add-calendar') { <rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18M12 13v5M9.5 15.5h5"/> }
        @case ('balance') { <path d="M4 19h16M6 16V8M10 16V4M14 16v-6M18 16V7"/> }
        @case ('calendar') { <rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01"/> }
        @case ('team-request') { <path d="M15 19h6v-2a4 4 0 0 0-6-3.46M9 19H3v-2a4 4 0 0 1 6-3.46"/><circle cx="9" cy="7" r="4"/><path d="M15 3.5a4 4 0 0 1 0 7"/> }
        @case ('team') { <circle cx="9" cy="8" r="3"/><circle cx="17" cy="9" r="2.5"/><path d="M3 20a6 6 0 0 1 12 0M14 15a5 5 0 0 1 7 4.5"/> }
        @case ('availability') { <circle cx="8" cy="8" r="3"/><path d="M2.5 20a5.5 5.5 0 0 1 11 0M15 14l2 2 4-5"/> }
        @case ('transactions') { <path d="M7 7h13l-3-3M17 17H4l3 3"/><path d="m20 7-3 3M4 17l3-3"/> }
        @case ('position') { <rect x="3" y="7" width="18" height="13" rx="2"/><path d="M8 7V4h8v3M3 12h18M10 12v2h4v-2"/> }
        @case ('medical') { <path d="M9 3h6v5h5v8h-5v5H9v-5H4V8h5z"/> }
        @case ('reports') { <path d="M5 21V10M12 21V3M19 21v-7"/> }
        @case ('status') { <circle cx="12" cy="12" r="9"/><path d="m8 12 2.5 2.5L16.5 8"/> }
        @case ('contract') { <path d="M6 3h9l4 4v14H6z"/><path d="M14 3v5h5M9 12h6M9 16h6"/> }
        @case ('types') { <path d="M4 7h10M4 12h16M4 17h12"/><circle cx="18" cy="7" r="2"/> }
        @case ('holiday') { <path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M18.4 5.6l-2.1 2.1M7.7 16.3l-2.1 2.1"/><circle cx="12" cy="12" r="4"/> }
        @case ('history') { <path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/> }
        @case ('workflow') { <circle cx="6" cy="5" r="2"/><circle cx="18" cy="12" r="2"/><circle cx="6" cy="19" r="2"/><path d="M8 5h3a4 4 0 0 1 4 4v1M16 14v1a4 4 0 0 1-4 4H8"/> }
        @case ('settings') { <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06A1.7 1.7 0 0 0 15 19.4a1.7 1.7 0 0 0-1 .6 1.7 1.7 0 0 0-.4 1v.1h-4v-.1a1.7 1.7 0 0 0-1.1-1.6 1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.6 15a1.7 1.7 0 0 0-.6-1 1.7 1.7 0 0 0-1-.4h-.1v-4H3A1.7 1.7 0 0 0 4.6 8.5a1.7 1.7 0 0 0-.34-1.88l-.06-.06 2.83-2.83.06.06A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-.6 1.7 1.7 0 0 0 .4-1v-.1h4V3a1.7 1.7 0 0 0 1.1 1.6 1.7 1.7 0 0 0 1.88-.34l.06-.06 2.83 2.83-.06.06A1.7 1.7 0 0 0 19.4 9c.16.37.37.7.6 1 .28.25.64.39 1 .4h.1v4H21a1.7 1.7 0 0 0-1.6.6z"/> }
        @default { <circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/> }
      }
    </svg>
  `,
  styles: [`:host{display:inline-grid;width:1.25rem;height:1.25rem;flex:0 0 1.25rem;place-items:center}:host svg{width:100%;height:100%}`]
})
export class AppIconComponent { readonly name = input.required<string>(); }
