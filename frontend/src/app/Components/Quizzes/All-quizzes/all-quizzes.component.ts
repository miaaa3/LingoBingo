import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz } from 'src/app/Models/quiz/quiz.model';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';

@Component({
  selector: 'app-all-quizzes',
  templateUrl: './all-quizzes.component.html',
  styleUrls: ['./all-quizzes.component.css']
})
export class AllQuizzesComponent implements OnInit{
  quizCategories: string[];
  quizzes: Quiz[] = [];
  isLoading:boolean = true;

  constructor(
    private quizService: QuizService,
    private router:Router
      ) {
//    this.quizCategories = getQuizCategories();
  }
  async ngOnInit(): Promise<void> {
    this.quizzes = await this.quizService.getAllQuizzes().toPromise();
    this.isLoading = false;
  }

  navigateToQuiz(){
    this.router.navigate(['/Display-quiz/:id'])
  }

  
}
