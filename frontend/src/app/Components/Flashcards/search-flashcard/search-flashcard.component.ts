import { Component, OnInit, OnDestroy } from '@angular/core';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { Subject, forkJoin, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';

@Component({
  selector: 'app-search-flashcard',
  templateUrl: './search-flashcard.component.html',
  styleUrls: ['./search-flashcard.component.css'],
})
export class SearchFlashcardComponent implements OnInit, OnDestroy {
  filterValue: string = '';

  ownFlashcardSets: any[] = [];
  publicFlashcardSets: any[] = [];
  ownQuizzes: any[] = [];
  publicQuizzes: any[] = [];

  loading: boolean = false;
  error: string | null = null;

  private searchSubject = new Subject<string>();
  private searchSubscription!: Subscription;

  constructor(
    private flashcardSetService: FlashcardSetService,
    private quizService: QuizService
  ) {}

  ngOnInit(): void {
    // Subscribe to the search subject and do live search
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(300),               // Wait 300ms pause in typing
      distinctUntilChanged(),          // Only emit if value changed
      tap(() => {
        this.loading = true;
        this.error = null;
      }),
      switchMap((searchTerm) => {
        if (!searchTerm.trim()) {
          // If empty input, load default visible items
          return forkJoin({
            flashcardSets: this.flashcardSetService.getVisibleFlashcardSets(),
            quizzes: this.quizService.getVisibleQuizzes()
          });
        } else {
          // Otherwise do search with filter
          return forkJoin({
            flashcardSets: this.flashcardSetService.searchFlashcardSetsSeparate(searchTerm),
            quizzes: this.quizService.searchQuizzesSeparate(searchTerm)
          });
        }
      })
    ).subscribe({
      next: ({ flashcardSets, quizzes }) => {
        this.ownFlashcardSets = flashcardSets.ownSets ?? [];
        this.publicFlashcardSets = flashcardSets.publicSets ?? [];
        this.ownQuizzes = quizzes.ownQuizzes ?? [];
        this.publicQuizzes = quizzes.publicQuizzes ?? [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Search failed. Please try again.';
        this.loading = false;
      }
    });

    // Initially load all visible items
    this.searchSubject.next('');
  }

  ngOnDestroy(): void {
    if (this.searchSubscription) {
      this.searchSubscription.unsubscribe();
    }
  }

  // Called by the input (see template)
  onFilterChange(event: Event): void {
  const input = event.target as HTMLInputElement;
  const value = input.value;
  this.filterValue = value;
  this.searchSubject.next(value);
}

}
