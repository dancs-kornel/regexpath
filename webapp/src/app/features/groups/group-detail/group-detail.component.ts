import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { GroupService } from '../../../core/services/group.service';
import { AuthService } from '../../../core/services/auth.service';
import { AssignmentService } from '../../../core/services/assignment.service';
import { Group, GroupMember } from '../../../core/models/group.models';
import { StudentAssignmentSummary } from '../../../core/models/assignment.models';
import { AssignAssignmentModalComponent } from '../../teacher/assignments/assign-assignment-modal/assign-assignment-modal.component';
import { AssignmentPreviewComponent } from '../../teacher/assignments/assignment-preview/assignment-preview.component';
import { NotificationService } from '../../../shared/services/notification.service';

type AssignmentStatusType = 'not-started' | 'in-progress' | 'completed' | 'expired';

@Component({
    selector: 'app-group-detail',
    standalone: true,
    imports: [CommonModule, AssignAssignmentModalComponent],
    templateUrl: './group-detail.component.html',
    styleUrl: './group-detail.component.css'
})
export class GroupDetailComponent implements OnInit, OnDestroy {
    group: Group | null = null;
    members: GroupMember[] = [];
    assignments: StudentAssignmentSummary[] = [];
    isLoading = true;
    errorMessage = '';
    isTeacher = false;
    isStudent = false;
    currentUserId: number | null = null;

    totalPointsEarned = 0;
    totalPointsPossible = 0;
    percentageGrade = 0;

    assignModal = {
        show: false,
        groupId: 0
    };

