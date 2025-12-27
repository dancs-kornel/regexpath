
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { switchMap, catchError, map } from 'rxjs/operators';
import { AssignmentService } from '../../../../core/services/assignment.service';
import { ExerciseBuilderComponent } from '../exercise-builder/exercise-builder.component';
import {
    Assignment,
    AssignmentStatus,
    ExerciseUnion,
    UpdateAssignmentRequest,
    CreateAssignmentRequest,
    AddExerciseRequest
} from '../../../../core/models/assignment.models';
import { NotificationService } from '../../../../shared/services/notification.service';
import { toAddExerciseRequest } from './assignment-exercise.mapper';
import { AssignmentBaseComponent } from '../shared/assignment-base.component';
import { fromBackendResponse } from './assignment-exercise.mapper';

@Component({
    selector: 'app-assignment-edit',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, ExerciseBuilderComponent],
    templateUrl: './assignment-edit.component.html',
    styleUrl: './assignment-edit.component.css'
})
export class AssignmentEditComponent extends AssignmentBaseComponent implements OnInit {
    assignment: Assignment | null = null;
    isLoading = true;
    successMessage = '';
    exerciseToEdit: ExerciseUnion | null = null;
    private originalExercises: ExerciseUnion[] = [];

    constructor(
        fb: FormBuilder,
        assignmentService: AssignmentService,
        private route: ActivatedRoute,
        router: Router,
        notificationService: NotificationService
    ) {
        super(fb, assignmentService, router, notificationService);
    }

    ngOnInit(): void {
        const id = this.route.snapshot.params['id'];
        if (id) {
            this.loadAssignment(parseInt(id));
        } else {
            this.router.navigate(['/teacher/assignments']);
        }
    }

    loadAssignment(id: number): void {
        this.assignmentService.getAssignment(id).subscribe({
            next: (assignment) => {
                const parsedExercises = assignment.exercises.map(ex => fromBackendResponse(ex));

                this.assignment = {
                    ...assignment,
                    exercises: parsedExercises
                };

                this.populateForm(assignment);
                this.exercises = [...parsedExercises];
                this.originalExercises = JSON.parse(JSON.stringify(parsedExercises));
                this.isLoading = false;
            },
            error: (error) => {
                this.error = 'Failed to load assignment';
                this.isLoading = false;
                console.error('Error loading assignment:', error);
            }
        });
    }

    populateForm(assignment: Assignment): void {
        this.assignmentForm.patchValue({
            title: assignment.title,
            description: assignment.description || '',
            dueDate: assignment.dueDate ? new Date(assignment.dueDate).toISOString().slice(0, 16) : '',
            timeLimit: assignment.timeLimitMinutes || null,
            attemptLimit: assignment.maxAttempts
        });
    }

    override addExercise(): void {
        this.editingExerciseIndex = null;
        this.exerciseToEdit = null;
        this.showExerciseBuilder = true;
    }

    override editExercise(index: number): void {
        this.editingExerciseIndex = index;
        this.exerciseToEdit = { ...this.exercises[index] } as ExerciseUnion;
        this.showExerciseBuilder = true;
    }

    onExerciseSaved(exercise: ExerciseUnion): void {
        if (this.editingExerciseIndex !== null) {
            exercise.id = this.exercises[this.editingExerciseIndex].id ?? exercise.id;
            exercise.orderIndex = this.editingExerciseIndex;
            this.exercises[this.editingExerciseIndex] = exercise;
        } else {
            exercise.orderIndex = this.exercises.length;
            this.exercises.push(exercise);
        }
        this.exerciseToEdit = null;
        this.showExerciseBuilder = false;
        this.editingExerciseIndex = null;
    }

    override onExerciseBuilderCancel(): void {
        this.exerciseToEdit = null;
        super.onExerciseBuilderCancel();
    }

    saveChanges(): void {
        if (!this.assignment) return;

        if (this.assignment.status === AssignmentStatus.SUBMITTED) {
            this.notificationService.showError('This assignment is published and cannot be edited. Revert to draft first.');
            return;
        }

        if (this.assignmentForm.invalid) {
            this.markFormGroupTouched(this.assignmentForm);
            this.error = 'Please fix the form errors before submitting.';
            return;
        }

        this.isSubmitting = true;

        this.persist$().subscribe({
            next: (final) => {
                this.assignment = final;
                this.exercises = [...final.exercises];
                this.originalExercises = JSON.parse(JSON.stringify(final.exercises));
                this.notificationService.showSuccess('Changes saved.');
                this.isSubmitting = false;
            },
            error: () => { this.isSubmitting = false; }
        });
    }

