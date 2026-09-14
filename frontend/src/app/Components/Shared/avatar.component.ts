import { Component, Input } from '@angular/core';

/**
 * Initials avatar used everywhere in place of profile photos: a coloured circle
 * with the person's initials. Colour is derived deterministically from the name
 * so the same person always gets the same colour. One component = one consistent
 * avatar style across the whole app.
 */
@Component({
  selector: 'app-avatar',
  template: `
    <div class="lb-avatar" [style.width.px]="size" [style.height.px]="size"
         [style.background]="bg" [style.fontSize.px]="size * 0.42" [title]="name || ''">
      {{ initials }}
    </div>
  `,
  styles: [`
    .lb-avatar {
      border-radius: 9999px; color: #fff; font-weight: 700;
      display: flex; align-items: center; justify-content: center;
      font-family: 'Fredoka','Rubik',sans-serif; line-height: 1;
      flex-shrink: 0; user-select: none; letter-spacing: .3px;
    }
  `]
})
export class AvatarComponent {
  @Input() name: string | null | undefined = '';
  @Input() size = 40;

  private static readonly PALETTE = [
    '#363af5', '#bb20d9', '#f9a207', '#128a4b', '#e5484d', '#0ea5b7', '#7c5cff', '#d9488b',
  ];

  get initials(): string {
    let n = (this.name || '').trim();
    if (!n) return '?';
    if (n.includes('@')) n = n.split('@')[0];
    const parts = n.split(/[\s._\-]+/).filter(Boolean);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return parts[0].slice(0, 2).toUpperCase();
  }

  get bg(): string {
    const n = (this.name || '').trim() || '?';
    let hash = 0;
    for (let i = 0; i < n.length; i++) { hash = (hash * 31 + n.charCodeAt(i)) & 0xffffffff; }
    const idx = Math.abs(hash) % AvatarComponent.PALETTE.length;
    return AvatarComponent.PALETTE[idx];
  }
}
