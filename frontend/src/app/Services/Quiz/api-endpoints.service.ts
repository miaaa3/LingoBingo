import { Injectable } from "@angular/core";
import { environment } from "src/app/Environments/environment";

@Injectable({
  providedIn: 'root',
})
export class ApiEndPoints {

  base = 'https://quizapi.io/api/v1';
  // Provided via environment config, never hardcoded. Rotate the old leaked key on quizapi.io.
  token = environment.quizApiKey;

  QUIZ_API_ENDPOINTS = {
    questions: `${this.base}/questions?apiKey=${this.token}`,
    questionByCategory: (category: string) => `${this.base}/questions?apiKey=${this.token}&limit=10&category=${category}`,
    questionByDifficultyAndCategory: (difficulty: string, category: string) => `${this.base}/questions?apiKey=${this.token}&limit=10&difficulty=${difficulty}&category=${category}`,
    questionByDifficulty: (difficulty: string) => `${this.base}/questions?apiKey=${this.token}&limit=10&difficulty=${difficulty}`,
  };
}
