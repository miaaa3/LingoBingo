import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { GameService } from 'src/app/Services/Game/game.service'; // Import your service

@Component({
  selector: 'app-join-game',
  templateUrl: './join-game.component.html',
  styleUrls: ['./join-game.component.css']
})
export class JoinGameComponent {
  gameCode: string = '';

  constructor(private router: Router) {}

  proceed() {
    if (this.gameCode.trim()) {
      this.router.navigate(['/choose-profile', this.gameCode.trim()]);
    }
  }
}
