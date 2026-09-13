import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { ToastrService } from 'ngx-toastr';

@Component({ selector: 'app-take-quiz', templateUrl: './take-quiz.component.html', styleUrls: ['./take-quiz.component.css'] })
export class TakeQuizComponent implements OnInit {
  quiz: any = null;
  loading = true;
  submitting = false;
  answers: { [key: string]: string } = {};
  result: any = null;

  constructor(private route: ActivatedRoute, private quizService: QuizService, private toastr: ToastrService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.quizService.getQuizById(+id).subscribe(
        (res: any) => { this.quiz = res; this.loading = false; },
        () => { this.loading = false; }
      );
    }
  }

  select(qid: number, key: string): void { this.answers[qid] = key; }

  allAnswered(): boolean {
    return this.quiz && this.quiz.questions && this.quiz.questions.every((q: any) => this.answers[q.questionId]);
  }

  submit(): void {
    if (!this.quiz) { return; }
    this.submitting = true;
    this.quizService.submitQuiz(this.quiz.id, this.answers).subscribe(
      (res: any) => { this.result = res; this.submitting = false; },
      () => { this.submitting = false; this.toastr.error('Could not submit quiz', 'Error'); }
    );
  }
}
