import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { GroupService } from '../../core/services/group.service';
import { Group, UpcomingAssignment } from '../../core/models/group.models';

interface AssignmentWithGroup extends UpcomingAssignment {
    groupId: number;
    groupName: string;
}

@Component({
    selector: 'app-groups',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './groups.component.html',
    styleUrl: './groups.component.css'
})
export class GroupsComponent implements OnInit, OnDestroy {
    private readonly UPCOMING_DAYS = 30;

    groups: Group[] = [];
    upcomingAssignments: AssignmentWithGroup[] = [];
    laterAssignments: AssignmentWithGroup[] = [];
    isLoading = true;
    errorMessage = '';
    isTeacher = false;
    isStudent = false;
    private subscription?: Subscription;

    constructor(
        private groupService: GroupService,
        private authService: AuthService,
        private router: Router
    ) {
        this.isTeacher = this.authService.isTeacher();
        this.isStudent = this.authService.isStudent();
    }

    ngOnInit(): void {
        this.loadGroups();
    }

    loadGroups(): void {
        this.isLoading = true;
        this.errorMessage = '';

        const groups$ = this.isStudent
            ? this.groupService.getUserGroupsWithUpcoming()
            : this.groupService.getUserGroups();

        this.subscription = groups$.subscribe({
            next: (groups) => {
                this.groups = groups;
                this.processUpcomingAssignments();
                this.isLoading = false;
            },
            error: (error) => {
                this.errorMessage = error.error?.message || 'Failed to load groups';
                this.isLoading = false;
            }
        });
    }

    private processUpcomingAssignments(): void {
        const allAssignments: AssignmentWithGroup[] = [];
        const now = new Date();
        const thirtyDaysFromNow = new Date(now.getTime() + (this.UPCOMING_DAYS * 24 * 60 * 60 * 1000));

        this.groups.forEach(group => {
            if (group.upcomingAssignments) {
                group.upcomingAssignments.forEach(assignment => {
                    allAssignments.push({
                        ...assignment,
                        groupId: group.id,
                        groupName: group.name
                    });
                });
            }
        });

        this.upcomingAssignments = allAssignments.filter(a => {
            if (!a.dueDate) return false;
            const dueDate = new Date(a.dueDate);
            return dueDate <= thirtyDaysFromNow;
        });

        this.laterAssignments = allAssignments.filter(a => {
            if (!a.dueDate) return true;
            const dueDate = new Date(a.dueDate);
            return dueDate > thirtyDaysFromNow;
        });
    }

    navigateToCreateGroup(): void {
        this.router.navigate(['/groups/create']);
    }

    navigateToJoinGroup(): void {
        this.router.navigate(['/groups/join']);
    }

    navigateToGroupDetail(groupId: number): void {
        this.router.navigate(['/groups', groupId]);
    }

    takeAssignment(groupId: number, assignmentId: number): void {
        this.router.navigate(['/groups', groupId, 'assignments', assignmentId, 'take']);
    }

    formatDueDate(dueDate: string): string {
        if (!dueDate) return 'No due date';
        
        const date = new Date(dueDate);
        const now = new Date();
        const diffTime = date.getTime() - now.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays === 0) return 'Due today';
        if (diffDays === 1) return 'Due tomorrow';
        if (diffDays < 7) return `Due in ${diffDays} days`;
        
        return `Due ${date.toLocaleDateString()}`;
    }

    formatLaterDate(dueDate: string): string {
        const date = new Date(dueDate);
        return `Due ${date.toLocaleDateString()}`;
    }

    ngOnDestroy(): void {
        this.subscription?.unsubscribe();
    }
}