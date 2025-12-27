
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AssignmentService } from '../../../core/services/assignment.service';
import { AttemptSummary, StudentAssignmentDetail } from '../../../core/models/assignment.models';

@Component({
    selector: 'app-assignment-attempts',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './assignment-attempts.component.html',
    styleUrl: './assignment-attempts.component.css'
})
export class AssignmentAttemptsComponent implements OnInit, OnDestroy {
    assignment: StudentAssignmentDetail | null = null;
    attempts: AttemptSummary[] = [];

    groupId: number = 0;
    assignmentId: number = 0;

    isLoading = true;
    error = '';

    bestAttemptId: number | null = null;

    private subscriptions = new Subscription();

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private assignmentService: AssignmentService
    ) {}

    ngOnInit(): void {
        this.assignmentId = Number(this.route.snapshot.paramMap.get('id'));
        this.groupId = Number(this.route.snapshot.queryParamMap.get('groupId'));

        if (!this.groupId) {
            this.error = 'Group ID is required';
            this.isLoading = false;
            return;
        }

        this.loadData();
    }

    loadData(): void {
        this.isLoading = true;
        this.error = '';

        this.subscriptions.add(
            this.assignmentService.getAssignmentForStudent(this.groupId, this.assignmentId).subscribe({
                next: (assignment) => {
                    this.assignment = assignment;
                    this.loadAttempts();
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to load assignment';
                    this.isLoading = false;
                }
            })
        );
    }

    loadAttempts(): void {
        this.subscriptions.add(
            this.assignmentService.getStudentAttempts(this.groupId, this.assignmentId).subscribe({
                next: (attempts) => {
                    this.attempts = attempts.sort((a, b) => b.attemptNumber - a.attemptNumber);

                    if (attempts.length > 0) {
                        const best = attempts.reduce((prev, curr) =>
                            curr.score > prev.score ? curr : prev
                        );
                        this.bestAttemptId = best.id;
                    }

                    this.isLoading = false;
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to load attempts';
                    this.isLoading = false;
                }
            })
        );
    }

    viewAttempt(attemptId: number): void {
        this.router.navigate(['/assignments', this.assignmentId, 'attempts', attemptId], {
            queryParams: { groupId: this.groupId }
        });
    }

    retakeAssignment(): void {
        if (!this.assignment) return;

        const canRetake = this.assignment.maxAttempts === null || 
                         this.assignment.maxAttempts === undefined ||
                         this.assignment.attemptsUsed < this.assignment.maxAttempts;

        if (!canRetake) {
            alert('You have used all available attempts for this assignment.');
            return;
        }

        if (this.assignment.isExpired) {
            alert('This assignment has expired.');
            return;
        }

        this.router.navigate(['/assignments', this.assignmentId, 'take'], {
            queryParams: { groupId: this.groupId }
        });
    }

    goBack(): void {
        this.router.navigate(['/groups', this.groupId]);
    }

    isBestAttempt(attemptId: number): boolean {
        return attemptId === this.bestAttemptId;
    }

    getScoreClass(percentageScore: number): string {
        if (percentageScore >= 90) return 'excellent';
        if (percentageScore >= 70) return 'good';
        if (percentageScore >= 50) return 'average';
        return 'poor';
    }

    canRetake(): boolean {
        if (!this.assignment) return false;
        if (this.assignment.isExpired) return false;
        if (this.assignment.hasActiveAttempt) return false;

        return this.assignment.maxAttempts === null ||
               this.assignment.maxAttempts === undefined ||
               this.assignment.attemptsUsed < this.assignment.maxAttempts;
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }
}