
import { Directive, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AssignmentService } from '../../../../core/services/assignment.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import {
    ExerciseType,
    ExerciseUnion,
    AssignmentStatus
} from '../../../../core/models/assignment.models';

@Directive()
export abstract class AssignmentBaseComponent implements OnInit {
    assignmentForm: FormGroup;
    isSubmitting = false;
    error = '';
    
    exercises: ExerciseUnion[] = [];
    showExerciseBuilder = false;
    editingExerciseIndex: number | null = null;
    
    readonly minDate = new Date().toISOString().slice(0, 16);
    readonly attemptLimitOptions = [
        { value: null, label: 'Unlimited' },
        { value: 1, label: '1 attempt' },
        { value: 2, label: '2 attempts' },
        { value: 3, label: '3 attempts' },
        { value: 5, label: '5 attempts' },
        { value: 10, label: '10 attempts' }
    ];

    constructor(
        protected fb: FormBuilder,
        protected assignmentService: AssignmentService,
        protected router: Router,
        protected notificationService: NotificationService
    ) {
        this.assignmentForm = this.createAssignmentForm();
    }

    abstract ngOnInit(): void;

    createAssignmentForm(): FormGroup {
        return this.fb.group({
            title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
            description: ['', [Validators.maxLength(500)]],
            dueDate: [''],
            timeLimit: [null, [Validators.min(1), Validators.max(300)]],
            attemptLimit: [null]
        });
    }

    addExercise(): void {
        this.editingExerciseIndex = null;
        this.showExerciseBuilder = true;
    }

    editExercise(index: number): void {
        this.editingExerciseIndex = index;
        this.showExerciseBuilder = true;
    }

    removeExercise(index: number): void {
        this.exercises.splice(index, 1);
        this.updateOrderIndices();
    }

    moveExerciseUp(index: number): void {
        if (index > 0) {
            this.swapExercises(index, index - 1);
            this.updateOrderIndices();
        }
    }

    moveExerciseDown(index: number): void {
        if (index < this.exercises.length - 1) {
            this.swapExercises(index, index + 1);
            this.updateOrderIndices();
        }
    }

    onExerciseBuilderCancel(): void {
        this.showExerciseBuilder = false;
        this.editingExerciseIndex = null;
    }

    protected swapExercises(i: number, j: number): void {
        const temp = this.exercises[i];
        this.exercises[i] = this.exercises[j];
        this.exercises[j] = temp;
    }

    protected updateOrderIndices(): void {
        this.exercises.forEach((ex, i) => ex.orderIndex = i);
    }

    protected markFormGroupTouched(formGroup: FormGroup): void {
        Object.keys(formGroup.controls).forEach(key => {
            const control = formGroup.get(key);
            control?.markAsTouched();
            if (control instanceof FormGroup) {
                this.markFormGroupTouched(control);
            }
        });
    }

    getTotalPoints(): number {
        return this.exercises.reduce((sum, ex) => sum + ex.points, 0);
    }

    getExerciseTypeLabel(type: ExerciseType): string {
        switch (type) {
            case ExerciseType.MULTIPLE_CHOICE: return 'Multiple Choice';
            case ExerciseType.RADIO: return 'Single Choice';
            case ExerciseType.REGEX: return 'RegEx';
            case ExerciseType.XPATH: return 'XPath';
            default: return type;
        }
    }

    getExerciseSummary(exercise: ExerciseUnion): string {
        const maxLength = 50;
        return exercise.question.length > maxLength 
            ? exercise.question.substring(0, maxLength) + '...' 
            : exercise.question;
    }

    getStatusBadgeClass(status?: AssignmentStatus): string {
        return status === AssignmentStatus.SUBMITTED ? 'published' : 'draft';
    }
}