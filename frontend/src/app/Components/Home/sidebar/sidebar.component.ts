import { Component, OnInit, OnDestroy } from '@angular/core';
import { CurrentUserService } from 'src/app/Services/Auth/current-user.service';
import { MessageService } from 'src/app/Services/Messaging/message.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {
  username: string;
  msgUnread = 0;
  private poll: any = null;

  constructor(private currentUser: CurrentUserService, private messages: MessageService) {
    this.username = this.currentUser.name;
  }

  ngOnInit(): void {
    this.refreshUnread();
    this.poll = setInterval(() => this.refreshUnread(), 30000);
  }

  ngOnDestroy(): void {
    if (this.poll) { clearInterval(this.poll); }
  }

  private refreshUnread(): void {
    this.messages.unreadCount().subscribe({
      next: r => (this.msgUnread = r?.count || 0),
      error: () => {}
    });
  }
}
