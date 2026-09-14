import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { GameService } from 'src/app/Services/Game/game.service';

/**
 * Host screen for a synchronized live game. One component drives all three
 * phases by polling the shared state: WAITING (lobby with QR + players),
 * ACTIVE (current question + live board + Next control), ENDED (final board).
 */
@Component({
  selector: 'app-game-lobby',
  templateUrl: './game-lobby.component.html',
  styleUrls: ['./game-lobby.component.css']
})
export class GameLobbyComponent implements OnInit, OnDestroy {
  gameCode = '';
  gameId: number | null = null;
  state: any = null;
  qrUrl: string | null = null;
  joinUrl = '';
  starting = false;
  private poll: any = null;

  constructor(private route: ActivatedRoute, private game: GameService) {}

  ngOnInit(): void {
    this.gameCode = this.route.snapshot.paramMap.get('gameCode') || '';
    this.joinUrl = `${window.location.origin}/Join?code=${this.gameCode}`;

    this.game.getGameByCode(this.gameCode).subscribe({
      next: g => { this.gameId = (g as any)?.id ?? null; },
    });
    this.game.getGameQRCode(this.gameCode).subscribe({
      next: blob => { this.qrUrl = URL.createObjectURL(blob); },
      error: () => {},
    });

    this.refresh();
    this.poll = setInterval(() => this.refresh(), 1200);
  }

  ngOnDestroy(): void {
    if (this.poll) clearInterval(this.poll);
    if (this.qrUrl) URL.revokeObjectURL(this.qrUrl);
  }

  private refresh(): void {
    this.game.getState(this.gameCode).subscribe({ next: s => { this.state = s; }, error: () => {} });
  }

  start(): void {
    if (this.gameId == null) return;
    this.starting = true;
    this.game.startGame(this.gameId).subscribe({
      next: () => { this.starting = false; this.refresh(); },
      error: () => { this.starting = false; },
    });
  }

  next(): void {
    if (this.gameId == null) return;
    this.game.nextQuestion(this.gameId).subscribe({ next: () => this.refresh() });
  }

  get players(): any[] { return this.state?.players || []; }
  get phase(): string { return this.state?.phase || 'WAITING'; }
  get questionNumber(): number { return (this.state?.currentQuestionIndex ?? 0) + 1; }
}
