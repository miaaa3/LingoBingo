export class QuestionDTO {
  questionId: number;
  questionText: string;
  options: string[];  // Multiple choice answers
  correctAnswer: string;
  userAnswer: string | null = null;  // Default to null when initializing

  constructor(
    questionId: number,
    questionText: string,
    options: string[],
    correctAnswer: string,
    userAnswer: string | null = null
  ) {
    this.questionId = questionId;
    this.questionText = questionText;
    this.options = options;
    this.correctAnswer = correctAnswer;
    this.userAnswer = userAnswer;
  }
}
