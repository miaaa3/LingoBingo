import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from 'src/app/Services/Game/game.service';

/** Player entry point: enter a game code + a display name, then go to the play screen. */
@Component({
  selector: 'app-join-game',
  templateUrl: './join-game.component.html',
  styleUrls: ['./join-game.component.css']
})
export class JoinGameComponent implements OnInit {
  code = '';
  name = '';
  joining = false;
  error = '';

  constructor(private route: ActivatedRoute, private router: Router, private game: GameService) {}

  ngOnInit(): void {
    const c = this.route.snapshot.queryParamMap.get('code');
    if (c) this.code = c.trim().toUpperCase();
  }

  join(): void {
    const code = (this.code || '').trim();
    const name = (this.name || '').trim();
    if (!code) { this.error = 'Enter the game code.'; return; }
    if (!name) { this.error = 'Pick a name to play.'; return; }
    this.error = '';
    this.joining = true;
    this.game.joinGame(code, name, '').subscribe({
      next: (player: any) => {
        this.joining = false;
        try {
          sessionStorage.setItem('game_playerId', String(player?.id ?? ''));
          sessionStorage.setItem('game_name', name);
        } catch {}
        this.router.navigate(['/game', code, 'play']);
      },
      error: () => { this.joining = false; this.error = 'Could not join — check the code and try again.'; },
    });
  }
}
