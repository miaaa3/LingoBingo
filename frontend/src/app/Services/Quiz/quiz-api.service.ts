import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';

import { catchError, map, Observable, of, OperatorFunction } from 'rxjs';
import { Question } from '../../Models/quiz/question.model'; 
import { ApiEndPoints } from './api-endpoints.service';

@Injectable({
  providedIn: 'root',
})
export class QuizApiService {
  constructor(private http: HttpClient,private api:ApiEndPoints) {}
    
  
  getQuestions(): Observable<Question[]> {
    return this.http.get(this.api.QUIZ_API_ENDPOINTS.questions).pipe(
      this.mappeddata()
    );
  }
  mappeddata():OperatorFunction<any,Question[]>{
    return map((data: any) => {
      if (Array.isArray(data)) {
        console.error(data);
        return false
        // data.map((questionData: any) => Question(questionData)) as Question[];
      } else {
        console.error('Invalid response format:', data);
        return [];
      }
    }),
    catchError((error) => {
      console.error('Error fetching questions:', error);
      return of([]);
    })
  }

}
