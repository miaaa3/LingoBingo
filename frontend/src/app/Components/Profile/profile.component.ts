import { Component, OnInit } from '@angular/core';
import { ProfileService, Profile } from 'src/app/Services/User/profile.service';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profile: Profile | null = null;
  loading = true;
  saving = false;

  name = '';
  username = '';

  constructor(private api: ProfileService, private local: LocalService, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.api.me().subscribe({
      next: p => { this.profile = p; this.name = p.name || ''; this.username = p.username || ''; this.loading = false; },
      error: () => { this.loading = false; this.toastr.error('Could not load your profile.'); },
    });
  }

  get dirty(): boolean {
    return !!this.profile && (this.name.trim() !== (this.profile.name || '') || this.username.trim() !== (this.profile.username || ''));
  }

  save(): void {
    if (!this.dirty || this.saving) return;
    if (!this.name.trim()) { this.toastr.warning('Name cannot be empty.'); return; }
    this.saving = true;
    this.api.update({ name: this.name.trim(), username: this.username.trim() }).subscribe({
      next: p => {
        this.profile = p; this.saving = false;
        this.local.saveData('username', p.name || '');   // keep sidebar/navbar in sync
        this.toastr.success('Profile updated.');
      },
      error: () => { this.saving = false; this.toastr.error('Could not save your profile.'); },
    });
  }

  memberSince(): string {
    if (!this.profile?.createdAt) return '';
    const d = new Date(this.profile.createdAt);
    return isNaN(d.getTime()) ? '' : d.toLocaleDateString(undefined, { year: 'numeric', month: 'long' });
  }
}
