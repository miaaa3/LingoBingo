import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { LocalService } from 'src/app/Services/Auth/local.service';

@Component({ selector: 'app-gradebook', templateUrl: './gradebook.component.html', styleUrls: ['./gradebook.component.css'] })
export class GradebookComponent implements OnInit {
  results: any[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private quizService: QuizService,
    private local: LocalService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Gradebook is teacher-only. Students are sent back to the quizzes page.
    const role = this.local.getData('role');
    if (role !== 'TEACHER' && role !== 'ADMIN') {
      this.router.navigate(['/Quizzes']);
      return;
    }

    const quizId = this.route.snapshot.paramMap.get('id');
    if (quizId) {
      this.quizService.getQuizResults(quizId).subscribe(
        r => { this.results = r || []; this.loading = false; },
        () => { this.loading = false; }
      );
    }
  }
}
