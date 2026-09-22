import { Component, OnInit } from '@angular/core';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { LocalService } from 'src/app/Services/Auth/local.service';
import { GameService } from 'src/app/Services/Game/game.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({ selector: 'app-quiz-hub', templateUrl: './quiz-hub.component.html', styleUrls: ['./quiz-hub.component.css'] })
export class QuizHubComponent implements OnInit {
  isTeacher = false;
  loading = false;
  quizzes: any[] = [];
  myResults: any[] = [];

  constructor(private quizService: QuizService, private local: LocalService, private game: GameService, private router: Router, private toastr: ToastrService) {}

  hosting = false;

  hostGame(quiz: any): void {
    if (this.hosting) return;
    this.hosting = true;
    const n = Math.max(1, this.qCount(quiz) || 10);
    this.game.createGame('RANDOM', n, undefined, quiz.id).subscribe({
      next: (g: any) => { this.hosting = false; this.router.navigate(['/GameLobby', g.gameCode]); },
      error: () => { this.hosting = false; this.toastr.error('Could not start a live game.'); },
    });
  }

  ngOnInit(): void {
    const role = this.local.getData('role');
    this.isTeacher = role === 'TEACHER' || role === 'ADMIN';
    this.loading = true;
    this.quizService.getVisibleQuizzes().subscribe(
      (res: any) => {
        this.quizzes = this.isTeacher ? (res?.ownQuizzes || []) : (res?.publicQuizzes || []);
        this.loading = false;
      },
      () => { this.loading = false; }
    );
    if (!this.isTeacher) {
      this.quizService.getMyResults().subscribe(r => this.myResults = r || [], () => {});
    }
  }

  qCount(q: any): number { return q.questions ? q.questions.length : 0; }

  /** A quiz with a deadline in the past is closed. */
  isClosed(q: any): boolean {
    return !!q?.dueDate && new Date(q.dueDate).getTime() < Date.now();
  }
}
