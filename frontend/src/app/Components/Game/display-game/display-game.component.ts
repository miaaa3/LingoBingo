import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { GameService } from 'src/app/Services/Game/game.service';
import { Game } from 'src/app/Models/game/Game';
import { QuestionDTO } from 'src/app/Models/game/QuestionDTO';
import { Router } from '@angular/router';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { User } from 'src/app/Models/user.model';

@Component({
  selector: 'app-display-game',
  templateUrl: './display-game.component.html',
  styleUrls: ['./display-game.component.css'],
})
export class DisplayGameComponent implements OnInit {
  gameCode!: string;
  game: Game | null = null;
  questions: QuestionDTO[] = [];
  currentQuestionIndex: number = 0;
  correctAnswersCount: number = 0;
  isTeacher:boolean = false;
  userId='';
  createdBy: User | null = null;


  // Track answer correctness
  isAnswerCorrect: boolean | null = null;
  selectedAnswer: string | null = null;
  correctAnswer: string | null = null;

  // Letters for answer options display
  letters = ['A', 'B', 'C', 'D'];

  constructor(
    private route: ActivatedRoute,
    private gameService: GameService,
    private cdr: ChangeDetectorRef,
    private router: Router, // Inject Router for navigation
    local:LocalService

  ) {
    
    this.userId=local.getData('userid');

  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {      
      this.gameCode = params['gameCode'];
      this.getcreatedByGameCode()

      this.getGameById();
    });
    setTimeout(() => {
    
    this.checkIfTeacher();
    console.log(this.isTeacher);
    
      if (this.isTeacher) {
        
        this.router.navigate(['/leaderboard',this.gameCode])
      }
   
    }, 1500);
    
    
  }

  getGameById() {
    this.gameService.getGameByCode(this.gameCode).subscribe(
      (game: Game) => {
        this.game = game;
        this.fetchGameQuestions();
      },
      (error) => {
        console.error('Error fetching game by ID:', error);
      }
    );
  }

  getcreatedByGameCode(){
    this.gameService.getCreatedByByCode(this.gameCode).subscribe(
      (user: User) => {
        this.createdBy = user;
        console.log(this.createdBy);
        

      },
      (error) => {
        console.error('Error fetching user by gamecode:', error);
      }
    );
  }

  fetchGameQuestions() {
    this.gameService.getGameQuestions(this.gameCode).subscribe(
      (questions: QuestionDTO[]) => {
        this.questions = questions;
        // Initialize userAnswer to null for each question
        this.questions.forEach((question) => {
          question.userAnswer = null;
        });
      },
      (error) => {
        console.error('Error fetching game questions:', error);
      }
    );
  }

  submitAnswer() {
    const selectedAnswer = this.questions[this.currentQuestionIndex].userAnswer;

    if (selectedAnswer) {
      this.selectedAnswer = selectedAnswer;
      this.correctAnswer = this.questions[this.currentQuestionIndex].correctAnswer;
      const isCorrect = this.checkAnswer(selectedAnswer, this.correctAnswer);

      // Update the isAnswerCorrect flag based on the user's answer
      this.isAnswerCorrect = isCorrect;

      // Increment score if answer is correct
      if (isCorrect) {
        this.correctAnswersCount++;
        
        // Call backend to update the score
        const playerId = +localStorage.getItem('playerId')!; // Get playerId from localStorage

        console.log(this.game!.id+" "+ playerId);
        
        this.gameService.updateScore(this.game!.id, playerId).subscribe({
          next: (response) => {
            console.log('Score updated successfully:', response);
          },
          error: (err) => {
            console.error('Error updating score:', err);
          }
        });
      }

      // If the answer is incorrect, wait for 5 seconds before going to the next question
      if (!isCorrect) {
        setTimeout(() => {
          this.moveToNextQuestion();
        }, 5000); // Wait for 5 seconds before moving to the next question
      } else {
        this.moveToNextQuestion(); // Move to the next question immediately if the answer is correct
      }

      // Check if all questions are answered (i.e., game finished)
      if (this.currentQuestionIndex >= this.questions.length) {
        this.endGame(); // End the game and redirect to leaderboard
      }
    }
  }

  moveToNextQuestion() {
    // Move to the next question
    this.currentQuestionIndex++;

    // If there are more questions, reset userAnswer for the next question
    if (this.currentQuestionIndex < this.questions.length) {
      this.questions[this.currentQuestionIndex].userAnswer = null;
    }

    // Reset the answer-related flags
    this.isAnswerCorrect = null;
    this.selectedAnswer = null;
    this.correctAnswer = null;

    // Manually trigger change detection
    this.cdr.detectChanges();
  }

  endGame() {
    // Call the backend to end the game
    this.gameService.endGame(this.game!.id).subscribe({
      next: () => {
        console.log('Game ended successfully');
        // Redirect to leaderboard page
        this.router.navigate(['/leaderboard', this.gameCode]);
      },
      error: (err) => {
        console.error('Error ending the game:', err);
      }
    });
  }

  checkAnswer(selectedAnswer: string | null, correctAnswer: string): boolean {
    return selectedAnswer === correctAnswer;
  }

  getScore(): string {
    const totalQuestions = this.questions.length;
    const scorePercentage = (this.correctAnswersCount / totalQuestions) * 100;
    return `${this.correctAnswersCount} out of ${totalQuestions} correct (${scorePercentage.toFixed(2)}%)`;
  }

  isQuizGame(): boolean {
    return this.game?.quiz != null;
  }

  isFlashcardGame(): boolean {
    return this.game?.flashcardSet != null;
  }

  // Helper function to style the answers based on correctness
  getAnswerClass(answer: string): string {
    if (this.isAnswerCorrect === null) return ''; // No answer selected yet

    if (this.isAnswerCorrect && answer === this.correctAnswer) {
      return 'bg-green-500'; // Correct answer: green background
    } else if (this.isAnswerCorrect === false && answer === this.selectedAnswer) {
      return 'bg-red-500'; // Incorrect answer: red background
    } else if (answer === this.correctAnswer) {
      return 'bg-green-500'; // Correct answer: green background
    }
    return ''; // Default background if no conditions match
  }



  checkIfTeacher() {
    console.log(this.userId);
    
    if (Number(this.userId) === this.createdBy.id) {
      this.isTeacher = this.userId === String(this.createdBy.id); // Set isTeacher to true if the authenticated user is the teacher
    }
  }
}
