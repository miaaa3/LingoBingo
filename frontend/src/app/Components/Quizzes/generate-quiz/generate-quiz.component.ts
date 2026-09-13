import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { QuizService } from 'src/app/Services/Quiz/quiz.service';
import { QuizGeneratorService, GenMode, DraftQuestion } from 'src/app/Services/Quiz/quiz-generator.service';

/**
 * Draft a quiz from an existing flashcard set, offline. The heavy lifting
 * (question shaping) lives in QuizGeneratorService; this component owns the
 * three-step UI (pick set -> tune & generate -> review/edit & save) and the
 * two HTTP calls (load a set's cards, save the finished quiz).
 */
@Component({
  selector: 'app-generate-quiz',
  templateUrl: './generate-quiz.component.html',
  styleUrls: ['./generate-quiz.component.css']
})
export class GenerateQuizComponent implements OnInit {
  // Step 1 - source
  sets: any[] = [];
  loadingSets = true;
  selectedSetId: number | null = null;
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

  onSelectSet(): void {
    this.drafts = [];
    this.cards = [];
    if (this.selectedSetId == null) { return; }
    this.loadingCards = true;
    this.setsApi.getFlashcardSetById(this.selectedSetId).subscribe({
      next: set => {
        this.cards = (set?.flashcards || []).map((c: any) => ({ term: c.term, definition: c.definition }));
        this.loadingCards = false;
        if (!this.quizName) { this.quizName = set?.title ? `${set.title} quiz` : 'Generated quiz'; }
        this.count = Math.min(this.count, Math.max(2, this.usableCount));
        if (!this.canGenerate) { this.toastr.info('This set needs at least 2 complete cards to generate a quiz.'); }
      },
      error: () => { this.loadingCards = false; this.toastr.error('Could not load that set.'); }
    });
  }

  generate(): void {
    if (!this.canGenerate) { return; }
    this.drafts = this.gen.generate(this.cards, this.count, this.mode);
    if (this.drafts.length === 0) { this.toastr.info('Nothing to generate from this set.'); }
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

    const payload = {
      name: this.quizName.trim(),
      description: this.quizDescription.trim(),
      isPublic: this.isPublic,
      questions
    };

    this.saving = true;
    this.quizApi.addQuiz(payload).subscribe({
      next: () => { this.saving = false; this.toastr.success('Quiz saved!'); this.router.navigate(['/Quizzes']); },
      error: () => { this.saving = false; this.toastr.error('Could not save the quiz.'); }
    });
  }
}
