import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LessonProgress,
  ModuleProgress,
  ContinueLearning,
  MarkLessonCompleteRequest,
  PrerequisiteCheckResponse
} from '../models/lesson-progress.models';

@Injectable({ providedIn: 'root' })
export class LessonProgressService {
  private baseUrl = `${environment.apiUrl}/lessons`;
  
  private progressUpdatedSubject = new BehaviorSubject<boolean>(false);
  public progressUpdated$ = this.progressUpdatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  getUserProgress(): Observable<LessonProgress[]> {
    return this.http.get<LessonProgress[]>(`${this.baseUrl}/progress`);
  }

  getModuleProgress(): Observable<ModuleProgress[]> {
    return this.http.get<ModuleProgress[]>(`${this.baseUrl}/progress/modules`);
  }

  markLessonComplete(lessonId: string): Observable<LessonProgress> {
    return this.http
      .post<LessonProgress>(`${this.baseUrl}/${lessonId}/complete`, {})
      .pipe(
        tap(() => this.notifyProgressUpdate())
      );
  }

  recordLessonAccess(lessonId: string): Observable<void> {
    return this.http
      .post<void>(`${this.baseUrl}/${lessonId}/access`, {})
      .pipe(
        tap(() => this.notifyProgressUpdate())
      );
  }

  getContinueLearning(): Observable<ContinueLearning | null> {
    return this.http.get<ContinueLearning | null>(`${this.baseUrl}/progress/continue`);
  }

  checkPrerequisites(lessonId: string): Observable<PrerequisiteCheckResponse> {
    return this.http.get<PrerequisiteCheckResponse>(`${this.baseUrl}/${lessonId}/prerequisites/check`);
  }

  isLessonCompleted(lessonId: string, progressList: LessonProgress[]): boolean {
    const progress = progressList.find(p => p.lessonId === lessonId);
    return progress?.completed ?? false;
  }

  private notifyProgressUpdate(): void {
    this.progressUpdatedSubject.next(true);
  }
}