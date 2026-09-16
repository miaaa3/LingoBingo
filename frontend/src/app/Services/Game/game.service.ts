import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../Environments/environment';
import { Game } from '../../Models/game/Game';
import { Player } from '../../Models/game/Player';
import { QuestionDTO } from 'src/app/Models/game/QuestionDTO';
import { User } from 'src/app/Models/user.model';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private apiUrl = environment.apiUrl + '/game';

  constructor(private http: HttpClient) {}

  /**
   * Create a new game
   * POST /api/game/create
   * Accepts optional flashcardSetId and quizId parameters
   */
  createGame(
    questionType: string,
    numberOfQuestions: number,
    flashcardSetId?: number,
    quizId?: number
  ): Observable<Game> {
    let params = new HttpParams()
      .set('questionType', questionType)
      .set('numberOfQuestions', numberOfQuestions.toString());

    if (flashcardSetId != null) {
      params = params.set('flashcardSetId', flashcardSetId.toString());
    }
    if (quizId != null) {
      params = params.set('quizId', quizId.toString());
    }

    return this.http.post<Game>(`${this.apiUrl}/create`, null, { params });
  }

  /**
   * Player joins a game using gameCode and username
   * POST /api/game/join
   */
 joinGame(gameCode: string, username: string, avatarUrl: string): Observable<Player> {
  const params = new HttpParams()
    .set('gameCode', gameCode)
    .set('username', username)
    .set('avatarUrl', avatarUrl);

  return this.http.post<Player>(`${this.apiUrl}/join`, null, { params });
}



  /**
   * Start the game
   * POST /api/game/start/{gameId}
   */
  startGame(gameId: number): Observable<string> {
  return this.http.post(`${this.apiUrl}/start/${gameId}`, null, { responseType: 'text' });
}


  /**
   * Check answer for a question or flashcard in the game
   * POST /api/game/check/{gameId}
   */
  checkAnswer(
    gameId: number,
    questionId: number,
    answer: string,
    playerId: number
  ): Observable<string> {
    const params = new HttpParams()
      .set('questionId', questionId.toString())
      .set('answer', answer)
      .set('playerId', playerId.toString());

    return this.http.post<string>(`${this.apiUrl}/check/${gameId}`, null, { params });
  }

  /**
   * Mark game as finished for a player
   * POST /api/game/finish/{gameId}/{playerId}
   */
  finishGame(gameId: number, playerId: number): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/finish/${gameId}/${playerId}`, null);
  }

  /**
   * End the game and get the leaderboard
   * POST /api/game/end/{gameId}
   */
  endGame(gameId: number): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/end/${gameId}`, null);
  }

  /**
   * Get all active games
   * GET /api/game/active
   */
  getActiveGames(): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.apiUrl}/active`);
  }

  /**
   * Get game details by game code
   * GET /api/game/byCode/{gameCode}
   */
  getGameByCode(gameCode: string): Observable<Game> {
    return this.http.get<Game>(`${this.apiUrl}/byCode/${gameCode}`);
  }

   /**
   * Get game details by game code
   * GET /api/game/byCode/{gameCode}
   */
   getCreatedByByCode(gameCode: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/createdBy/${gameCode}`);
  }

  /**
   * Get players in a specific game
   * GET /api/game/players/{gameId}
   */
  getPlayersForGame(gameId: number): Observable<Player[]> {
    return this.http.get<Player[]>(`${this.apiUrl}/players/${gameId}`);
  }

  /**
   * Get the QR code image (PNG) for the game
   * GET /api/game/{gameCode}/qrcode
   */
  getGameQRCode(gameCode: string): Observable<Blob> {
    // Pass the address this page is open on so the QR points somewhere the phone can reach.
    const base = encodeURIComponent(window.location.origin);
    return this.http.get(`${this.apiUrl}/${gameCode}/qrcode?base=${base}`, { responseType: 'blob' });
  }

   // Fetch game questions based on the gameCode
  getGameQuestions(gameCode: string): Observable<QuestionDTO[]> {
    return this.http.get<QuestionDTO[]>(`${this.apiUrl}/game-questions/${gameCode}`);
  }

  updateScore(gameId: number, playerId: number): Observable<any> {
    const params = new HttpParams()
      .set('playerId', playerId)

    return this.http.post(`${this.apiUrl}/updateScore/${gameId}`, null, { params });
  }

  // ---- Synchronized (Kahoot-style) live game ----

  /** Poll the shared game state (~1s). */
  getState(gameCode: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${gameCode}/state`);
  }

  /** Submit an answer for the current question. First correct wins & advances everyone. */
  submitAnswer(gameId: number, playerId: number, questionIndex: number, answer: string): Observable<any> {
    const params = new HttpParams()
      .set('playerId', playerId.toString())
      .set('questionIndex', questionIndex.toString())
      .set('answer', answer);
    return this.http.post<any>(`${this.apiUrl}/${gameId}/answer`, null, { params });
  }

  /** Host forces the next question. */
  nextQuestion(gameId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${gameId}/next`, null);
  }
}
