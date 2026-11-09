import { Component, OnInit } from '@angular/core';
import { FlashcardSetService } from 'src/app/Services/Flashcards/flashcardSet.service';
import { TranslationService } from 'src/app/Services/Translation/translation.service';
import { SpeechService } from 'src/app/Services/Speech/speech.service';
import { BingoBatch, BingoService, FREE } from 'src/app/Services/Bingo/bingo.service';

type Phase = 'setup' | 'play';

/**
 * Classroom vocabulary bingo, built for the teacher at the front of the room:
 *  1. pick a flashcard set, a grid size and how many students,
 *  2. print the grids (each one different, numbered),
 *  3. draw words one by one — the screen shows the translation, the students
 *     look for the word on their grid,
 *  4. when someone shouts "Bingo!", type their grid number: the server checks
 *     it against the words really drawn, never against the student's ticks.
 *
 * The batch is kept in sessionStorage so a page refresh mid-game loses nothing.
 */
@Component({
  selector: 'app-bingo',
  templateUrl: './bingo.component.html',
})
export class BingoComponent implements OnInit {
  readonly FREE = FREE;
  private static readonly STORE = 'lb-bingo-game';

  phase: Phase = 'setup';
  loadingSets = true;
  sets: any[] = [];

  // setup form
  setId: number | null = null;
  size = 4;
  count = 25;
  freeCenter = true;
  generating = false;
  error = '';

  // game
  batch: BingoBatch | null = null;
  clueLang: string | undefined;   // language of the clue (the definition side)
  called: number[] = [];          // word indexes in draw order
  private remaining: number[] = [];

  // referee
  checkNumber: number | null = null;
  checking = false;
  checkResult: { grid: number; won: boolean; lines: string[] } | null = null;
  checkError = '';

  constructor(
    private setsApi: FlashcardSetService,
    private bingoApi: BingoService,
    public lang: TranslationService,
    public speech: SpeechService,
  ) {}

