import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LocalService } from 'src/app/Services/Auth/local.service';

/**
 * Public landing page shown to visitors who are not signed in. Authenticated
 * users are redirected straight to their dashboard, so this never gets in the
 * way of returning users.
 */
@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit {
  joinCode = '';

  constructor(private router: Router, private local: LocalService) {}

  ngOnInit(): void {
    const token = this.local.getData('userApiKey2');
    if (token) { this.router.navigate(['/Dashboard']); }
  }

  join(): void {
    const code = (this.joinCode || '').trim();
    this.router.navigate(['/Join'], code ? { queryParams: { code } } : {});
  }
}
