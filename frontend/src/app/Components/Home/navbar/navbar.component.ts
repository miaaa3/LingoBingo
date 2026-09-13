import { Component, OnInit } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { BehaviorSubject, from, Observable } from 'rxjs';


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
export class NavbarComponent {
  zIndex = 1000;
  open = false;

  zIndexForProfile = 1000;
  openForProfile = false;
  openForNotification = false;
  zIndexForNotification = 100;
  isUser: BehaviorSubject<boolean>;

  constructor(private local: LocalService) {
    this.isUser = new BehaviorSubject<boolean>(this.local.getData("userApiKey2") !== "");
  }

  

  logout(): void {
    this.local.removeData("userApiKey2");   
    this.local.removeData("userid");     
  
    this.isUser.next(false); 
  }
  toggleDropdown(): void {
    this.open = !this.open;
  }

  closeDropdown(): void {
    this.open = false;
  }

  toggleDropdownProfile(): void {
    this.openForProfile = !this.openForProfile; 
  }
  toggleDropdownNotification(): void {
    this.openForNotification = !this.openForNotification; 
  }
  closeDropdownNotification(): void {
    this.openForNotification = false; 
  }

  closeDropdownProfile(): void {
    this.openForProfile = false; 
  }
}
