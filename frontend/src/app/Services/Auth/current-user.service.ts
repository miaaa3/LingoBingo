import { Injectable } from '@angular/core';
import { LocalService } from './local.service';

/** Single source of truth for the signed-in user's display identity. */
@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  constructor(private local: LocalService) {}

  get email(): string { return this.local.getData('email') || ''; }
  get name(): string {
    const n = this.local.getData('username');
    if (n && n.trim()) return n.trim();
    const e = this.email;
    return e ? e.split('@')[0] : '';
  }
}
