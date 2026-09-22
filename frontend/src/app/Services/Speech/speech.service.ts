import { Injectable, NgZone } from '@angular/core';

/** Handle returned by a dictation session: stop it early, await the final text. */
export interface SpeechHandle {
  stop: () => void;
  promise: Promise<string>;
}

/**
 * Thin wrapper over the browser's native Web Speech API — text-to-speech
 * (speechSynthesis) and one-shot speech-to-text (SpeechRecognition). No
 * external dependency, no network, no backend: everything runs in the browser.
 *
 * Both halves are feature-detected (ttsSupported / sttSupported) so callers
 * can hide the affordance where the engine is missing. Recognition callbacks
 * fire outside Angular's zone, so results are re-entered via NgZone to keep
 * the view in sync.
 *
 * Note: dictation needs a secure context (https or localhost). Playback works
 * on plain http too.
 */
@Injectable({ providedIn: 'root' })
export class SpeechService {
  private synth: SpeechSynthesis | undefined =
    typeof window !== 'undefined' ? window.speechSynthesis : undefined;
  private voices: SpeechSynthesisVoice[] = [];

  constructor(private zone: NgZone) {
    if (this.synth) {
      this.loadVoices();
      // Chrome populates the voice list asynchronously.
      try { this.synth.onvoiceschanged = () => this.loadVoices(); } catch { /* best-effort */ }
    }
  }

  private loadVoices(): void {
    try { this.voices = this.synth?.getVoices() || []; } catch { this.voices = []; }
  }

  get ttsSupported(): boolean {
    return !!this.synth && typeof SpeechSynthesisUtterance !== 'undefined';
  }

  get sttSupported(): boolean {
    return typeof window !== 'undefined' &&
      !!((window as any).SpeechRecognition || (window as any).webkitSpeechRecognition);
  }

  // ---- text to speech ----------------------------------------------------

  /** Speak a word/phrase; langCode is the set's short code (e.g. 'fr'). */
  speak(text: string, langCode?: string): void {
    if (!this.ttsSupported || !text || !text.trim()) { return; }
    try {
      this.synth!.cancel();                       // interrupt anything mid-sentence
      const utter = new SpeechSynthesisUtterance(text.trim());
      const locale = this.locale(langCode);
      if (locale) { utter.lang = locale; }
      const voice = this.pickVoice(locale);
      if (voice) { utter.voice = voice; }
      utter.rate = 0.95;                          // a touch slower helps learners
      this.synth!.speak(utter);
    } catch { /* speech is best-effort */ }
  }

  stop(): void {
    try { this.synth?.cancel(); } catch { /* best-effort */ }
  }

  // ---- speech to text ----------------------------------------------------

  /**
   * One-shot dictation. Resolves with the recognised text ('' if nothing was
   * heard or the engine failed). onInterim streams partial words while talking.
   */
  listen(langCode?: string, onInterim?: (partial: string) => void): SpeechHandle {
    const Ctor = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!Ctor) { return { stop: () => {}, promise: Promise.resolve('') }; }

    const rec = new Ctor();
    rec.lang = this.locale(langCode) || 'en-US';
    rec.interimResults = !!onInterim;
    rec.maxAlternatives = 1;
    rec.continuous = false;

    let finalText = '';
    const promise = new Promise<string>((resolve) => {
      let done = false;
      const finish = () => { if (done) { return; } done = true; this.zone.run(() => resolve(finalText.trim())); };

      rec.onresult = (e: any) => {
        let interim = '';
        for (let i = e.resultIndex; i < e.results.length; i++) {
          const r = e.results[i];
          if (r.isFinal) { finalText += r[0].transcript; }
          else { interim += r[0].transcript; }
        }
        if (onInterim && interim) { this.zone.run(() => onInterim(interim)); }
      };
      rec.onerror = () => finish();
      rec.onend = () => finish();
    });

    try { rec.start(); } catch { /* already running / insecure origin */ }
    return { stop: () => { try { rec.stop(); } catch { /* best-effort */ } }, promise };
  }

  // ---- helpers -----------------------------------------------------------

  /** Expand a short language code to a BCP-47 locale the engines accept. */
  private locale(code?: string): string | undefined {
    if (!code) { return undefined; }
    if (code.includes('-')) { return code; }
    const map: { [k: string]: string } = {
      en: 'en-US', fr: 'fr-FR', es: 'es-ES', de: 'de-DE', it: 'it-IT',
      pt: 'pt-PT', nl: 'nl-NL', ru: 'ru-RU', ar: 'ar-SA', zh: 'zh-CN',
      ja: 'ja-JP', ko: 'ko-KR', tr: 'tr-TR', pl: 'pl-PL', sv: 'sv-SE',
      da: 'da-DK', nb: 'nb-NO', fi: 'fi-FI', cs: 'cs-CZ', el: 'el-GR',
      he: 'he-IL', hi: 'hi-IN', ro: 'ro-RO', hu: 'hu-HU', uk: 'uk-UA',
    };
    return map[code.toLowerCase()] || code;
  }

  private pickVoice(locale?: string): SpeechSynthesisVoice | undefined {
    if (!locale || !this.voices.length) { return undefined; }
    const l = locale.toLowerCase();
    const base = l.split('-')[0];
    return this.voices.find(v => v.lang.toLowerCase() === l)
        || this.voices.find(v => v.lang.toLowerCase().startsWith(base));
  }
}
