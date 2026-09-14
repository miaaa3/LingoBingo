import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { GameService } from 'src/app/Services/Game/game.service';

/**
 * Player play screen for the synchronized game. Polls shared state; when the
 * host/first-correct answer advances the question, every player's screen moves
 * on together. Tapping an option submits it; the first correct player scores.
 */
@Component({
  selector: 'app-display-game',
  templateUrl: './display-game.component.html',
  styleUrls: ['./display-game.component.css']
})
export class DisplayGameComponent implements OnInit, OnDestroy {
  gameCode = '';
  gameId: number | null = null;
  playerId: number | null = null;
  myName = '';
  state: any = null;

  status: 'idle' | 'correct' | 'wrong' | 'tooLate' = 'idle';
  selected: string | null = null;
  private answeredIndex = -1;
  private poll: any = null;

  constructor(private route: ActivatedRoute, private game: GameService) {}

  ngOnInit(): void {
    this.gameCode = this.route.snapshot.paramMap.get('gameCode') || '';
    try {
      const pid = sessionStorage.getItem('game_playerId');
      this.playerId = pid ? Number(pid) : null;
      this.myName = sessionStorage.getItem('game_name') || '';
    } catch {}
    this.game.getGameByCode(this.gameCode).subscribe({ next: g => { this.gameId = (g as any)?.id ?? null; } });
    this.refresh();
    this.poll = setInterval(() => this.refresh(), 1200);
  }

  ngOnDestroy(): void { if (this.poll) clearInterval(this.poll); }

  private refresh(): void {
    this.game.getState(this.gameCode).subscribe({
      next: s => {
        this.state = s;
        // New question -> reset my answer state so everyone advances together.
        if (s?.phase === 'ACTIVE' && s.currentQuestionIndex !== this.answeredIndex) {
          this.status = 'idle';
          this.selected = null;
        }
      },
      error: () => {},
    });
  }

  answer(opt: string): void {
    if (this.status !== 'idle' || this.gameId == null || this.playerId == null) return;
    this.selected = opt;
    this.answeredIndex = this.state.currentQuestionIndex;
    this.game.submitAnswer(this.gameId, this.playerId, this.state.currentQuestionIndex, opt).subscribe({
      next: res => {
        this.status = res?.correct ? 'correct' : (res?.status === 'tooLate' ? 'tooLate' : 'wrong');
        this.refresh();
      },
      error: () => { this.status = 'idle'; },
    });
  }

  get phase(): string { return this.state?.phase || 'WAITING'; }
  get players(): any[] { return this.state?.players || []; }
  get myRank(): number {
    return this.players.findIndex(p => p.id === this.playerId) + 1;
  }
  get myScore(): number {
    const me = this.players.find(p => p.id === this.playerId);
    return me ? me.score : 0;
  }
  optLetter(i: number): string { return ['A', 'B', 'C', 'D', 'E', 'F'][i] || '?'; }
}
