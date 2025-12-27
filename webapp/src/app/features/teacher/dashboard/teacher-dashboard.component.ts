import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AssignmentService } from '../../../core/services/assignment.service';
import { GroupService } from '../../../core/services/group.service';
import { AssignmentSummary, AssignmentStatus } from '../../../core/models/assignment.models';
import { Group } from '../../../core/models/group.models';

@Component({
    selector: 'app-teacher-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './teacher-dashboard.component.html',
    styleUrl: './teacher-dashboard.component.css'
})
export class TeacherDashboardComponent implements OnInit {
    assignments: AssignmentSummary[] = [];
    groups: Group[] = [];
    isLoading = true;
    error = '';

    totalAssignments = 0;
    publishedAssignments = 0;
    draftAssignments = 0;
    totalGroups = 0;
    totalStudents = 0;
    upcomingDueDates: AssignmentSummary[] = [];

    constructor(
        private assignmentService: AssignmentService,
        private groupService: GroupService
    ) {}

    ngOnInit(): void {
        this.loadDashboardData();
    }

    loadDashboardData(): void {
        this.isLoading = true;
        
        this.assignmentService.getMyAssignments().subscribe({
            next: (assignments) => {
                this.assignments = assignments;
                this.calculateStatistics();
                this.isLoading = false;
            },
            error: (error) => {
                this.error = 'Failed to load assignments';
                this.isLoading = false;
                console.error('Error loading assignments:', error);
            }
        });

        this.groupService.getUserGroups().subscribe({
            next: (groups) => {
                this.groups = groups;
                this.totalGroups = groups.length;
                this.totalStudents = groups.reduce((sum, g) => sum + (g.memberCount || 0), 0);
            },
            error: (error) => {
                console.error('Error loading groups:', error);
            }
        });
    }

    calculateStatistics(): void {
        this.totalAssignments = this.assignments.length;
        this.publishedAssignments = this.assignments.filter(a => a.status === AssignmentStatus.SUBMITTED).length;
        this.draftAssignments = this.assignments.filter(a => a.status === AssignmentStatus.DRAFT).length;
        
        const now = new Date();
        const nextWeek = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
        
        this.upcomingDueDates = this.assignments
            .filter(a => {
                if (!a.dueDate) return false;
                const dueDate = new Date(a.dueDate);
                return dueDate >= now && dueDate <= nextWeek;
            })
            .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())
            .slice(0, 5);
    }

    getStatusBadgeClass(status: AssignmentStatus): string {
        return status === AssignmentStatus.SUBMITTED ? 'published' : 'draft';
    }
}