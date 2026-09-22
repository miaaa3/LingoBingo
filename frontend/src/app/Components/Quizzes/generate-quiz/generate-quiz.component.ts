import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { QuizGeneratorService, GenMode, DraftQuestion } from 'src/app/Services/Quiz/quiz-generator.service';

/**
 * Draft a quiz from one or more flashcard sets, offline. Question shaping lives
 * in QuizGeneratorService; this component owns the UI (pick sets -> tune &
 * generate -> review/edit & save) and the HTTP calls (load each set's cards,
 * save the finished quiz).
 */
@Component({
  selector: 'app-generate-quiz',
  templateUrl: './generate-quiz.component.html',
  styleUrls: ['./generate-quiz.component.css']
})
export class GenerateQuizComponent implements OnInit {
  // Step 1 - sources (one or several sets)
  sets: any[] = [];
  loadingSets = true;
  selectedSetIds: number[] = [];
  loadingCards = false;
  private cards: { term: string; definition: string }[] = [];

  // Step 2 - options
  count = 5;
  mode: GenMode = 'TERM';

  // Step 3 - drafts
  drafts: DraftQuestion[] = [];
  quizName = '';
  quizDescription = '';
  isPublic = true;
  dueDate = '';   // optional deadline (datetime-local string)
  saving = false;

  constructor(
    private setsApi: FlashcardSetService,
    private quizApi: QuizService,
    private gen: QuizGeneratorService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.setsApi.getVisibleFlashcardSets().subscribe({
      next: r => {
        const own = r?.ownSets || [];
        const pub = (r?.publicSets || []).filter(p => !own.some(o => o.id === p.id));
        this.sets = [...own, ...pub];
        this.loadingSets = false;
      },
      error: () => { this.loadingSets = false; this.toastr.error('Could not load your flashcard sets.'); }
    });
  }

  get usableCount(): number { return this.gen.usableCards(this.cards).length; }
  get canGenerate(): boolean { return this.usableCount >= 2; }

  // --- source selection (multi-set) ---
  isSelected(id: number): boolean { return this.selectedSetIds.includes(id); }

  toggleSet(id: number): void {
    const i = this.selectedSetIds.indexOf(id);
    if (i >= 0) { this.selectedSetIds.splice(i, 1); } else { this.selectedSetIds.push(id); }
    this.loadSelectedCards();
  }

  private loadSelectedCards(): void {
    this.drafts = [];
    if (this.selectedSetIds.length === 0) { this.cards = []; return; }
    this.loadingCards = true;
    forkJoin(this.selectedSetIds.map(id => this.setsApi.getFlashcardSetById(id))).subscribe({
      next: (loaded: any[]) => {
        const merged: { term: string; definition: string }[] = [];
        loaded.forEach(set => (set?.flashcards || []).forEach((c: any) => merged.push({ term: c.term, definition: c.definition })));
        this.cards = merged;
        this.loadingCards = false;
        if (!this.quizName) {
          const first = this.sets.find(s => s.id === this.selectedSetIds[0]);
          this.quizName = first?.title ? `${first.title} quiz` : 'Generated quiz';
        }
        this.count = Math.min(this.count, Math.max(2, this.usableCount));
        if (!this.canGenerate) { this.toastr.info('Pick sets with at least 2 complete cards between them.'); }
      },
      error: () => { this.loadingCards = false; this.toastr.error('Could not load the selected sets.'); }
    });
  }

  generate(): void {
    if (!this.canGenerate) { return; }
    this.drafts = this.gen.generate(this.cards, this.count, this.mode);
    if (this.drafts.length === 0) { this.toastr.info('Nothing to generate from these sets.'); }
  }

  // --- draft editing ---
  markCorrect(q: DraftQuestion, index: number): void {
    q.options.forEach((o, i) => (o.correct = i === index));
  }
  addOption(q: DraftQuestion): void {
    if (q.options.length < QuizGeneratorService.MAX_OPTIONS) { q.options.push({ text: '', correct: false }); }
  }
  removeOption(q: DraftQuestion, index: number): void {
    if (q.options.length <= 2) { return; }
    const wasCorrect = q.options[index].correct;
    q.options.splice(index, 1);
    if (wasCorrect) { q.options[0].correct = true; }
  }
  removeQuestion(index: number): void { this.drafts.splice(index, 1); }
  optionKey(i: number): string { return String.fromCharCode(65 + i); }
  totalPoints(): number { return this.drafts.reduce((s, q) => s + (q.points || 0), 0); }

  incPoints(q: DraftQuestion): void { q.points = (q.points || 1) + 1; }
  decPoints(q: DraftQuestion): void { q.points = Math.max(1, (q.points || 1) - 1); }

  private invalidDraft(): DraftQuestion | null {
    return this.drafts.find(q =>
      !q.questionText.trim() ||
      q.options.length < 2 ||
      q.options.some(o => !o.text.trim()) ||
      q.options.filter(o => o.correct).length !== 1
    ) || null;
  }

  save(): void {
    if (this.drafts.length === 0) { this.toastr.info('Generate some questions first.'); return; }
    if (!this.quizName.trim()) { this.toastr.warning('Give your quiz a name.'); return; }
    if (this.invalidDraft()) {
      this.toastr.warning('Each question needs text, at least 2 filled options, and exactly one correct answer.');
      return;
    }

    const questions = this.drafts.map(q => {
      const answers: { [k: string]: string } = {};
      let correctAnswer = 'A';
      q.options.forEach((opt, i) => {
        const key = this.optionKey(i);
        answers[key] = opt.text.trim();
        if (opt.correct) { correctAnswer = key; }
      });
      return { questionText: q.questionText.trim(), answers, correctAnswer, points: q.points || 1 };
    });

    const payload: any = {
      name: this.quizName.trim(),
      description: this.quizDescription.trim(),
      isPublic: this.isPublic,
      questions
    };
    if (this.dueDate) { payload.dueDate = this.dueDate; }

    this.saving = true;
    this.quizApi.addQuiz(payload).subscribe({
      next: () => { this.saving = false; this.toastr.success('Quiz saved!'); this.router.navigate(['/Quizzes']); },
      error: () => { this.saving = false; this.toastr.error('Could not save the quiz.'); }
    });
  }
}
