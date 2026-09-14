import { Injectable } from '@angular/core';

export type MatchResult = 'correct' | 'almost' | 'wrong';

/**
 * Typo-tolerant answer checking for the language practice mode. Pure and
 * deterministic so the practice component stays thin and this stays testable.
 *
 * "almost" = right apart from small typos/accents (Levenshtein within a
 * length-scaled budget); the UI accepts it but shows the exact spelling.
 */
@Injectable({ providedIn: 'root' })
export class AnswerCheckService {

  /** Lowercase, trim, strip accents and surrounding punctuation for comparison. */
  normalize(s: string): string {
    return (s || '')
      .toLowerCase()
      .normalize('NFD').replace(/[̀-ͯ]/g, '')  // strip diacritics
      .replace(/[.,!?;:"'()\[\]{}]/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
  }

  /**
   * Compare a typed answer against the expected one. The expected value may
   * hold several acceptable answers separated by "/", "," or ";".
   */
  check(input: string, expected: string): MatchResult {
    const got = this.normalize(input);
    if (!got) return 'wrong';
    const options = (expected || '').split(/[\/,;]/).map(o => this.normalize(o)).filter(Boolean);
    if (options.length === 0) return 'wrong';

    if (options.some(o => o === got)) return 'correct';

    // typo tolerance scaled to the shortest acceptable answer
    for (const o of options) {
      const budget = Math.max(1, Math.floor(o.length / 5));
      if (this.distance(got, o) <= budget) return 'almost';
    }
    return 'wrong';
  }

  /** Iterative Levenshtein distance (O(n*m) time, O(min) space). */
  distance(a: string, b: string): number {
    if (a === b) return 0;
    if (!a.length) return b.length;
    if (!b.length) return a.length;
    if (a.length < b.length) { const t = a; a = b; b = t; }
    let prev = Array.from({ length: b.length + 1 }, (_, i) => i);
    for (let i = 1; i <= a.length; i++) {
      let cur = [i];
      for (let j = 1; j <= b.length; j++) {
        const cost = a[i - 1] === b[j - 1] ? 0 : 1;
        cur[j] = Math.min(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost);
      }
      prev = cur;
    }
    return prev[b.length];
  }
}
