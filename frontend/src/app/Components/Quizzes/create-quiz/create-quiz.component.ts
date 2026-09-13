import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';

/**
 * Manual quiz builder. Produces a clean JSON Quiz (matching the backend
 * entity: questionText, answers A-D map, single correctAnswer key, points)
 * and saves it through POST /api/quizzes/add — the same contract the
 * flashcard generator and the take/scoring flow rely on.
 */
@Component({
  selector: 'app-create-quiz',
  templateUrl: './create-quiz.component.html',
  styleUrls: ['./create-quiz.component.css']
})
export class CreateQuizComponent implements OnInit {
  quizForm!: FormGroup;
  questionsArray: AbstractControl[] = [];
  saving = false;
  readonly optionKeys = ['A', 'B', 'C', 'D'];

  constructor(
    private formBuilder: FormBuilder,
    private quizApi: QuizService,
    private router: Router,
    private toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    this.quizForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: [''],
      isPublic: [true],
      questions: this.formBuilder.array([]),
    });
    this.questionsArray = (this.quizForm.get('questions') as FormArray).controls;
    this.addNewQuestion();
  }

  addNewQuestion(): void {
    const questions = this.quizForm.get('questions') as FormArray;
    questions.push(this.formBuilder.group({
      question: ['', Validators.required],
      description: [''],
      answers: this.formBuilder.group({
        A: ['', Validators.required],
        B: ['', Validators.required],
        C: [''],
        D: [''],
      }),
      correctAnswer: ['A', Validators.required],
      points: [1, [Validators.required, Validators.min(1)]],
      explanation: [''],
      tip: [''],
    }));
    this.questionsArray = questions.controls;
  }

  deleteQuestion(index: number): void {
    const questions = this.quizForm.get('questions') as FormArray;
    if (questions.length <= 1) { return; }        // keep at least one
    questions.removeAt(index);
    this.questionsArray = questions.controls;
  }

  onQuestionDrop(event: CdkDragDrop<any[]>): void {
    moveItemInArray((this.quizForm.get('questions') as FormArray).controls, event.previousIndex, event.currentIndex);
  }

  totalPoints(): number {
    return this.questionsArray.reduce((sum, q) => sum + (Number(q.get('points')?.value) || 0), 0);
  }

  onSubmit(): void {
    if (this.quizForm.invalid) {
      this.quizForm.markAllAsTouched();
      this.toastr.warning('Add a quiz name, and for each question fill the text, options A and B, and pick the correct answer.');
      return;
    }

    const v = this.quizForm.value;
    const questions = v.questions.map((q: any) => {
      const answers: { [k: string]: string } = {};
      this.optionKeys.forEach(k => {
        const text = (q.answers[k] || '').trim();
        if (text) { answers[k] = text; }
      });
      // guard: the chosen correct key must point at a filled option
      const correctAnswer = answers[q.correctAnswer] ? q.correctAnswer : Object.keys(answers)[0];
      return {
        questionText: q.question.trim(),
        questionDescription: (q.description || '').trim(),
        answers,
        correctAnswer,
        points: q.points || 1,
        explanation: (q.explanation || '').trim(),
        tip: (q.tip || '').trim(),
      };
    });

    const payload = {
      name: v.name.trim(),
      description: (v.description || '').trim(),
      isPublic: v.isPublic,
      questions,
    };

    this.saving = true;
    this.quizApi.addQuiz(payload).subscribe({
      next: () => { this.saving = false; this.toastr.success('Quiz created successfully'); this.router.navigate(['/Quizzes']); },
      error: () => { this.saving = false; this.toastr.error('Failed to create quiz.'); },
    });
  }

}
