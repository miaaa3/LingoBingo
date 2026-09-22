import { Component, OnInit, HostListener } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Flashcard } from 'src/app/Models/flashcardSet/flashcard';
import { FlashcardSet } from 'src/app/Models/flashcardSet/flashcard-set';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { SpeechService } from 'src/app/Services/Speech/speech.service';
import { LocalService } from 'src/app/Services/Auth/local.service';

@Component({
  selector: 'app-display-set-of-flashcards',
  templateUrl: './display-set-of-flashcards.component.html',
  styleUrls: ['./display-set-of-flashcards.component.css']
})
export class DisplaySetOfFlashcardsComponent implements OnInit {
  flashcardSetId!: number;
  flashcardSet?: FlashcardSet;
  flashcards: Flashcard[] = [];
  currentFlashcardIndex = 0;

  // Set-management state
  isFavorite = false;
  isPublic = true;
  canManage = false;   // owner (or admin) only: edit / delete / change visibility
  menuOpen = false;
  editOpen = false;
  editForm!: FormGroup;
  saving = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private flashcardSetService: FlashcardSetService,
    private formBuilder: FormBuilder,
    private toastr: ToastrService,
    public speech: SpeechService,
    private local: LocalService
  ) {}

  ngOnInit(): void {
    this.editForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: ['']
    });

    this.route.params.subscribe(params => {
      this.flashcardSetId = +params['id'];
      this.getFlashcardSetById();
    });
  }

  // --- Data ---------------------------------------------------------------
  getFlashcardSetById(): void {
    this.flashcardSetService.getFlashcardSetById(this.flashcardSetId).subscribe({
      next: (set: FlashcardSet) => {
        this.flashcardSet = set;
        this.flashcards = set.flashcards ? set.flashcards.map(fc => ({ ...fc, flipped: false })) : [];
        this.isFavorite = !!set.isFavorite;
        this.isPublic = set.isPublic !== false;
        this.canManage = this.computeCanManage(set);
        this.currentFlashcardIndex = 0;
        this.editForm.patchValue({ name: this.title, description: set.description || '' });
      },
      error: () => this.toastr.error('Failed to load flashcard set.')
    });
  }

  get title(): string {
    return this.flashcardSet?.title || this.flashcardSet?.name || 'Flashcards';
  }

  /** Manage controls (edit/delete/visibility) are for the owner or an admin only. */
  private computeCanManage(set: any): boolean {
    const role = this.local.getData('role');
    if (role === 'ADMIN') { return true; }
    const email = (this.local.getData('email') || '').toLowerCase();
    const owner = (set?.ownerEmail || '').toLowerCase();
    return !!owner && !!email && owner === email;
  }

  get currentCard(): Flashcard | undefined {
    return this.flashcards[this.currentFlashcardIndex];
  }

  // --- Study navigation ---------------------------------------------------
  flip(): void {
    const card = this.currentCard;
    if (card) { card.flipped = !card.flipped; }
  }

  next(): void {
    if (this.currentFlashcardIndex < this.flashcards.length - 1) {
      this.currentFlashcardIndex++;
      this.showFrontSide();
    }
  }

  previous(): void {
    if (this.currentFlashcardIndex > 0) {
      this.currentFlashcardIndex--;
      this.showFrontSide();
    }
  }

  /** Always reveal the term first when a new card comes into view. */
  private showFrontSide(): void {
    const card = this.currentCard;
    if (card) { card.flipped = false; }
  }

  shuffle(): void {
    // Fisher-Yates in place, then restart from the first card.
    for (let i = this.flashcards.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.flashcards[i], this.flashcards[j]] = [this.flashcards[j], this.flashcards[i]];
    }
    this.flashcards.forEach(c => (c.flipped = false));
    this.currentFlashcardIndex = 0;
    this.toastr.info('Cards shuffled');
  }

  /** Pronounce the side currently showing, in that side's language. */
  speakCurrent(event?: Event): void {
    event?.stopPropagation();               // don't flip the card
    const card = this.currentCard;
    if (!card) { return; }
    const showingBack = !!card.flipped;
    const text = showingBack ? card.definition : card.term;
    const lang = showingBack ? this.flashcardSet?.targetLang : this.flashcardSet?.sourceLang;
    this.speech.speak(text, lang);
  }

  // --- Manage: favorite / visibility / edit / delete ----------------------
  toggleFavorite(): void {
    this.flashcardSetService.toggleFavorite(this.flashcardSetId).subscribe({
      next: (updated: any) => { this.isFavorite = updated?.isFavorite ?? !this.isFavorite; },
      error: () => this.toastr.error('Could not update favorite')
    });
  }

  toggleVisibility(): void {
    const target = !this.isPublic;
    this.flashcardSetService.setFlashcardSetVisibility(this.flashcardSetId, target).subscribe({
      next: () => {
        this.isPublic = target;
        this.toastr.success(target ? 'Set is now public' : 'Set is now private');
      },
      error: () => this.toastr.error('Could not change visibility')
    });
  }

  toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  closeMenu(): void { this.menuOpen = false; }

  openEdit(): void {
    this.editForm.patchValue({ name: this.title, description: this.flashcardSet?.description || '' });
    this.editOpen = true;
    this.menuOpen = false;
  }

  closeEdit(): void { this.editOpen = false; }

  saveEdit(): void {
    if (this.editForm.invalid || !this.flashcardSet) {
      this.editForm.markAllAsTouched();
      return;
    }
    this.saving = true;
    // Send the full set back so nothing (cards, visibility, languages) is lost;
    // only name/description change.
    const payload: FlashcardSet = {
      ...this.flashcardSet,
      name: this.editForm.value.name,
      title: this.editForm.value.name,
      description: this.editForm.value.description,
      flashcards: this.flashcards
    };
    this.flashcardSetService.updateFlashcardSet(this.flashcardSetId, payload).subscribe({
      next: (updated: FlashcardSet) => {
        this.flashcardSet = updated || payload;
        this.saving = false;
        this.editOpen = false;
        this.toastr.success('Set updated');
      },
      error: () => { this.saving = false; this.toastr.error('Could not update set'); }
    });
  }

  deleteSet(): void {
    this.menuOpen = false;
    if (!confirm(`Delete "${this.title}"? This cannot be undone.`)) { return; }
    this.flashcardSetService.deleteFlashcardSet(this.flashcardSetId).subscribe({
      next: () => {
        this.toastr.success('Set deleted');
        this.router.navigate(['/Search']);
      },
      error: () => this.toastr.error('Could not delete set')
    });
  }

  // --- Keyboard shortcuts -------------------------------------------------
  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (this.editOpen) { return; }
    const el = event.target as HTMLElement;
    if (el && /^(INPUT|TEXTAREA|SELECT)$/.test(el.tagName)) { return; }

    switch (event.key) {
      case 'ArrowRight': this.next(); break;
      case 'ArrowLeft': this.previous(); break;
      case ' ':
      case 'Enter':
        event.preventDefault();
        this.flip();
        break;
      case 'p':
      case 'P':
        this.speakCurrent();
        break;
    }
  }
}
