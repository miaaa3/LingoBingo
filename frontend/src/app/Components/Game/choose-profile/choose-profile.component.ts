import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from 'src/app/Services/Game/game.service';

@Component({
  selector: 'app-choose-profile',
  templateUrl: './choose-profile.component.html',
  styleUrls: ['./choose-profile.component.css']
})
export class ChooseProfileComponent implements OnInit {
  gameCode!: string;
  username: string = '';
  selectedAvatar: string = '';
  showError: boolean = false;
  isLoading: boolean = false;
  avatars: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gameService: GameService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.gameCode = params['gameCode'];
    });
    this.generateAvatars();
  }

  generateAvatars() {
    const baseUrl = 'https://api.dicebear.com/6.x/adventurer/svg?seed=';
    for (let i = 0; i < 5; i++) {
      const seed = Math.random().toString(36).substring(2, 15);
      this.avatars.push(`${baseUrl}${seed}`);
    }
  }

  selectAvatar(avatar: string) {
    this.selectedAvatar = avatar;
  }

  confirm() {
    if (!this.username.trim() || !this.selectedAvatar) {
      this.showError = true;
      return;
    }

    this.showError = false;
    this.isLoading = true;
    this.gameService.joinGame(this.gameCode, this.username, this.selectedAvatar).subscribe({
  next: (response: any) => {
    console.log('Join game response:', response);  // Log response here
    this.isLoading = false;
    
    const playerId = response?.id || response?.playerId || null;

    if (playerId) {
      localStorage.setItem('playerId', playerId.toString());
      this.router.navigate(['/GameLobby', this.gameCode], { queryParams: { playerId } });
    } else {
      console.error('Player ID not found in response.');
    }
  },
  error: (err) => {
    this.isLoading = false;
    this.showError = true;
    alert('Failed to join the game. Please check the code and try again.');
    console.error('Join game error:', err);
  }
});
  }}