import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { Question } from 'src/app/Models/quiz/question.model';
import { Quiz } from 'src/app/Models/quiz/quiz.model';

@Component({
  selector: 'app-display-quiz',
  templateUrl: './display-quiz.component.html',
  styleUrls: ['./display-quiz.component.css'],
})
export class DisplayQuizComponent implements OnInit {
  id!: number;
  quiz: Quiz = new Quiz();
  questions: Question[] = [];
  currentQuestionIndex: number = 0;
  correctAnswersCount: number = 0;

  constructor(private route: ActivatedRoute, private quizService: QuizService) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.id = params['id'];
      this.getQuizById();
    });
  }

  getQuizById() {
    this.quizService.getQuizById(this.id).subscribe(
      (quiz: Quiz) => {
        this.quiz = quiz;
        this.questions = quiz.questions.map((question) => ({
          ...question,
          userAnswer: null, // Initialize userAnswer for each question
        }));
      },
      (error) => {
        console.error('Error fetching quiz by ID:', error);
      }
    );
  }

  submitAnswer() {
    const selectedAnswer = this.questions[this.currentQuestionIndex].userAnswer;


    // Check if the selected answer is correct
    if(selectedAnswer){
          const isCorrect = this.checkAnswer(selectedAnswer, this.questions[this.currentQuestionIndex].correctAnswer);

          // Update the correctAnswersCount
          if (isCorrect) {
            this.correctAnswersCount++;
            }
          // Add your logic based on whether the answer is correct
          if (isCorrect) {
            console.log('Correct answer!');
            // Add any logic you want for a correct answer
          } else {
            console.log('Incorrect answer!');
            // Add any logic you want for an incorrect answer
          }

          // Move to the next question
          this.currentQuestionIndex++;

          // Reset userAnswer for the next question
          if (this.currentQuestionIndex < this.questions.length) {
            this.questions[this.currentQuestionIndex].userAnswer = null;
          }
    }
    
  }

  checkAnswer(selectedAnswer: string | null, correctAnswer: string | undefined): boolean {
    if (selectedAnswer && correctAnswer) {
      return selectedAnswer === correctAnswer;
    }
    return false;
  }

    
  // getBackgroundColor(category: string): string {
  //   const bgColors: Record<string, string> = any;
  //   const defaultColor = "#ffffff";
    
  //   const categoryLower = category.toLowerCase();
  
  //   for (const key in bgColors) {
  //     if (key.toLowerCase() === categoryLower) {
  //       return bgColors[key];
  //     }
  //   }
  
  //   return defaultColor;
  // }
  
  // nextQuestion() {
  //   // Move to the next question
  //   this.currentQuestionIndex++;

  //   // Reset userAnswer for the next question
  //   if (this.currentQuestionIndex < this.questions.length) {
  //     this.questions[this.currentQuestionIndex].userAnswer = null;
  //   }
  // }

  getScore(): string {
    const totalQuestions = this.questions.length;
    const scorePercentage = (this.correctAnswersCount / totalQuestions) * 100;
    return `${this.correctAnswersCount} out of ${totalQuestions} correct (${scorePercentage.toFixed(2)}%)`;
  }
}
