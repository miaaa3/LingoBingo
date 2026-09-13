import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class ApiEndPoints {
  
  base = 'https://quizapi.io/api/v1';
  token = 'REDACTED';
  
  QUIZ_API_ENDPOINTS = {
    questions: `${this.base}/questions?apiKey=${this.token}`,
    questionByCategory: (category: string) => `${this.base}/questions?apiKey=${this.token}&limit=10&category=${category}`,
    questionByDifficultyAndCategory: (difficulty: string, category: string) => `${this.base}/questions?apiKey=${this.token}&limit=10&difficulty=${difficulty}&category=${category}`,
    questionByDifficulty: (difficulty: string) => `${this.base}/questions?apiKey=${this.token}&limit=10&difficulty=${difficulty}`,

  };
}