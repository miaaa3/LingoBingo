export interface Question {
  questionId?: number;
  questionText: string;
  questionDescription?: string;

  // Map of answer options, e.g. { "A": "Answer 1", "B": "Answer 2", ... }
  answers: { [key: string]: string };

  // Single correct answer key, e.g. "A" or "B"
  correctAnswer: string;

  explanation?: string;
  tip?: string;

  // Picture as base64 string for frontend use
  picture?: string; // base64 encoded image string

  // Reference to Quiz ID or object if needed
  quizId?: number;

  userAnswer?: string;
}
