import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  DifficultyLevel,
  DifficultyPromptResponse,
  RecordAttemptRequest,
  UpdateDifficultyRequest,
  DifficultyLevelResponse
} from '../models/difficulty.models';

@Injectable({
  providedIn: 'root'
})
export class DifficultyService {
  private baseUrl = `${environment.apiUrl}/difficulty`;
  
  private currentDifficultySubject = new BehaviorSubject<DifficultyLevel>(DifficultyLevel.MEDIUM);
  public currentDifficulty$ = this.currentDifficultySubject.asObservable();

  constructor(private http: HttpClient) {}

  getCurrentDifficulty(): Observable<DifficultyLevelResponse> {
    return this.http.get<DifficultyLevelResponse>(this.baseUrl).pipe(
      tap(response => {
        this.currentDifficultySubject.next(response.difficultyLevel);
      })
    );
  }

  recordAttempt(request: RecordAttemptRequest): Observable<DifficultyPromptResponse> {
    return this.http.post<DifficultyPromptResponse>(`${this.baseUrl}/record-attempt`, request);
  }

  updateDifficulty(level: DifficultyLevel): Observable<void> {
    const request: UpdateDifficultyRequest = { difficultyLevel: level };
    return this.http.put<void>(this.baseUrl, request).pipe(
      tap(() => {
        this.currentDifficultySubject.next(level);
      })
    );
  }

  recordPromptShown(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/prompt-shown`, {});
  }

  getCurrentDifficultySync(): DifficultyLevel {
    return this.currentDifficultySubject.value;
  }
}