  ngOnInit(): void {
    this.restore();
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

  get selectedSet(): any { return this.sets.find(s => s.id === this.setId) || null; }
  get canFree(): boolean { return this.size % 2 === 1; }
  get wordsNeeded(): number { return this.size * this.size - (this.freeCenter && this.canFree ? 1 : 0); }
  get lastCalled(): number | null { return this.called.length ? this.called[this.called.length - 1] : null; }
  get finished(): boolean { return !!this.batch && this.remaining.length === 0; }

  // ---------------------------------------------------------------- setup

  generate(): void {
    if (this.setId == null) { this.error = 'Choose a flashcard set first.'; return; }
    this.error = '';
    this.generating = true;
    this.bingoApi.generate(this.setId, this.size, this.count, this.freeCenter && this.canFree).subscribe({
      next: batch => {
        this.generating = false;
        this.batch = batch;
        this.clueLang = this.selectedSet?.targetLang || undefined;
        this.called = [];
        this.remaining = this.shuffle(batch.words.map((_, i) => i));
        this.checkResult = null;
        this.phase = 'play';
        this.persist();
      },
      error: err => {
        this.generating = false;
        this.error = err?.error?.message || 'Could not create the grids. Please try again.';
      },
    });
  }

  newGame(): void {
    if (this.called.length && !confirm('Stop this game and set up a new one?')) return;
    this.batch = null;
    this.called = [];
    this.remaining = [];
    this.checkResult = null;
    this.phase = 'setup';
    this.persist();
  }

  // ---------------------------------------------------------------- calling

  draw(): void {
    if (!this.batch || this.remaining.length === 0) return;
    const next = this.remaining.pop()!;
    this.called.push(next);
    this.checkResult = null;
    this.persist();
    this.say(next);
  }

  undo(): void {
    const last = this.called.pop();
    if (last === undefined) return;
    this.remaining.push(last);
    this.checkResult = null;
    this.persist();
  }

  /** Read the clue aloud: the translation (definition side, target language). */
  say(index: number): void {
    const w = this.batch?.words[index];
    if (w) this.speech.speak(w.definition || w.term, this.clueLang);
  }

  isCalled(index: number): boolean { return this.called.includes(index); }

  // ---------------------------------------------------------------- referee

  check(): void {
    this.checkError = '';
    this.checkResult = null;
    const n = Number(this.checkNumber);
    if (!this.batch || !Number.isInteger(n) || n < 1 || n > this.batch.grids.length) {
      this.checkError = `Enter a grid number between 1 and ${this.batch?.grids.length ?? 0}.`;
      return;
    }
    this.checking = true;
    this.bingoApi.check(this.batch.grids[n - 1], this.called).subscribe({
      next: r => { this.checking = false; this.checkResult = { grid: n, ...r }; },
      error: () => { this.checking = false; this.checkError = 'Could not check this grid. Please try again.'; },
    });
  }

  /** Squares of the checked grid that belong to a winning line, for highlighting. */
  inWinningLine(cell: number): boolean {
    if (!this.checkResult?.won || !this.batch) return false;
    const n = this.batch.size, r = Math.floor(cell / n), c = cell % n;
    return this.checkResult.lines.some(l =>
      l === `row ${r + 1}` || l === `column ${c + 1}` ||
      (l === 'diagonal \\' && r === c) || (l === 'diagonal /' && r + c === n - 1));
  }

  // ---------------------------------------------------------------- printing

  /** Opens a clean, printable sheet: two grids per A4 page, numbered. */
  print(): void {
    if (!this.batch) return;
    const b = this.batch, esc = (s: string) => s.replace(/[&<>"]/g, ch =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' } as any)[ch]);
    const cards = b.grids.map((g, i) => `
      <section class="card">
        <header><span>LingoBingo · ${esc(b.setTitle)}</span><b>Grid ${i + 1}</b></header>
        <table>${Array.from({ length: b.size }, (_, r) => `<tr>${g.slice(r * b.size, (r + 1) * b.size)
          .map(w => w === FREE ? '<td class="free">★<br>FREE</td>' : `<td>${esc(b.words[w].term)}</td>`).join('')}</tr>`).join('')}
        </table>
      </section>`).join('');
    const html = `<!doctype html><html><head><meta charset="utf-8"><title>Bingo grids — ${esc(b.setTitle)}</title>
      <style>
        body{font-family:Arial,Helvetica,sans-serif;margin:0;padding:12mm;color:#111}
        .card{page-break-inside:avoid;break-inside:avoid;margin:0 auto 10mm;max-width:170mm}
        .card:nth-of-type(2n){page-break-after:always;break-after:page}
        header{display:flex;justify-content:space-between;font-size:12pt;margin-bottom:3mm}
        table{width:100%;border-collapse:collapse;table-layout:fixed}
        td{border:2px solid #111;height:${b.size >= 5 ? 22 : 28}mm;text-align:center;vertical-align:middle;
           font-size:${b.size >= 5 ? 12 : 14}pt;font-weight:bold;padding:2mm;word-wrap:break-word}
        td.free{background:#eee;font-size:10pt}
      </style></head><body>${cards}<script>window.onload=()=>window.print()<\/script></body></html>`;
    const w = window.open('', '_blank');
    if (!w) { alert('Please allow pop-ups to print the grids.'); return; }
    w.document.open(); w.document.write(html); w.document.close();
  }

  // ---------------------------------------------------------------- persistence

  private persist(): void {
    try {
      if (!this.batch) { sessionStorage.removeItem(BingoComponent.STORE); return; }
      sessionStorage.setItem(BingoComponent.STORE, JSON.stringify({
        batch: this.batch, clueLang: this.clueLang, called: this.called, remaining: this.remaining }));
    } catch { /* storage unavailable: the game still works for this page view */ }
  }

  private restore(): void {
    try {
      const raw = sessionStorage.getItem(BingoComponent.STORE);
      if (!raw) return;
      const s = JSON.parse(raw);
      if (s?.batch?.grids?.length) {
        this.batch = s.batch; this.clueLang = s.clueLang; this.called = s.called || []; this.remaining = s.remaining || [];
        this.phase = 'play';
      }
    } catch { /* ignore a corrupt saved game */ }
  }

  private shuffle<T>(a: T[]): T[] {
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }
}
