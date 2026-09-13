
export class UserAnswerRequest {
  questionId: number;
  userAnswer: string;

constructor(questionId: number, userAnswer: string) {
    this.questionId = questionId;
    this.userAnswer = userAnswer;
  }
}
