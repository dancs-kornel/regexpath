import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription, combineLatest } from 'rxjs';
import { AssignmentService } from '../../../core/services/assignment.service';
import { AssignmentAttempt, AttemptSummary, StudentAssignmentDetail, ExerciseType } from '../../../core/models/assignment.models';

@Component({
    selector: 'app-attempt-results',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './attempt-results.component.html',
    styleUrl: './attempt-results.component.css'
})
export class AttemptResultsComponent implements OnInit, OnDestroy {
    attempt: AssignmentAttempt | null = null;
    assignment: StudentAssignmentDetail | null = null;
    allAttempts: AttemptSummary[] = [];

    groupId: number = 0;
    assignmentId: number = 0;
    attemptId: number = 0;

    isLoading = true;
    error = '';

    private subscriptions = new Subscription();

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private assignmentService: AssignmentService
    ) { }

    ngOnInit(): void {
        this.subscriptions.add(
            combineLatest([
                this.route.paramMap,
                this.route.queryParamMap
            ]).subscribe(([params, query]) => {
                this.assignmentId = Number(params.get('id'));
                this.attemptId = Number(params.get('attemptId'));
                this.groupId = Number(query.get('groupId'));

                if (!this.groupId) {
                    this.error = 'Group ID is required';
                    this.isLoading = false;
                    return;
                }

                this.loadAttemptResults();
                window.scrollTo({ top: 0, behavior: 'instant' as ScrollBehavior });
            })
        );
    }

    loadAttemptResults(): void {
        this.isLoading = true;
        this.error = '';

        this.subscriptions.add(
            this.assignmentService.getAssignmentForStudent(this.groupId, this.assignmentId).subscribe({
                next: (assignment) => {
                    this.assignment = assignment;

                    this.loadAttempt();

                    this.loadAllAttempts();
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to load assignment';
                    this.isLoading = false;
                    console.error('Error loading assignment:', error);
                }
            })
        );
    }

    loadAttempt(): void {
        this.subscriptions.add(
            this.assignmentService.getAttemptResults(this.groupId, this.assignmentId, this.attemptId).subscribe({
                next: (attempt) => {
                    this.attempt = attempt;
                    this.isLoading = false;
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to load attempt results';
                    this.isLoading = false;
                    console.error('Error loading attempt:', error);
                }
            })
        );
    }

    loadAllAttempts(): void {
        this.subscriptions.add(
            this.assignmentService.getStudentAttempts(this.groupId, this.assignmentId).subscribe({
                next: (attempts) => {
                    this.allAttempts = attempts.sort((a, b) => b.attemptNumber - a.attemptNumber);
                },
                error: (error) => {
                    console.error('Error loading attempts:', error);
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
        this.router.navigate(['/assignments', this.assignmentId, 'take'], {
            queryParams: { groupId: this.groupId }
        });
    }

    goBack(): void {
        this.router.navigate(['/groups', this.groupId]);
    }

    getExerciseById(exerciseId: number) {
        return this.assignment?.exercises.find(ex => ex.id === exerciseId);
    }

    parseAnswerJson(answerJson: string): any {
        try {
            return JSON.parse(answerJson);
        } catch {
            return {};
        }
    }

    getScoreClass(): string {
        if (!this.attempt) return '';
        const percentage = this.attempt.percentageScore;
        if (percentage >= 90) return 'excellent';
        if (percentage >= 70) return 'good';
        if (percentage >= 50) return 'average';
        return 'poor';
    }

    isHighestScore(attemptId: number): boolean {
        if (this.allAttempts.length === 0) return false;
        const maxScore = Math.max(...this.allAttempts.map(a => a.score));
        const thisAttempt = this.allAttempts.find(a => a.id === attemptId);
        return thisAttempt?.score === maxScore;
    }


    getOptionText(exercise: any, key: number | string): string {
        const opts = (exercise?.options ?? []) as Array<{ id: string; text: string } | string>;
        if (!Array.isArray(opts)) return String(key);

        if (typeof key === 'number') {
            const v = opts[key] as any;
            return typeof v === 'string' ? v : (v?.text ?? String(key));
        }
        const byId = (opts as any[]).find(o => typeof o === 'object' && o?.id === key);
        if (byId) return byId.text ?? String(key);

        return String(key);
    }
    getUserAnswer(answerJson: string, exerciseType: ExerciseType | string): string {
        const obj = this.parseAnswerJson(answerJson) ?? {};
        switch (exerciseType) {
            case ExerciseType.REGEX:
            case 'REGEX_SANDBOX':
                return obj.pattern ?? obj.userAnswer ?? obj.value ?? obj.answer ?? '';
            case ExerciseType.XPATH:
            case 'XPATH_SANDBOX':
                return obj.expression ?? obj.userAnswer ?? obj.value ?? obj.answer ?? '';
            default:
                return obj.userAnswer ?? obj.value ?? obj.answer ?? '';
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }
}