import { Game } from "../game/Game";
import { Flashcard } from "./flashcard";


export interface FlashcardSet {
  id?: number;
  // The backend entity exposes the field as `title`; older frontend code used
  // `name`. Both are optional here so either shape deserializes cleanly.
  name?: string;
  title?: string;
  description: string;
  flashcards: Flashcard[];
  createdAt?: Date;
  games?: Game[];
  isPublic?: boolean;   // public or private
  isFavorite?: boolean; // starred by the owner
  sourceLang?: string;  // ISO code, e.g. "en"
  targetLang?: string;  // ISO code, e.g. "fr"
}
