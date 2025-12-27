
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AssignmentService } from '../../../../core/services/assignment.service';
import { ExerciseBuilderComponent } from '../exercise-builder/exercise-builder.component';
import { 
    AssignmentStatus, 
    ExerciseUnion,
    CreateAssignmentRequest 
} from '../../../../core/models/assignment.models';
import { NotificationService } from '../../../../shared/services/notification.service';
import { AssignmentBaseComponent } from '../shared/assignment-base.component';
import { toAddExerciseRequest } from '../assignment-edit/assignment-exercise.mapper';

@Component({
    selector: 'app-assignment-create',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, ExerciseBuilderComponent],
    templateUrl: './assignment-create.component.html',
    styleUrl: './assignment-create.component.css'
})
export class AssignmentCreateComponent extends AssignmentBaseComponent implements OnInit {

    constructor(
        fb: FormBuilder,
        assignmentService: AssignmentService,
        router: Router,
        notificationService: NotificationService
    ) {
        super(fb, assignmentService, router, notificationService);
    }

    ngOnInit(): void {
        // Start with no exercises 
    }

    onExerciseSaved(exercise: ExerciseUnion): void {
        if (this.editingExerciseIndex !== null) {
            this.exercises[this.editingExerciseIndex] = exercise;
        } else {
            exercise.orderIndex = this.exercises.length;
            this.exercises.push(exercise);
        }
        
        this.showExerciseBuilder = false;
        this.editingExerciseIndex = null;
    }

    saveAsDraft(): void {
        this.submitAssignment(AssignmentStatus.DRAFT);
    }

    publish(): void {
        this.submitAssignment(AssignmentStatus.SUBMITTED);
    }

    private submitAssignment(status: AssignmentStatus): void {
        if (this.assignmentForm.invalid) {
            this.markFormGroupTouched(this.assignmentForm);
            this.error = 'Please fix the form errors before submitting.';
            return;
        }

        if (this.exercises.length === 0) {
            this.error = 'Please add at least one exercise to the assignment.';
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const formValue = this.assignmentForm.value;
        const request: CreateAssignmentRequest = {
            title: formValue.title,
            description: formValue.description || undefined,
            status: status,
            dueDate: formValue.dueDate || undefined,
            timeLimitMinutes: formValue.timeLimitMinutes || undefined,
            maxAttempts: formValue.maxAttempts,
            exercises: this.exercises.map(ex => toAddExerciseRequest(ex))
        };

        this.assignmentService.createAssignment(request).subscribe({
            next: (assignment) => {
                const message = status === AssignmentStatus.SUBMITTED 
                    ? 'Assignment published successfully!' 
                    : 'Assignment saved as draft!';
                this.notificationService.showSuccess(message);
                this.router.navigate(['/teacher/assignments']);
            },
            error: (error) => {
                this.isSubmitting = false;
                const errorMessage = error.error?.message || 'Failed to create assignment';
                this.notificationService.showError(errorMessage);
                console.error('Error creating assignment:', error);
            }
        });
    }
}