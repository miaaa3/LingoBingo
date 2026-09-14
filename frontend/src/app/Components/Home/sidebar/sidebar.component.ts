import { Component } from '@angular/core';
import { CurrentUserService } from 'src/app/Services/Auth/current-user.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  username: string;
  constructor(private currentUser: CurrentUserService) {
    this.username = this.currentUser.name;
  }
}
