import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';

@Component({ selector: 'app-gradebook', templateUrl: './gradebook.component.html', styleUrls: ['./gradebook.component.css'] })
export class GradebookComponent implements OnInit {
  results: any[] = [];
  loading = true;

  constructor(private route: ActivatedRoute, private quizService: QuizService) {}

  ngOnInit(): void {
    const quizId = this.route.snapshot.paramMap.get('id');
    if (quizId) {
      this.quizService.getQuizResults(quizId).subscribe(
        r => { this.results = r || []; this.loading = false; },
        () => { this.loading = false; }
      );
    }
  }
}
