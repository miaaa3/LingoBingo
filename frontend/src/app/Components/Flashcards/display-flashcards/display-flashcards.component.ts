import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Flashcard } from 'src/app/Models/flashcardSet/flashcard';
import { FlashcardSet } from 'src/app/Models/flashcardSet/flashcard-set';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';

@Component({
  selector: 'app-display-flashcards',
  templateUrl: './display-flashcards.component.html',
  styleUrls: ['./display-flashcards.component.css']
})
export class DisplayFlashcardsComponent implements OnInit {
  flashcardSetId!: number;
  flashcardSet: FlashcardSet | undefined;
  flashcards: Flashcard[] = [];

  constructor(private route: ActivatedRoute, private flashcardSetService: FlashcardSetService) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.flashcardSetId = params['id'];
      this.getFlashcardSetById();
    });
  }

  getFlashcardSetById() {
    this.flashcardSetService.getFlashcardSetById(this.flashcardSetId).subscribe(
      (flashcardSet: FlashcardSet) => {
        this.flashcardSet = flashcardSet;
        this.flashcards = flashcardSet.flashcards ? flashcardSet.flashcards.map((flashcard) => ({ ...flashcard })) : [];
      },
      (error) => {
        console.error('Error fetching flashcardSet by ID:', error);
      }
    );
  }
  // Assuming this is in your component class
  currentFlashcardIndex: number = 0;

  goToNextFlashcard() {
    if (this.currentFlashcardIndex < this.flashcardSet.flashcards.length - 1) {
      this.currentFlashcardIndex++;
    }
  }

  goToPreviousFlashcard() {
    if (this.currentFlashcardIndex > 0) {
      this.currentFlashcardIndex--;
    }
  }

  flipFlashcard(flashcard: Flashcard) {
    flashcard.flipped = !flashcard.flipped;
  }


}
