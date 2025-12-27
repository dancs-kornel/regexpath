import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import {
  Assignment,
  AssignmentSummary,
  CreateAssignmentRequest,
  UpdateAssignmentRequest,
  AssignGroupRequest,
  ExerciseUnion,
  AddExerciseRequest,
  StudentAssignmentSummary,
  StudentAssignmentDetail,
  AssignmentAttempt,
  AttemptSummary,
  SubmitAttemptRequest
} from '../models/assignment.models';

import {
  adaptAssignment,
  BackendAssignmentResponse,
} from '../adapters/assignment.adapter';

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  private baseUrl = `${environment.apiUrl}/assignments`;
  private groupsBaseUrl = `${environment.apiUrl}/groups`;

  constructor(private http: HttpClient) { }

  createAssignment(request: CreateAssignmentRequest): Observable<Assignment> {
    return this.http.post<Assignment>(this.baseUrl, request);
  }
  getMyAssignments(): Observable<AssignmentSummary[]> {
    return this.http.get<AssignmentSummary[]>(`${this.baseUrl}`);
  }
  getAssignment(id: number): Observable<Assignment> {
    return this.http.get<Assignment>(`${this.baseUrl}/${id}`);
  }
  updateAssignment(id: number, request: UpdateAssignmentRequest): Observable<Assignment> {
    return this.http.put<Assignment>(`${this.baseUrl}/${id}`, request);
  }
  addExercise(assignmentId: number, exercise: AddExerciseRequest): Observable<ExerciseUnion> {
    return this.http.post<ExerciseUnion>(`${this.baseUrl}/${assignmentId}/exercises`, exercise);
  }
  updateExercise(assignmentId: number, exerciseId: number, exercise: AddExerciseRequest): Observable<ExerciseUnion> {
    return this.http.put<ExerciseUnion>(`${this.baseUrl}/${assignmentId}/exercises/${exerciseId}`, exercise);
  }
  deleteAssignment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
  publishAssignment(id: number): Observable<Assignment> {
    return this.http.post<Assignment>(`${this.baseUrl}/${id}/submit`, {});
  }
  revertToDraft(id: number): Observable<Assignment> {
    return this.http.post<Assignment>(`${this.baseUrl}/${id}/revert-to-draft`, {});
  }

  assignToGroups(assignmentId: number, groupIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${assignmentId}/assign`, { groupIds });
  }
  unassignFromGroup(assignmentId: number, groupId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${assignmentId}/groups/${groupId}`);
  }

  getGroupAssignments(groupId: number): Observable<StudentAssignmentSummary[]> {
    return this.http
      .get<any[]>(`${this.groupsBaseUrl}/${groupId}/assignments`)
      .pipe(
        map(list =>
          list.map(a => ({
            ...a,
            maxAttempts: a.maxAttempts ?? a.attemptLimit ?? null,
            timeLimitMinutes: a.timeLimitMinutes ?? a.timeLimit ?? null,
          }))
        )
      );
  }

  getAssignmentForStudent(groupId: number, assignmentId: number): Observable<StudentAssignmentDetail> {
    return this.http
      .get<BackendAssignmentResponse>(`${this.groupsBaseUrl}/${groupId}/assignments/${assignmentId}`)
      .pipe(map(resp => adaptAssignment(resp)));
  }

  startAttempt(groupId: number, assignmentId: number): Observable<AssignmentAttempt> {
    return this.http.post<AssignmentAttempt>(
      `${this.groupsBaseUrl}/${groupId}/assignments/${assignmentId}/attempts`,
      {}
    );
  }

  submitAttempt(
    groupId: number,
    assignmentId: number,
    attemptId: number,
    request: SubmitAttemptRequest
  ): Observable<AssignmentAttempt> {
    return this.http.post<AssignmentAttempt>(
      `${this.groupsBaseUrl}/${groupId}/assignments/${assignmentId}/attempts/${attemptId}/submit`,
      request
    );
  }

  getAttemptResults(groupId: number, assignmentId: number, attemptId: number): Observable<AssignmentAttempt> {
    return this.http.get<AssignmentAttempt>(
      `${this.groupsBaseUrl}/${groupId}/assignments/${assignmentId}/attempts/${attemptId}`
    );
  }

  getStudentAttempts(groupId: number, assignmentId: number): Observable<AttemptSummary[]> {
    return this.http.get<AttemptSummary[]>(
      `${this.groupsBaseUrl}/${groupId}/assignments/${assignmentId}/attempts`
    );
  }

  continueAttempt(groupId: number, assignmentId: number): Observable<AssignmentAttempt> {
    return this.http.get<AssignmentAttempt>(
      `${this.groupsBaseUrl}/${groupId}/assignments/${assignmentId}/attempts/active`
    );
  }

  previewAssignment(id: number): Observable<StudentAssignmentDetail> {
    return this.http
      .get<BackendAssignmentResponse>(`${this.baseUrl}/${id}/preview`)
      .pipe(map(resp => adaptAssignment(resp)));
  }
  calculateTotalPoints(assignment: Assignment): number {
    return assignment.exercises.reduce((sum, ex) => sum + ex.points, 0);
  }
}