    publish(): void {
        if (!this.assignment) return;
        if (this.assignment.status === AssignmentStatus.SUBMITTED) {
            this.notificationService.showSuccess('Assignment is already published.');
            return;
        }

        this.isSubmitting = true;

        this.persist$()
            .pipe(
                switchMap(() => this.assignmentService.publishAssignment(this.assignment!.id!)),
                switchMap(() => this.assignmentService.getAssignment(this.assignment!.id!))
            )
            .subscribe({
                next: (final) => {
                    this.assignment = final;
                    this.exercises = [...final.exercises];
                    this.originalExercises = JSON.parse(JSON.stringify(final.exercises));
                    this.notificationService.showSuccess('Assignment published successfully!');
                    this.isSubmitting = false;
                },
                error: (err) => {
                    this.notificationService.showError(err?.error?.message || 'Failed to publish assignment');
                    console.error(err);
                    this.isSubmitting = false;
                }
            });
    }

    revertToDraft(): void {
        if (!this.assignment) return;

        this.isSubmitting = true;

        this.assignmentService.revertToDraft(this.assignment.id!)
            .pipe(
                switchMap(() => this.assignmentService.getAssignment(this.assignment!.id!))
            )
            .subscribe({
                next: (final) => {
                    this.assignment = final;
                    this.exercises = [...final.exercises];
                    this.originalExercises = JSON.parse(JSON.stringify(final.exercises));
                    this.notificationService.showSuccess('Assignment reverted to draft!');
                    this.isSubmitting = false;
                },
                error: (err) => {
                    this.notificationService.showError(err?.error?.message || 'Failed to revert assignment');
                    console.error(err);
                    this.isSubmitting = false;
                }
            });
    }

    private persist$() {
        if (!this.assignment) return of(this.assignment!);

        this.updateOrderIndices();
        this.error = '';
        this.successMessage = '';

        const v = this.assignmentForm.value;

        // Step 1: Update assignment metadata (without exercises)
        const assignmentRequest = {
            id: this.assignment.id!,
            title: v.title,
            description: v.description || undefined,
            status: this.assignment.status,
            dueDate: v.dueDate || undefined,
            timeLimitMinutes: v.timeLimit !== null && v.timeLimit !== undefined
                ? Number(v.timeLimit)
                : undefined,
            maxAttempts: v.attemptLimit !== null && v.attemptLimit !== undefined
                ? Number(v.attemptLimit)
                : undefined,
        };

        return this.assignmentService.updateAssignment(this.assignment.id!, assignmentRequest).pipe(
            switchMap(() => {
                // Step 2: Handle exercise changes
                const exerciseRequests: any[] = [];

                this.exercises.forEach((exercise) => {
                    const mapped = toAddExerciseRequest(exercise);

                    if (exercise.id) {
                        exerciseRequests.push(
                            this.assignmentService.updateExercise(
                                this.assignment!.id!,
                                exercise.id,
                                mapped
                            )
                        );
                    } else {
                        exerciseRequests.push(
                            this.assignmentService.addExercise(
                                this.assignment!.id!,
                                mapped
                            )
                        );
                    }
                });

                if (exerciseRequests.length === 0) {
                    return of([]);
                }

                return forkJoin(exerciseRequests);
            }),
            switchMap(() => {
                // Step 3: Reload full assignment to get updated state
                return this.assignmentService.getAssignment(this.assignment!.id!);
            }),
            catchError(error => {
                this.isSubmitting = false;
                const msg = error?.error?.message || 'Failed to update assignment';
                this.notificationService.showError(msg);
                console.error('Error updating assignment:', error);
                throw error;
            })
        );
    }

    duplicateAssignment(): void {
        if (!this.assignment) return;

        const duplicateRequest: CreateAssignmentRequest = {
            title: `${this.assignment.title} (Copy)`,
            description: this.assignment.description,
            status: AssignmentStatus.DRAFT,
            dueDate: undefined,
            timeLimitMinutes: this.assignment.timeLimitMinutes,
            maxAttempts: this.assignment.maxAttempts,
            exercises: this.exercises.map(ex => toAddExerciseRequest({ ...ex, id: undefined }))
        };

        this.assignmentService.createAssignment(duplicateRequest).subscribe({
            next: (newAssignment) => {
                this.notificationService.showSuccess('Assignment duplicated successfully!');
                this.router.navigate(['/teacher/assignments', newAssignment.id, 'edit']);
            },
            error: (error) => {
                this.notificationService.showError('Failed to duplicate assignment');
                console.error('Error duplicating assignment:', error);
            }
        });
    }

    deleteAssignment(): void {
        if (!this.assignment || !confirm(`Are you sure you want to delete "${this.assignment.title}"? This action cannot be undone.`)) {
            return;
        }

        this.assignmentService.deleteAssignment(this.assignment.id!).subscribe({
            next: () => {
                this.notificationService.showSuccess('Assignment deleted successfully');
                this.router.navigate(['/teacher/assignments']);
            },
            error: (error) => {
                this.notificationService.showError('Failed to delete assignment');
                console.error('Error deleting assignment:', error);
            }
        });
    }

    navigateBack(): void {
        this.router.navigate(['/teacher/assignments']);
    }
}