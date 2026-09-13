import { Component, OnInit } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Question } from './Models/quiz/question.model';
import { GenerateQuizService } from './Services/Quiz/generate-quiz.service';
import { LocalService } from './Services/Auth/local.service';
import { QuizApiService } from './Services/Quiz/quiz-api.service';
import { RestApiService } from './Services/Auth/rest-api.service';
import { HttpHeaders } from '@angular/common/http';
import { AuthenticationService } from './Services/Auth/authentication.service';
import { initFlowbite } from 'flowbite';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  questions: Question[] = [];
  questionsByCategory: Question[] = [];
  questionsByDifficulty: Question[] = [];
  testQuiz: Question[] = [];
  quizzesByCategory : Question[]=[] 
  
  title = 'QuizIt';

  constructor(
    private test:QuizApiService,
     private generateQuiz : GenerateQuizService,
     private router: Router,
     private authService : AuthenticationService,
     private local:LocalService
    ) {}
  
  shouldDisplay(): boolean {
    const currentRoute = this.router.url;
    let endpoints = ['/Create-quiz', ];
    return !endpoints.some(endpoint => currentRoute.includes(endpoint));
  }

  onlyOneComponent(){
    const currentRoute = this.router.url;
    const endpoints = [ '/Login', '/Register', 'Reset-password','Forgot-password', 'dashboard'];
    return endpoints.some(endpoint => currentRoute.includes(endpoint));
  }

  shouldHideSidebar(): boolean {
    return !this.local.getData("userApiKey");
  }

  ngOnInit(): void {
    initFlowbite();


    // this.loadQuestions()

    // console.warn("after : "+this.local.getData("userApiKey"))
    // this.api.httpOptions = {
    //   headers: new HttpHeaders({
    //     'Content-Type': 'application/json',
    //     Accept: 'application/json',
    //     Authorization: 'Bearer ' + this.local.getData("userApiKey")
    //   })
    // };
    // this.api.profile().subscribe(
    //   res => {
    //     if(res['id']==null){
    //       this.local.removeData("userApiKey")
    //     }
    //   },
    //   err => {
    //     this.local.removeData("userApiKey")
    //   }
    // );
  }

  loadQuestions(): void {
    // this.test.getQuestions().subscribe(
    //   (data) => {
    //     this.questions = data;
    //     console.warn(this.questions)
    //   },
    //   (error) => {
    //     console.error('Error loading questions:', error);
    //   }
    // );
    // this.test.getQuestionsByCategory('Linux').subscribe(
    //   (data) => {
    //     this.questionsByCategory = data;
    //     console.warn(this.questionsByCategory)
    //   },
    //   (error) => {
    //     console.error('Error loading questions:', error);
    //   }
    // );
    // this.test.getQuestionsByDifficulty(Difficulty.Medium).subscribe(
    //   (data) => {
    //     this.questionsByDifficulty = data;
    //     console.warn(this.questionsByDifficulty)
    //   },
    //   (error) => {
    //     console.error('Error loading questions:', error);
    //   }
    // )

    // this.generateQuiz.generateQuizzes().subscribe(data => {
    //   this.testQuiz = data;
    //   console.warn(this.testQuiz)
    // });
    //this.generateQuiz.generateQuizzesForCategory(Category.CMS).subscribe(data => {
      //this.quizzesByCategory = data;
      //console.warn(this.quizzesByCategory)
    //});
  }

 
  
}
