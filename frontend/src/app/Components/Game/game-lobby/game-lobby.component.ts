import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { GameService } from 'src/app/Services/Game/game.service';
import { Subscription, timer } from 'rxjs';
import { Player } from 'src/app/Models/game/Player';
import { User } from 'src/app/Models/user.model'; // Assuming you have a User model
import { AuthenticationService } from 'src/app/Services/Auth/authentication.service';
import { Game } from 'src/app/Models/game/Game';
import { LocalService } from 'src/app/Services/Auth/local.service';

@Component({
  selector: 'app-game-lobby',
  templateUrl: './game-lobby.component.html',
  styleUrls: ['./game-lobby.component.css']
})
export class GameLobbyComponent implements OnInit, OnDestroy {
  joinedPlayers: Player[] = [];
  game: Game | null = null; // Store the game details
  gameCode!: string;
  gameId!: number;
  gameDetailsSubscription!: Subscription;
  playersSubscription!: Subscription;
  pollingSubscription!: Subscription;
  gameActivePollingSubscription!: Subscription;
  qrCodeUrl: string | null = null;
  playerId!: number;
  authenticatedUser: User | null = null;
  isTeacher: boolean = false ; // Flag to check if the user is the teacher
  userId='';

  constructor(
    private router: Router,
    local:LocalService,
    private route: ActivatedRoute,
    private gameService: GameService,
    private toastr: ToastrService,
    private authService: AuthenticationService  // Inject AuthenticationService
  ) {
    this.userId=local.getData('userid');

  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.gameCode = params['gameCode'];
      this.playerId = +params['playerId'];
      this.checkIfTeacher(); // Get playerId from URL queryParams
      this.loadGameDetails();
      this.loadQRCode();
      this.startPlayersPolling();
      setTimeout(() => {
      this.startGameActivePolling();
      }, 2000);
        // Check if the authenticated user is the teacher
    });
  }

  ngOnDestroy(): void {
    this.gameDetailsSubscription?.unsubscribe();
    this.playersSubscription?.unsubscribe();
    this.pollingSubscription?.unsubscribe();
    this.gameActivePollingSubscription?.unsubscribe();
  }

  loadGameDetails() {
    this.gameDetailsSubscription = this.gameService.getGameByCode(this.gameCode).subscribe({
      next: (game) => {
        this.game = game;
        this.gameId = game.id;
      },
      error: (err) => {
        this.toastr.error('Failed to load game details');
        console.error('Game details error:', err);
      }
    });
  }

  startPlayersPolling() {
    this.pollingSubscription = timer(0, 5000).subscribe(() => {
      if (this.gameId) {
        this.gameService.getPlayersForGame(this.gameId).subscribe({
          next: (players: Player[]) => {
            this.joinedPlayers = players;
          },
          error: (err) => {
            console.error('Failed to fetch players:', err);
          }
        });
      }
    });
  }

  loadQRCode(): void {
    this.gameService.getGameQRCode(this.gameCode).subscribe({
      next: (blob) => {
        this.qrCodeUrl = URL.createObjectURL(blob);
      },
      error: (err) => {
        console.error('Failed to load QR code', err);
      }
    });
  }

  startGameActivePolling() {
    this.gameActivePollingSubscription = timer(0, 3000).subscribe(() => {
      if (this.gameId) {
        this.gameService.getGameByCode(this.gameCode).subscribe({
          next: (game) => {
            if (game.isActive) {
              // Game started, navigate based on user role
              if (this.isTeacher) {
                this.router.navigate(['/leaderboard', this.gameCode]);
              } else {
                this.router.navigate(['/game', this.gameCode, 'play'], { queryParams: { playerId: this.playerId } });
              }
              this.gameActivePollingSubscription.unsubscribe();
            }
          },
          error: (err) => {
            console.error('Failed to check game active status:', err);
          }
        });
      }
    });
  }

  startGame() {
    if (this.joinedPlayers.length >= 2) {
      this.gameService.startGame(this.gameId).subscribe({
        next: () => {
          this.toastr.success('Game started!');
        },
        error: (err) => {
          this.toastr.error('Failed to start the game.');
          console.error('Start game error:', err);
        }
      });
    } else {
      this.toastr.warning('At least 2 players are required to start the game.');
    }
  }

  checkIfTeacher() {
    if (Number(this.userId) === this.game?.createdBy?.id) {
      this.isTeacher = String(this.userId) === String(this.game?.createdBy?.id); // Set isTeacher to true if the authenticated user is the teacher
    }
  }
}
