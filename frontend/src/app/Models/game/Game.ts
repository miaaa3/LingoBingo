import { FlashcardService } from "src/app/Services/Flashcards/flashcard.service";
import { Quiz } from "../quiz/quiz.model";
import { Player } from "./Player";
import { User } from "../user.model";
import { FlashcardSet } from "../flashcardSet/flashcard-set";

export interface Game {
  id?: number;

  // Reference to teacher user
  createdBy?:User;
  createdAt?: string; // ISO date string

  // List of players in the game
  players?: Player[];

  // Optional flashcard set reference
  flashcardSet?: FlashcardSet;

  // Optional quiz reference
  quiz?: Quiz;
  questionType?: 'TERM' | 'DEFINITION' | 'RANDOM';

  isActive?: boolean;

  gameCode?: string;

  playerScores?: { [playerId: number]: number };

  playerFinishTimes?: { [playerId: number]: number };

  playerQuestionIds?: { [playerId: number]: number[] };

  playerCurrentQuestionIndex?: { [playerId: number]: number };

  numberOfQuestions: number;
}
