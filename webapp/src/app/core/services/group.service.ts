import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateGroupRequest, Group, GroupMember, JoinGroupRequest, UpcomingAssignment } from '../models/group.models';
import { AssignmentService } from './assignment.service';
import { StudentAssignmentSummary } from '../models/assignment.models';

@Injectable({ providedIn: 'root' })
export class GroupService {
    private baseUrl = `${environment.apiUrl}/groups`;

    constructor(
        private http: HttpClient,
        private assignmentService: AssignmentService
    ) {}

    createGroup(request: CreateGroupRequest): Observable<Group> { 
        return this.http.post<Group>(this.baseUrl, request); 
    }

    joinGroup(request: JoinGroupRequest): Observable<Group> { 
        return this.http.post<Group>(`${this.baseUrl}/join`, request); 
    }

    getUserGroups(): Observable<Group[]> { 
        return this.http.get<Group[]>(this.baseUrl); 
    }

    getUserGroupsWithUpcoming(): Observable<Group[]> {
        return this.getUserGroups().pipe(
            switchMap(groups => {
                if (groups.length === 0) {
                    return of([]);
                }
                
                const groupsWithAssignments$ = groups.map(group => 
                    this.assignmentService.getGroupAssignments(group.id).pipe(
                        map(assignments => ({
                            ...group,
                            upcomingAssignments: this.filterUpcoming(assignments)
                        }))
                    )
                );
                
                return forkJoin(groupsWithAssignments$);
            })
        );
    }

    private filterUpcoming(assignments: StudentAssignmentSummary[]): UpcomingAssignment[] {
        const now = new Date();
        
        return assignments
            .filter(a => {
                if (a.isExpired) return false;
                
                if (a.maxAttempts !== null && a.maxAttempts !== undefined && a.attemptsUsed >= a.maxAttempts) {
                    return false;
                }
                
                return true;
            })
            .map(a => ({
                id: a.id,
                title: a.title,
                dueDate: a.dueDate || '',
                totalPoints: a.totalPoints
            }))
            .sort((a, b) => {
                if (!a.dueDate) return 1;
                if (!b.dueDate) return -1;
                return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
            });
    }

    getGroupMembers(groupId: number): Observable<GroupMember[]> { 
        return this.http.get<GroupMember[]>(`${this.baseUrl}/${groupId}/members`); 
    }

    leaveGroup(groupId: number): Observable<void> { 
        return this.http.post<void>(`${this.baseUrl}/${groupId}/leave`, {}); 
    }

    deleteGroup(groupId: number): Observable<void> { 
        return this.http.delete<void>(`${this.baseUrl}/${groupId}`); 
    }
}