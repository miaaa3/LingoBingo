import { Component, HostListener, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { RestApiService } from 'src/app/Services/Auth/rest-api.service';

@Component({
  selector: 'app-create-quiz',
  templateUrl: './create-quiz.component.html',
  styleUrls: ['./create-quiz.component.css']
})
export class CreateQuizComponent implements OnInit {
  difficulties: string[] = [];
  categories: string[] = [];
  numbers: number[] = [];
  id: number = 28;
  quizForm!: FormGroup;
  questionsArray: AbstractControl[] = [];
  imageFile: File | null = null; // Add this to store the image file

  constructor(
    private formBuilder: FormBuilder,
    private api: RestApiService,
    private router: Router,
    private toastr: ToastrService,
  ) {}

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input && input.files) {
      this.imageFile = input.files[0];
    }
  }
  

  // Method to add a new question
  addNewQuestion(): void {
    const questions = this.quizForm.get('questions') as FormArray;
    questions.push(this.formBuilder.group({
      question: ['', Validators.required],
      description: ['', Validators.required],
      answers: this.formBuilder.group({
        A: ['', Validators.required],
        B: [''],
        C: [''],
        D: ['']
      }),
      multipleCorrectAnswers: false,
      correctAnswers: this.formBuilder.group({
        A: false,
        B: false,
        C: false,
        D: false
      }),
      explanation: [''],
      tip: [''],
      tags: [[]],
      userAnswer: null,
    }));
    this.numbers.push(this.numbers.length + 1);
    this.questionsArray = (this.quizForm.get('questions') as FormArray).controls;
  }

  ngOnInit(): void {
    this.numbers = [1];
    //this.categories = getQuizCategories();
    //this.difficulties = getDifficulties();

    this.quizForm = this.formBuilder.group({
      name: [''],
      category: [''],
      difficulty: [''],
      description: [''],
      image: [''], // Add a field for the image
      questions: this.formBuilder.array([]),
    });

    this.questionsArray = (this.quizForm.get('questions') as FormArray).controls;

    this.addNewQuestion();
  }

  async onSubmit() {
    if (this.quizForm.valid) {
      const formData = new FormData();
      formData.append('name', this.quizForm.value.name);
      formData.append('category', this.quizForm.value.category);
      formData.append('difficulty', this.quizForm.value.difficulty);
      formData.append('description', this.quizForm.value.description);
      
      // Append the image if there is one
      if (this.imageFile) {
        formData.append('image', this.imageFile, this.imageFile.name);
      }
  
      const lastIdResponse = await this.api.getLastId().toPromise();
      this.id = lastIdResponse['id'];
  
      // Push all questions to the form data as well
      formData.append('questions', JSON.stringify(this.quizForm.value.questions));
  
      await this.api.addQuiz(formData).subscribe(
        res => {
          this.toastr.success('Quiz created successfully');
          setTimeout(() => {
            this.router.navigate(['/Home']);
          }, 2000);
        },
        err => {
          this.toastr.error('Failed to create quiz.');
        }
      );
    }
  }
  

  isFixedHeaderVisible = false;

  @HostListener('window:scroll', [])
  onWindowScroll() {
    const scrollPosition = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
    this.isFixedHeaderVisible = scrollPosition > 100;
  }

  onCheckboxChange(index: number): void {
    const answersArray = this.quizForm.get('answers') as FormArray;
    const answerControl = answersArray.at(index).get('isCorrect');
    answerControl.setValue(!answerControl.value);
  }

  deleteQuestion(index: number): void {
    const questions = this.quizForm.get('questions') as FormArray;
    questions.removeAt(index);
    this.questionsArray = questions.controls;
  }

  onQuestionDrop(event: CdkDragDrop<any[]>): void {
    moveItemInArray((this.quizForm.get('questions') as FormArray).controls, event.previousIndex, event.currentIndex);
  }
}
