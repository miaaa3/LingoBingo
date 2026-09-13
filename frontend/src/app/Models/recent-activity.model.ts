import { FlashcardSet } from "./flashcardSet/flashcard-set";
import { Quiz } from "./quiz/quiz.model";

export class RecentActivity {
    id?: number;
    userId?: number;
    type: string; // You can use an enum for activity types
    title: string;
    quiz: Quiz | null = null; // Nullable to allow for activities without quizzes
    flashcardSet: FlashcardSet | null = null; // Nullable to allow for quizzes without flashcard sets
    timestamp?: Date;

}