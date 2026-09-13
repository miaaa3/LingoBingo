import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Flashcard } from 'src/app/Models/flashcardSet/flashcard';
import { FlashcardSet } from 'src/app/Models/flashcardSet/flashcard-set';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { GameService } from 'src/app/Services/Game/game.service';

@Component({
  selector: 'app-display-set-of-flashcards',
  templateUrl: './display-set-of-flashcards.component.html',
  styleUrls: ['./display-set-of-flashcards.component.css']
})
export class DisplaySetOfFlashcardsComponent implements OnInit {
  gameSettingsForm!: FormGroup;
  flashcardSetId!: number;
  flashcardSet?: FlashcardSet;
  flashcards: Flashcard[] = [];
  currentFlashcardIndex: number = 0;
  modalOpen = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private flashcardSetService: FlashcardSetService,
    private formBuilder: FormBuilder,
    private gameService: GameService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.flashcardSetId = +params['id']; // cast to number
      this.getFlashcardSetById();
    });

    this.gameSettingsForm = this.formBuilder.group({
      numberOfQuestions: [5, [Validators.required, Validators.min(1)]],
      questionType: ['RANDOM', Validators.required] // must match backend enum values: 'TERM', 'DEFINITION', 'RANDOM'
    });
  }

  getFlashcardSetById(): void {
    this.flashcardSetService.getFlashcardSetById(this.flashcardSetId).subscribe(
      (flashcardSet: FlashcardSet) => {
        this.flashcardSet = flashcardSet;
        this.flashcards = flashcardSet.flashcards ? flashcardSet.flashcards.map(fc => ({ ...fc })) : [];
      },
      error => {
        console.error('Error fetching flashcardSet by ID:', error);
        this.toastr.error('Failed to load flashcard set.');
      }
    );
  }

  openModal(): void {
    this.modalOpen = true;
  }

  closeModal(): void {
    this.modalOpen = false;
  }

  startGame(): void {
    if (this.gameSettingsForm.invalid) {
      this.toastr.warning('Please fill in all required fields.', 'Form Invalid');
      return;
    }

    if (!this.flashcardSetId) {
      this.toastr.error('Flashcard set is not loaded yet.', 'Error');
      return;
    }

    const questionType = this.gameSettingsForm.value.questionType;  // 'TERM', 'DEFINITION', 'RANDOM'
    const numberOfQuestions = this.gameSettingsForm.value.numberOfQuestions;

    this.gameService.createGame(questionType, numberOfQuestions, this.flashcardSetId).subscribe({
      next: (game) => {
        this.toastr.success('Game started successfully!', 'Success');
        this.closeModal();
        this.router.navigate(['/GameLobby', game.gameCode]);
      },
      error: (error) => {
        this.toastr.error('Failed to start the game. Please try again.', 'Error');
        console.error('Error starting the game:', error);
      }
    });
  }

  goToNextFlashcard(): void {
    if (this.flashcardSet && this.currentFlashcardIndex < this.flashcards.length - 1) {
      this.currentFlashcardIndex++;
    }
  }

  goToPreviousFlashcard(): void {
    if (this.currentFlashcardIndex > 0) {
      this.currentFlashcardIndex--;
    }
  }

  flipFlashcard(flashcard: Flashcard): void {
    flashcard.flipped = !flashcard.flipped;
  }
}
