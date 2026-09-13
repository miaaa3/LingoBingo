import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Subscription, timer } from 'rxjs';
import { Player } from 'src/app/Models/game/Player';
import { GameService } from 'src/app/Services/Game/game.service';

@Component({
  selector: 'app-leaderboard',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.css']
})
export class LeaderboardComponent implements OnInit, OnDestroy {
  joinedPlayers: Player[] = [];
  gameCode!: string;
  gameId!: number;
  playerId!: number;
  gameEnded = false;
  finalLeaderboard: any[] = [];

  // Timer to periodically poll the server for updated game status
  pollingSubscription!: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private gameService: GameService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.gameCode = params['gameCode'];
      
      this.playerId = params['playerId'];
      this.loadGameDetails();

      setTimeout(() => {
        this.startPolling();
      }, 1000);
      
    });
  }

  ngOnDestroy(): void {
    if (this.pollingSubscription) this.pollingSubscription.unsubscribe();
  }

  loadGameDetails() {
    this.gameService.getGameByCode(this.gameCode).subscribe({
      next: (game) => {
        this.gameId = game.id;
        
      },
      error: (err) => {
        this.toastr.error('Failed to load game details');
        console.error('Game details error:', err);
      },
    });
  }

  startPolling() {
    // Poll every 5 seconds to get player progress in real-time
    this.pollingSubscription = timer(0, 5000).subscribe(() => {
      
      this.gameService.getPlayersForGame(this.gameId).subscribe({
        next: (players) => {
          this.joinedPlayers = players;
          this.joinedPlayers.sort((a, b) => (b.score ?? 0) - (a.score ?? 0));
          if (this.checkIfGameEnded()) {
            this.gameEnded = true;
            this.finalLeaderboard = this.generateLeaderboard();
          }
        },
        error: (err) => {
          console.error('Failed to fetch players:', err);
        },
      });
    });
  }
  

  checkIfGameEnded(): boolean {
    // Check if all players have finished (assuming players have a 'finished' property)
    return this.joinedPlayers.every((player) => player.finished); // Assuming `finished` flag exists in player object
  }

  generateLeaderboard() {
    return this.joinedPlayers
      .map((player, index) => ({
        rank: index + 1,
        name: player.username,
        score: player.score // Assuming the score is part of the player object
      }))
      .sort((a, b) => b.score - a.score); // Sort leaderboard by score (highest first)
  }

  // Helper function to get player status (or score)
  getPlayerScore(playerId: number): number {
    const player = this.joinedPlayers.find((p) => p.id === playerId);
    return player ? player.score : 0;
  }

  // Navigate to the leaderboard page once game ends
  navigateToGamePlay() {
    if (this.gameEnded) {
      this.router.navigate(['/leaderboard', this.gameCode]);
    }
  }
}
