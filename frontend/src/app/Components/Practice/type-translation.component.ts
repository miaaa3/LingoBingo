import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { AnswerCheckService, MatchResult } from 'src/app/Services/Practice/answer-check.service';
import { TranslationService } from 'src/app/Services/Translation/translation.service';
import { PracticeService } from 'src/app/Services/Practice/practice.service';

type Phase = 'choose' | 'play' | 'done';
type Direction = 'forward' | 'reverse';   // forward: prompt=term, answer=definition
interface Card { term: string; definition: string; }

/**
 * Duolingo-style "type the translation" practice. Pick a set, get prompted with
 * a word, type the translation; answers are typo-tolerant (AnswerCheckService),
 * with letter hints and per-session XP/streak. No backend writes — session-only
 * scoring keeps it fast and avoids schema changes.
 */
@Component({
  selector: 'app-type-translation',
  templateUrl: './type-translation.component.html',
  styleUrls: ['./type-translation.component.css']
})
export class TypeTranslationComponent implements OnInit {
  @ViewChild('answerInput') answerInput?: ElementRef<HTMLInputElement>;

  phase: Phase = 'choose';
  loadingSets = true;
  sets: any[] = [];

  // active session
  set: any = null;
  direction: Direction = 'forward';
  private queue: Card[] = [];
  index = 0;
  total = 0;

  answer = '';
  status: 'idle' | MatchResult = 'idle';
  revealed = 0;                 // hint letters shown for current answer
  private helped = false;       // used a hint on this question

  // stats
  xp = 0;
  correctCount = 0;
  almostCount = 0;
  wrongCount = 0;
  currentStreak = 0;
  bestStreak = 0;
  saved = false;   // XP persisted to profile

  constructor(
    private setsApi: FlashcardSetService,
    private checker: AnswerCheckService,
    public lang: TranslationService,
    private practiceApi: PracticeService,
  ) {}

  ngOnInit(): void {
    this.setsApi.getVisibleFlashcardSets().subscribe({
      next: r => {
        const own = r?.ownSets || [];
        const pub = (r?.publicSets || []).filter(p => !own.some(o => o.id === p.id));
        this.sets = [...own, ...pub];
        this.loadingSets = false;
      },
      error: () => { this.loadingSets = false; },
    });
  }

  // ---- setup ----
  start(set: any): void {
    this.setsApi.getFlashcardSetById(set.id).subscribe({
      next: full => {
        const cards: Card[] = (full?.flashcards || [])
          .map((c: any) => ({ term: (c.term || '').trim(), definition: (c.definition || '').trim() }))
          .filter((c: Card) => c.term && c.definition);
        if (cards.length === 0) { return; }
        this.set = full;
        this.direction = 'forward';
        this.queue = this.shuffle(cards);
        this.total = this.queue.length;
        this.resetStats();
        this.index = 0;
        this.phase = 'play';
        this.newQuestion();
      },
    });
  }

  private resetStats(): void {
    this.xp = 0; this.correctCount = 0; this.almostCount = 0; this.wrongCount = 0;
    this.currentStreak = 0; this.bestStreak = 0;
    this.saved = false;
  }

  setDirection(d: Direction): void { this.direction = d; }

  // ---- question helpers ----
  private get card(): Card { return this.queue[this.index]; }
  get prompt(): string { return this.direction === 'forward' ? this.card.term : this.card.definition; }
  get expected(): string { return this.direction === 'forward' ? this.card.definition : this.card.term; }
  get promptLangCode(): string { return this.direction === 'forward' ? (this.set?.sourceLang || '') : (this.set?.targetLang || ''); }
  get answerLangCode(): string { return this.direction === 'forward' ? (this.set?.targetLang || '') : (this.set?.sourceLang || ''); }

  private newQuestion(): void {
    this.answer = '';
    this.status = 'idle';
    this.revealed = 0;
    this.helped = false;
    setTimeout(() => this.answerInput?.nativeElement.focus(), 30);
  }

  // ---- hint (the "keyboard suggestion" while typing) ----
  hint(): void {
    if (this.status !== 'idle') { return; }
    if (this.revealed < this.expected.length) { this.revealed++; this.helped = true; }
  }
  /** e.g. "f r a _ _ _" — revealed letters plus underscores; spaces kept. */
  get hintDisplay(): string {
    const ans = this.expected;
    return ans.split('').map((ch, i) => (ch === ' ' ? ' ' : i < this.revealed ? ch : '_')).join(' ');
  }

  // ---- answering ----
  submit(): void {
    if (this.status !== 'idle') { this.next(); return; }        // Enter advances after feedback
    if (!this.answer.trim() && this.revealed === 0) { return; }
    const result = this.checker.check(this.answer, this.expected);
    this.status = result;
    if (result === 'correct') { this.xp += this.helped ? 5 : 10; this.correctCount++; this.bumpStreak(); }
    else if (result === 'almost') { this.xp += this.helped ? 3 : 7; this.almostCount++; this.bumpStreak(); }
    else { this.wrongCount++; this.currentStreak = 0; }
  }

  private bumpStreak(): void { this.currentStreak++; this.bestStreak = Math.max(this.bestStreak, this.currentStreak); }

  skip(): void {
    if (this.status !== 'idle') { return; }
    this.status = 'wrong';
    this.wrongCount++;
    this.currentStreak = 0;
  }

  next(): void {
    if (this.index + 1 >= this.total) { this.finish(); return; }
    this.index++;
    this.newQuestion();
  }

  private finish(): void {
    this.phase = 'done';
    if (this.xp > 0) {
      this.practiceApi.complete({ xp: this.xp, correct: this.correctCount + this.almostCount, total: this.total })
        .subscribe(res => { this.saved = !!res; });
    }
  }

  // ---- end / restart ----
  restart(): void {
    this.queue = this.shuffle(this.queue);
    this.resetStats();
    this.index = 0;
    this.phase = 'play';
    this.newQuestion();
  }
  chooseAnother(): void { this.phase = 'choose'; this.set = null; }

  // ---- computed for template ----
  get answered(): number { return this.correctCount + this.almostCount + this.wrongCount; }
  get progressPct(): number { return this.total ? Math.round((this.index / this.total) * 100) : 0; }
  get accuracy(): number {
    const denom = this.correctCount + this.almostCount + this.wrongCount;
    return denom ? Math.round(((this.correctCount + this.almostCount) / denom) * 100) : 0;
  }

  private shuffle<T>(arr: T[]): T[] {
    const a = arr.slice();
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }
}
