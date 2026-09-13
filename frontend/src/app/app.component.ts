import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LocalService } from './Services/Auth/local.service';
import { ThemeService } from './Services/Theme/theme.service';
import { initFlowbite } from 'flowbite';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'LingoBingo';

  constructor(private router: Router, private local: LocalService, private theme: ThemeService) {}

  shouldDisplay(): boolean {
    const currentRoute = this.router.url;
    const endpoints = ['/Create-quiz'];
    return !endpoints.some(endpoint => currentRoute.includes(endpoint));
  }

  onlyOneComponent(): boolean {
    const currentRoute = this.router.url;
    const endpoints = ['/Login', '/Register', 'Reset-password', 'Forgot-password', 'dashboard'];
    return endpoints.some(endpoint => currentRoute.includes(endpoint));
  }

  shouldHideSidebar(): boolean {
    return !this.local.getData('userApiKey2');
  }

  ngOnInit(): void {
    this.theme.init();
    initFlowbite();
  }
}
