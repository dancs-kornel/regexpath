import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
    AssignmentStatistics,
    GroupStatistics,
    StudentProgress,
    TeacherAttemptView
} from '../models/statistics.models';

@Injectable({ providedIn: 'root' })
export class StatisticsService {
    private baseUrl = `${environment.apiUrl}/teacher`;

    constructor(private http: HttpClient) {}

    getAssignmentStatistics(groupId: number, assignmentId: number): Observable<AssignmentStatistics> {
        return this.http.get<AssignmentStatistics>(
            `${this.baseUrl}/groups/${groupId}/assignments/${assignmentId}/statistics`
        );
    }

    getGroupStatistics(groupId: number): Observable<GroupStatistics> {
        return this.http.get<GroupStatistics>(
            `${this.baseUrl}/groups/${groupId}/statistics`
        );
    }

    getStudentProgress(groupId: number, studentId: number): Observable<StudentProgress> {
        return this.http.get<StudentProgress>(
            `${this.baseUrl}/groups/${groupId}/students/${studentId}/progress`
        );
    }

    getTeacherAttemptView(attemptId: number): Observable<TeacherAttemptView> {
        return this.http.get<TeacherAttemptView>(
            `${this.baseUrl}/attempts/${attemptId}/view`
        );
    }

    exportGroupStatistics(groupId: number): Observable<Blob> {
        return this.http.get(
            `${this.baseUrl}/groups/${groupId}/statistics/export`,
            { responseType: 'blob' }
        );
    }
}