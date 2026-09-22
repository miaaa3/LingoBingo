import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';
import { Router } from '@angular/router';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { ThemeService } from 'src/app/Services/Theme/theme.service';
import { CurrentUserService } from 'src/app/Services/Auth/current-user.service';
import { NotificationService } from 'src/app/Services/Notification/notification.service';
import { BehaviorSubject } from 'rxjs';


@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  animations: [
    trigger('dropdownAnimation', [
      transition('void => visible', [
        style({ opacity: 0, transform: 'scale(0.95)' }),
        animate('100ms ease-out', style({ opacity: 1, transform: 'scale(1)' })),
      ]),
      transition('visible => void', [
        style({ opacity: 1, transform: 'scale(1)' }),
        animate('75ms ease-in', style({ opacity: 0, transform: 'scale(0.95)' })),
      ]),
    ]),
  ],
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  open = false;
  openForProfile = false;
  openForNotification = false;
  isUser: BehaviorSubject<boolean>;

  notifications: any[] = [];
  unread = 0;
  notifLoading = false;
  private notifPoll: any = null;

  constructor(
    private local: LocalService,
    public theme: ThemeService,
    private currentUser: CurrentUserService,
    private notifs: NotificationService,
    private router: Router
  ) {
    this.isUser = new BehaviorSubject<boolean>(this.local.getData("userApiKey2") !== "");
  }

  ngOnInit(): void {
    if (this.isUser.value) {
      this.refreshUnread();
      // Light poll so the badge stays current without a websocket.
      this.notifPoll = setInterval(() => this.refreshUnread(), 30000);
    }
  }

  ngOnDestroy(): void {
    if (this.notifPoll) { clearInterval(this.notifPoll); }
  }

  toggleTheme(): void {
    this.theme.toggle();
  }

  get displayName(): string { return this.currentUser.name; }
  get email(): string { return this.currentUser.email; }

  // --- notifications ---
  refreshUnread(): void {
    this.notifs.unreadCount().subscribe({
      next: r => (this.unread = r?.count || 0),
      error: () => {}
    });
  }

  loadNotifications(): void {
    this.notifLoading = true;
    this.notifs.list().subscribe({
      next: n => { this.notifications = n || []; this.notifLoading = false; },
      error: () => { this.notifLoading = false; }
    });
  }

  openNotification(n: any): void {
    if (n && !n.read) {
      n.read = true;
      this.unread = Math.max(0, this.unread - 1);
      this.notifs.markRead(n.id).subscribe({ next: () => {}, error: () => {} });
    }
    this.closeAll();
    if (n?.link) { this.router.navigateByUrl(n.link); }
  }

  markAllRead(): void {
    this.notifications.forEach(n => (n.read = true));
    this.unread = 0;
    this.notifs.markAllRead().subscribe({ next: () => {}, error: () => {} });
  }

  logout(): void {
    this.local.removeData("userApiKey2");
    this.local.removeData("userid");
    this.local.removeData("role");
    this.local.removeData("username");
    this.local.removeData("email");
    this.isUser.next(false);
    if (this.notifPoll) { clearInterval(this.notifPoll); this.notifPoll = null; }
    this.unread = 0;
    this.notifications = [];
    this.closeAll();
    this.router.navigate(['/Welcome']);
  }

  private closeAll(): void {
    this.open = false;
    this.openForProfile = false;
    this.openForNotification = false;
  }

  // A click anywhere else on the page closes any open menu. Each toggle below
  // stops the event from reaching the document so opening a menu doesn't
  // immediately re-close it.
  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeAll();
  }

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    const next = !this.open;
    this.closeAll();
    this.open = next;
  }

  toggleDropdownProfile(event: Event): void {
    event.stopPropagation();
    const next = !this.openForProfile;
    this.closeAll();
    this.openForProfile = next;
  }

  toggleDropdownNotification(event: Event): void {
    event.stopPropagation();
    const next = !this.openForNotification;
    this.closeAll();
    this.openForNotification = next;
    if (next) { this.loadNotifications(); }
  }

  closeDropdown(): void { this.open = false; }
  closeDropdownProfile(): void { this.openForProfile = false; }
  closeDropdownNotification(): void { this.openForNotification = false; }
}
