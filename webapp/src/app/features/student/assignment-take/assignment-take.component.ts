import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AssignmentService } from '../../../core/services/assignment.service';
import { NotificationService } from '../../../shared/services/notification.service';
import {
    StudentAssignmentDetail,
    AssignmentAttempt,
    ExerciseAnswerRequest,
    ExerciseUnion,
    ExerciseType
} from '../../../core/models/assignment.models';

import { RadioComponent } from '../../lessons/exercises/radio/radio.component';
import { MultipleChoiceComponent } from '../../lessons/exercises/multiple-choice/multiple-choice.component';
import { RegexSandboxComponent } from '../../lessons/exercises/regex-sandbox/regex-sandbox.component';
import { XPathSandboxExerciseComponent } from '../../lessons/exercises/xpath-sandbox-exercise/xpath-sandbox-exercise.component';

@Component({
    selector: 'app-assignment-take',
    standalone: true,
    imports: [
        CommonModule,
        RadioComponent,
        MultipleChoiceComponent,
        RegexSandboxComponent,
        XPathSandboxExerciseComponent
    ],
    templateUrl: './assignment-take.component.html',
    styleUrl: './assignment-take.component.css'
})
export class AssignmentTakeComponent implements OnInit, OnDestroy {
    assignment: StudentAssignmentDetail | null = null;
    attempt: AssignmentAttempt | null = null;
    groupId: number = 0;
    assignmentId: number = 0;

    isLoading = true;
    isSubmitting = false;
    error = '';

    timeRemaining: number = 0;
    timerInterval: ReturnType<typeof setInterval> | undefined;

    exerciseAnswers: Map<number, any> = new Map();

    private subscriptions = new Subscription();

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private assignmentService: AssignmentService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.assignmentId = Number(this.route.snapshot.paramMap.get('id'));
        this.groupId = Number(this.route.snapshot.queryParamMap.get('groupId'));

        if (!this.groupId) {
            this.error = 'Group ID is required';
            this.isLoading = false;
            return;
        }

        this.loadAssignmentAndStartAttempt();
    }

    ngOnDestroy(): void {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
        }
        this.subscriptions.unsubscribe();
    }

    loadAssignmentAndStartAttempt(): void {
        this.isLoading = true;
        this.error = '';

        this.subscriptions.add(
            this.assignmentService.getAssignmentForStudent(this.groupId, this.assignmentId).subscribe({
                next: (assignment) => {
                    console.log('Assignment loaded: ', assignment);
                    console.log('Assignment hasActiveAttempt: ', assignment.hasActiveAttempt);
                    this.assignment = assignment;

                    if (assignment.hasActiveAttempt) {
                        console.log('Loading active attempt');
                        this.loadActiveAttempt();
                    } else {
                        console.log('Starting new attempt')
                        this.startNewAttempt();
                    }
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to load assignment';
                    this.isLoading = false;
                    console.error('Error loading assignment:', error);
                }
            })
        );
    }

    loadActiveAttempt(): void {
        this.subscriptions.add(
            this.assignmentService.continueAttempt(this.groupId, this.assignmentId).subscribe({
                next: (attempt) => {
                    this.attempt = attempt;
                    this.isLoading = false;
                    if (attempt.expiresAt) {
                        this.startTimer(attempt.expiresAt);
                    }
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to load active attempt';
                    this.isLoading = false;
                }
            })
        );
    }

    startNewAttempt(): void {
        this.subscriptions.add(
            this.assignmentService.startAttempt(this.groupId, this.assignmentId).subscribe({
                next: (attempt) => {
                    this.attempt = attempt;
                    this.isLoading = false;

                    if (attempt.expiresAt) {
                        this.startTimer(attempt.expiresAt);
                    }
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to start attempt';
                    this.isLoading = false;
                    console.error('Error starting attempt:', error);
                }
            })
        );
    }

    startTimer(expiresAt: string): void {
        const updateTimer = () => {
            const now = new Date().getTime();
            const expiry = new Date(expiresAt).getTime();
            const diff = Math.floor((expiry - now) / 1000);

            if (diff <= 0) {
                this.timeRemaining = 0;
                clearInterval(this.timerInterval);
                this.notificationService.showError('Time expired! Submitting automatically...');
                setTimeout(() => this.submitAttempt(), 2000);
            } else {
                this.timeRemaining = diff;
            }
        };

        updateTimer();
        this.timerInterval = setInterval(updateTimer, 1000);
    }

    getTimeDisplay(): string {
        const hours = Math.floor(this.timeRemaining / 3600);
        const minutes = Math.floor((this.timeRemaining % 3600) / 60);
        const seconds = this.timeRemaining % 60;

        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }

    isTimerWarning(): boolean {
        return this.timeRemaining > 0 && this.timeRemaining <= 300;
    }

    onExerciseAnswerChange(exerciseId: number, answer: any): void {
        this.exerciseAnswers.set(exerciseId, answer);
        console.log(`Answer updated for exercise ${exerciseId}:`, answer);
    }

    getExerciseAnswer(exerciseId: number): any {
        return this.exerciseAnswers.get(exerciseId);
    }

    canSubmit(): boolean {
        if (!this.assignment) return false;

        return this.assignment.exercises.every(ex => {
            const answer = this.exerciseAnswers.get(ex.id!);
            if (answer === null || answer === undefined) return false;
            if (typeof answer === 'string' && answer.trim() === '') return false;
            if (Array.isArray(answer) && answer.length === 0) return false;
            return true;
        });
    }

    submitAttempt(): void {
        if (!this.attempt || !this.assignment || this.isSubmitting) return;

        this.isSubmitting = true;

        const answers: ExerciseAnswerRequest[] = this.assignment.exercises.map(exercise => {
            const answer = this.exerciseAnswers.get(exercise.id!);
            return {
                exerciseId: exercise.id!,
                answerJson: JSON.stringify(this.formatAnswerForBackend(exercise, answer))
            };
        });

        this.subscriptions.add(
            this.assignmentService.submitAttempt(
                this.groupId,
                this.assignmentId,
                this.attempt.id,
                { answers }
            ).subscribe({
                next: (result) => {
                    this.notificationService.showSuccess('Assignment submitted successfully!');

                    this.router.navigate(['/assignments', this.assignmentId, 'attempts', result.id], {
                        queryParams: { groupId: this.groupId }
                    });
                },
                error: (error) => {
                    this.error = error.error?.message || 'Failed to submit assignment';
                    this.notificationService.showError(this.error);
                    this.isSubmitting = false;
                    console.error('Error submitting attempt:', error);
                }
            })
        );
    }

    formatAnswerForBackend(exercise: ExerciseUnion, answer: any): any {
        switch (exercise.type) {
            case ExerciseType.MULTIPLE_CHOICE:
                return { selectedOptions: answer || [] };

            case ExerciseType.RADIO:
                return { selectedOptions: answer ? [answer] : [] };

            case ExerciseType.REGEX:
                return { pattern: answer || '' };

            case ExerciseType.XPATH:
                return { expression: answer || '' };

            default:
                return answer;
        }
    }

    goBack(): void {
        if (confirm('Are you sure? Your progress will be lost.')) {
            this.router.navigate(['/groups', this.groupId]);
        }
    }
}