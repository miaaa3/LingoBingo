import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { TranslationService, LANGUAGES } from 'src/app/Services/Translation/translation.service';
import { ToastrService } from 'ngx-toastr';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, map, switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-create-set-of-flashcards',
  templateUrl: './create-set-of-flashcards.component.html',
  styleUrls: ['./create-set-of-flashcards.component.css']
})
export class CreateSetOfFlashcardsComponent implements OnInit, OnDestroy {
  flashcardSetForm!: FormGroup;
  flashcardsArray: AbstractControl[] = [];
  readonly languages = LANGUAGES;

  // Per-card live-translation state, keyed by the card control so it survives reordering/deletion.
  suggestions = new Map<AbstractControl, string>();
  translating = new Set<AbstractControl>();
  private cardSubs = new Map<AbstractControl, Subscription>();

  constructor(
    private formBuilder: FormBuilder,
    private flashcardSetService: FlashcardSetService,
    private translator: TranslationService,
    private router: Router,
    private toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    this.flashcardSetForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: [''],
      sourceLang: ['en'],
      targetLang: ['fr'],
      flashcards: this.formBuilder.array([]),
    });
    this.flashcardsArray = (this.flashcardSetForm.get('flashcards') as FormArray).controls;
    this.addNewCard();
  }

  ngOnDestroy(): void {
    this.cardSubs.forEach(s => s.unsubscribe());
  }

  addNewCard(): void {
    const flashcards = this.flashcardSetForm.get('flashcards') as FormArray;
    const group = this.formBuilder.group({
      term: ['', Validators.required],
      definition: ['', Validators.required],
    });
    flashcards.push(group);
    this.flashcardsArray = flashcards.controls;
    this.watchCard(group);
  }

  /** Debounced live translation of the term -> suggestion for the definition. */
  private watchCard(group: AbstractControl): void {
    const term = group.get('term');
    if (!term) { return; }
    const sub = term.valueChanges.pipe(
      map(v => (v || '').trim()),
      debounceTime(450),
      distinctUntilChanged(),
      switchMap(text => {
        // Don't suggest when empty or when the teacher already filled the definition.
        if (!text || (group.get('definition')?.value || '').trim()) {
          this.suggestions.delete(group);
          return of(null);
        }
        const from = this.flashcardSetForm.get('sourceLang')?.value || 'en';
        const to = this.flashcardSetForm.get('targetLang')?.value || 'fr';
        if (from === to) { return of(null); }
        this.translating.add(group);
        return this.translator.translate(text, from, to).pipe(catchError(() => of(null)));
      }),
    ).subscribe(res => {
      this.translating.delete(group);
      if (res && res.ok && res.translation) { this.suggestions.set(group, res.translation); }
      else { this.suggestions.delete(group); }
    });
    this.cardSubs.set(group, sub);
  }

  suggestionFor(card: AbstractControl): string | null { return this.suggestions.get(card) || null; }
  isTranslating(card: AbstractControl): boolean { return this.translating.has(card); }

  acceptSuggestion(card: AbstractControl): void {
    const s = this.suggestions.get(card);
    if (s) { card.get('definition')?.setValue(s); this.suggestions.delete(card); }
  }
  dismissSuggestion(card: AbstractControl): void { this.suggestions.delete(card); }

  deleteCard(index: number): void {
    const flashcards = this.flashcardSetForm.get('flashcards') as FormArray;
    if (flashcards.length <= 1) { this.toastr.warning('You must have at least one card.', 'Warning'); return; }
    const removed = flashcards.at(index);
    this.cardSubs.get(removed)?.unsubscribe();
    this.cardSubs.delete(removed);
    this.suggestions.delete(removed);
    this.translating.delete(removed);
    flashcards.removeAt(index);
    this.flashcardsArray = flashcards.controls;
  }

  onQuestionDrop(event: CdkDragDrop<any[]>): void {
    moveItemInArray(this.flashcardsArray, event.previousIndex, event.currentIndex);
  }

  swapLanguages(): void {
    const from = this.flashcardSetForm.get('sourceLang')?.value;
    const to = this.flashcardSetForm.get('targetLang')?.value;
    this.flashcardSetForm.patchValue({ sourceLang: to, targetLang: from });
  }

  createFlashcardSet(): void {
    if (this.flashcardSetForm.invalid) {
      this.flashcardSetForm.markAllAsTouched();
      this.toastr.warning('Give the set a name and fill in every card (term + definition).');
      return;
    }
    const raw = this.flashcardSetForm.value;
    const flashcardSet = {
      title: raw.name,
      description: raw.description,
      sourceLang: raw.sourceLang,
      targetLang: raw.targetLang,
      flashcards: raw.flashcards,
    };
    this.flashcardSetService.addFlashcardSet(flashcardSet).subscribe({
      next: () => { this.toastr.success('Flashcard set created successfully', 'Success'); this.router.navigate(['/Search']); },
      error: () => { this.toastr.error('Error creating flashcard set', 'Error'); },
    });
  }
}
