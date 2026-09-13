import { Game } from "../game/Game";
import { Flashcard } from "./flashcard";


export interface FlashcardSet {
  id?: number;
  name: string; 
  description: string;
  flashcards: Flashcard[]; 
  createdAt?: Date;
  games: Game[];
  isPublic?: boolean; // Indicates if the set is public or private
}
