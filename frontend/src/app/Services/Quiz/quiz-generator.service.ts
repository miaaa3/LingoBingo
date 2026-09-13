import { Injectable } from '@angular/core';

export type GenMode = 'TERM' | 'DEFINITION' | 'RANDOM';

export interface SourceCard { term: string; definition: string; }
export interface DraftOption { text: string; correct: boolean; }
export interface DraftQuestion { questionText: string; options: DraftOption[]; points: number; }

/**
 * Builds draft multiple-choice questions from a flashcard set, fully offline
 * (no external API). Pure and deterministic given its RNG: the component owns
 * all UI/HTTP, this owns only the question-shaping logic so it stays testable.
 *
 * TERM mode asks the meaning of a term (answer = definition); DEFINITION mode
 * asks which term matches a definition; RANDOM mixes per card. Distractors are
 * other cards' definitions/terms from the same set.
 */
@Injectable({ providedIn: 'root' })
export class QuizGeneratorService {
  static readonly MAX_OPTIONS = 4;

  /** Cards usable as questions (both fields filled). */
  usableCards(cards: SourceCard[]): SourceCard[] {
    return (cards || []).filter(c => c && (c.term || '').trim() && (c.definition || '').trim());
  }

  generate(cards: SourceCard[], count: number, mode: GenMode): DraftQuestion[] {
    const valid = this.usableCards(cards);
    if (valid.length < 2) return [];              // need at least one distractor
    const n = Math.max(1, Math.min(count, valid.length));
    return this.sample(valid, n).map(card => this.buildQuestion(card, valid, mode));
  }

  private buildQuestion(card: SourceCard, pool: SourceCard[], mode: GenMode): DraftQuestion {
    const useTerm = mode === 'RANDOM' ? Math.random() < 0.5 : mode === 'TERM';
    const prompt = (useTerm ? card.term : card.definition).trim();
    const correct = (useTerm ? card.definition : card.term).trim();
    const questionText = useTerm
      ? `What does "${prompt}" mean?`
      : `Which term matches: "${prompt}"?`;

    const distractorPool = this.unique(
      pool.filter(c => c !== card).map(c => (useTerm ? c.definition : c.term).trim()),
      correct
    );
    const distractors = this.sample(distractorPool, Math.min(QuizGeneratorService.MAX_OPTIONS - 1, distractorPool.length));

    const options = this.shuffle([
      { text: correct, correct: true },
      ...distractors.map(t => ({ text: t, correct: false })),
    ]);
    return { questionText, options, points: 1 };
  }

  /** De-duplicate (case-insensitive) and drop anything equal to `exclude`. */
  private unique(values: string[], exclude: string): string[] {
    const seen = new Set<string>([exclude.toLowerCase()]);
    const out: string[] = [];
    for (const v of values) {
      const key = v.toLowerCase();
      if (v && !seen.has(key)) { seen.add(key); out.push(v); }
    }
    return out;
  }

  /** Fisher-Yates sample of up to k items (does not mutate the input). */
  private sample<T>(arr: T[], k: number): T[] {
    const a = arr.slice();
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a.slice(0, Math.max(0, k));
  }

  private shuffle<T>(arr: T[]): T[] { return this.sample(arr, arr.length); }
}
