import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { MessageService } from 'src/app/Services/Messaging/message.service';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit {
  threads: any[] = [];
  contacts: any[] = [];
  loading = true;

  activeId: number | null = null;
  activeName = '';
  messages: any[] = [];
  threadLoading = false;

  draft = '';
  sending = false;
  showContacts = false;

  constructor(private api: MessageService, private route: ActivatedRoute, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.load();
    const withId = this.route.snapshot.queryParamMap.get('with');
    if (withId) { this.open(+withId); }
  }

  private load(): void {
    this.loading = true;
    this.api.threads().subscribe({
      next: t => { this.threads = t || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
    this.api.contacts().subscribe({
      next: c => (this.contacts = c || []),
      error: () => {}
    });
  }

  open(otherId: number, name?: string): void {
    this.activeId = otherId;
    this.showContacts = false;
    if (name) { this.activeName = name; }
    this.threadLoading = true;
    this.api.thread(otherId).subscribe({
      next: t => {
        this.messages = t?.messages || [];
        this.activeName = t?.otherName || this.activeName;
        this.threadLoading = false;
        // clear the unread badge on this conversation locally
        const s = this.threads.find(x => x.otherId === otherId);
        if (s) { s.unread = 0; }
      },
      error: () => { this.threadLoading = false; }
    });
  }

  send(): void {
    const text = this.draft.trim();
    if (!text || this.activeId == null || this.sending) { return; }
    this.sending = true;
    const recipient = this.activeId;
    this.api.send(recipient, text).subscribe({
      next: () => {
        this.draft = '';
        this.sending = false;
        this.open(recipient, this.activeName); // reload thread
        this.api.threads().subscribe({ next: t => (this.threads = t || []), error: () => {} });
      },
      error: () => { this.sending = false; this.toastr.error('Could not send message'); }
    });
  }

  startWith(contact: any): void {
    this.open(contact.id, contact.name || contact.email);
  }

  initials(name: string): string {
    if (!name) { return '?'; }
    const parts = name.trim().split(/\s+/);
    return (parts[0][0] + (parts[1] ? parts[1][0] : '')).toUpperCase();
  }
}