    private subscriptions = new Subscription();

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private groupService: GroupService,
        private authService: AuthService,
        private assignmentService: AssignmentService,
        private notificationService: NotificationService
    ) {
        this.isTeacher = this.authService.isTeacher();
        this.isStudent = this.authService.isStudent();
        this.currentUserId = this.authService.getCurrentUser()?.id || null;
    }

    ngOnInit(): void {
        const groupId = Number(this.route.snapshot.paramMap.get('id'));
        if (groupId) {
            this.loadGroupDetails(groupId);
        }
    }

    loadGroupDetails(groupId: number): void {
        this.isLoading = true;
        this.errorMessage = '';

        this.subscriptions.add(
            this.groupService.getUserGroups().subscribe({
                next: (groups) => {
                    this.group = groups.find(g => g.id === groupId) || null;
                    if (this.group) {
                        this.loadMembers(groupId);
                        this.loadAssignments(groupId);
                    } else {
                        this.errorMessage = 'Group not found';
                        this.isLoading = false;
                    }
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Failed to load group';
                    this.isLoading = false;
                }
            })
        );
    }

    loadMembers(groupId: number): void {
        this.subscriptions.add(
            this.groupService.getGroupMembers(groupId).subscribe({
                next: (members) => {
                    this.members = members;
                    this.isLoading = false;
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Failed to load members';
                    this.isLoading = false;
                }
            })
        );
    }

    loadAssignments(groupId: number): void {
        this.subscriptions.add(
            this.assignmentService.getGroupAssignments(groupId).subscribe({
                next: (assignments) => {
                    this.assignments = assignments;
                    this.calculateStatistics();
                },
                error: (error) => {
                    console.error('Error loading assignments:', error);
                }
            })
        );
    }

    calculateStatistics(): void {
        if (!this.isStudent) return;

        this.totalPointsPossible = this.assignments.reduce(
            (sum, assignment) => sum + assignment.totalPoints,
            0
        );

        this.totalPointsEarned = this.assignments.reduce((sum, assignment) => {
            const earned = assignment.bestScore ?? 0;
            return sum + earned;
        }, 0);

        this.percentageGrade = this.totalPointsPossible > 0
            ? (this.totalPointsEarned / this.totalPointsPossible) * 100
            : 0;
    }

    getAssignmentStatus(assignment: StudentAssignmentSummary): AssignmentStatusType {
        if (assignment.isExpired) return 'expired';
        if (assignment.hasActiveAttempt) return 'in-progress';
        if (assignment.attemptsUsed > 0) return 'completed';
        return 'not-started';
    }

    canStartNew(assignment: StudentAssignmentSummary): boolean {
        if (assignment.isExpired) return false;
        if (assignment.hasActiveAttempt) return false;

        if (assignment.maxAttempts !== null && assignment.maxAttempts !== undefined) {
            return assignment.attemptsUsed < assignment.maxAttempts;
        }

        return true; 
    }

    canContinue(assignment: StudentAssignmentSummary): boolean {
        return assignment.hasActiveAttempt && !assignment.isExpired;
    }

    canViewAttempts(assignment: StudentAssignmentSummary): boolean {
        return assignment.attemptsUsed > 0;
    }

    getAssignmentStatusClass(assignment: StudentAssignmentSummary): string {
        return this.getAssignmentStatus(assignment);
    }

    getAssignmentStatusText(assignment: StudentAssignmentSummary): string {
        const status = this.getAssignmentStatus(assignment);

        switch (status) {
            case 'expired':
                return 'Lejárt';
            case 'in-progress':
                return 'Folyamatban';
            case 'completed':
                if (assignment.bestScore !== undefined && assignment.bestScore !== null) {
                    return `Teljesítve (${assignment.bestScore}/${assignment.totalPoints})`;
                }
                return 'Teljesítve';
            case 'not-started':
                return 'Nincs elkezdve';
            default:
                return 'Ismeretlen';
        }
    }

    startAssignment(assignmentId: number): void {
        this.router.navigate(['/assignments', assignmentId, 'take'], {
            queryParams: { groupId: this.group?.id }
        });
    }

    viewAttempts(assignmentId: number): void {
        this.router.navigate(['/assignments', assignmentId, 'attempts'], {
            queryParams: { groupId: this.group?.id }
        });
    }

    openAssignModal(): void {
        if (this.group) {
            this.assignModal = {
                show: true,
                groupId: this.group.id
            };
        }
    }

    closeAssignModal(): void {
        this.assignModal = {
            show: false,
            groupId: 0
        };
    }

    onAssignmentAssigned(): void {
        if (this.group) {
            this.loadAssignments(this.group.id);
        }
    }

    leaveGroup(): void {
        if (!this.group || !confirm('Are you sure you want to leave this group?')) {
            return;
        }

        this.subscriptions.add(
            this.groupService.leaveGroup(this.group.id).subscribe({
                next: () => {
                    this.router.navigate(['/groups']);
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Failed to leave group';
                }
            })
        );
    }

    deleteGroup(): void {
        if (!this.group || !confirm('Are you sure you want to delete this group? This action cannot be undone.')) {
            return;
        }

        this.subscriptions.add(
            this.groupService.deleteGroup(this.group.id).subscribe({
                next: () => {
                    this.router.navigate(['/groups']);
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Failed to delete group';
                }
            })
        );
    }

    goBack(): void {
        this.router.navigate(['/groups']);
    }

    previewAssignment(assignmentId: number): void {
        this.router.navigate(['/teacher/assignments', assignmentId, 'preview']);
    }

    unassignAssignment(assignmentId: number): void {
        if (!confirm('Remove this assignment from the group?')) return;

        if (!this.group) return;

        this.subscriptions.add(
            this.assignmentService.unassignFromGroup(assignmentId, this.group.id).subscribe({
                next: () => {
                    this.notificationService.showSuccess('Assignment removed from group');
                    this.loadAssignments(this.group!.id);
                },
                error: (error) => {
                    this.notificationService.showError(
                        error.error?.message || 'Failed to remove assignment'
                    );
                }
            })
        );

    }

    viewGroupStatistics(): void {
        if (this.group) {
            this.router.navigate(['/teacher/groups', this.group.id, 'statistics']);
        }
    }

    viewStudentProgress(studentId: number): void {
        if (this.group) {
            this.router.navigate(['/teacher/groups', this.group.id, 'students', studentId, 'progress']);
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }
}