import { Component, OnInit } from '@angular/core';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { LocalService } from 'src/app/Services/Auth/local.service';

@Component({ selector: 'app-quiz-hub', templateUrl: './quiz-hub.component.html', styleUrls: ['./quiz-hub.component.css'] })
export class QuizHubComponent implements OnInit {
  isTeacher = false;
  loading = false;
  quizzes: any[] = [];
  myResults: any[] = [];

  constructor(private quizService: QuizService, private local: LocalService) {}

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
}
