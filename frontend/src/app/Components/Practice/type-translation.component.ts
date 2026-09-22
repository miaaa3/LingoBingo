import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { AnswerCheckService, MatchResult } from 'src/app/Services/Practice/answer-check.service';
import { TranslationService } from 'src/app/Services/Translation/translation.service';
import { PracticeService } from 'src/app/Services/Practice/practice.service';
import { SpeechService, SpeechHandle } from 'src/app/Services/Speech/speech.service';

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

  // Answering style: 'type' (keyboard) or 'tap' (word bank, beginner-friendly)
  inputMode: 'type' | 'tap' = 'type';
  currentOptions: string[] = [];

  // Voice dictation state
  listening = false;
  private speechHandle?: SpeechHandle;

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
    private route: ActivatedRoute,
    public speech: SpeechService,
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

    // Deep link: /Practice?set=<id> starts that set straight away.
    const setParam = this.route.snapshot.queryParamMap.get('set');
    if (setParam) { this.start({ id: +setParam }); }
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

  setDirection(d: Direction): void {
    this.direction = d;
    if (this.inputMode === 'tap' && this.status === 'idle') { this.buildOptions(); }
  }

  setInputMode(m: 'type' | 'tap'): void {
    this.inputMode = m;
    if (m === 'tap') { this.buildOptions(); }
    else { setTimeout(() => this.answerInput?.nativeElement.focus(), 30); }
  }

  /** Word-bank options for the current question: correct answer + up to 3 distractors from the set. */
  private buildOptions(): void {
    if (!this.queue.length) { this.currentOptions = []; return; }
    const correct = this.expected;
    const pool = this.queue
      .map(c => (this.direction === 'forward' ? c.definition : c.term))
      .filter(v => v && v.trim());
    const seen = new Set<string>([correct.trim().toLowerCase()]);
    const distractors: string[] = [];
    for (const v of this.shuffle(pool)) {
      const k = v.trim().toLowerCase();
      if (!seen.has(k)) { seen.add(k); distractors.push(v); }
      if (distractors.length >= 3) break;
    }
    this.currentOptions = this.shuffle([correct, ...distractors]);
  }

  // ---- question helpers ----
  private get card(): Card { return this.queue[this.index]; }
  get prompt(): string { return this.direction === 'forward' ? this.card.term : this.card.definition; }
  get expected(): string { return this.direction === 'forward' ? this.card.definition : this.card.term; }
  get promptLangCode(): string { return this.direction === 'forward' ? (this.set?.sourceLang || '') : (this.set?.targetLang || ''); }
  get answerLangCode(): string { return this.direction === 'forward' ? (this.set?.targetLang || '') : (this.set?.sourceLang || ''); }

  private newQuestion(): void {
    this.stopDictation();
    this.answer = '';
    this.status = 'idle';
    this.revealed = 0;
    this.helped = false;
    if (this.inputMode === 'tap') { this.buildOptions(); }
    else { setTimeout(() => this.answerInput?.nativeElement.focus(), 30); }
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
    this.grade();
  }

  /** Tap a word-bank option. */
  choose(opt: string): void {
    if (this.status !== 'idle') { return; }
    this.answer = opt;
    this.grade();
  }

  // ---- speech ----
  /** Read the prompt word aloud in its own language. */
  speakPrompt(): void {
    this.speech.speak(this.prompt, this.promptLangCode);
  }

  /** Toggle voice answering: dictate into the answer box, then grade it. */
  toggleDictation(): void {
    if (this.listening) { this.speechHandle?.stop(); return; }
    if (this.status !== 'idle') { return; }
    this.listening = true;
    this.answer = '';
    this.speechHandle = this.speech.listen(this.answerLangCode, partial => { this.answer = partial; });
    this.speechHandle.promise.then(finalText => {
      this.listening = false;
      this.speechHandle = undefined;
      if (finalText) { this.answer = finalText; this.grade(); }
    });
  }

  private stopDictation(): void {
    if (this.speechHandle) { this.speechHandle.stop(); this.speechHandle = undefined; }
    this.listening = false;
  }

  private grade(): void {
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
