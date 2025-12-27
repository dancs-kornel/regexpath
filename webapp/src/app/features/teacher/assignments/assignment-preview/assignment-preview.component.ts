import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AssignmentService } from '../../../../core/services/assignment.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { Assignment, ExerciseUnion, StudentAssignmentDetail } from '../../../../core/models/assignment.models';

import { RadioComponent } from '../../../lessons/exercises/radio/radio.component';
import { MultipleChoiceComponent } from '../../../lessons/exercises/multiple-choice/multiple-choice.component';
import { RegexSandboxComponent } from '../../../lessons/exercises/regex-sandbox/regex-sandbox.component';
import { XPathSandboxExerciseComponent } from '../../../lessons/exercises/xpath-sandbox-exercise/xpath-sandbox-exercise.component';
import { ExerciseType } from '../../../../core/models/assignment.models';

@Component({
    selector: 'app-assignment-preview',
    standalone: true,
    imports: [
        CommonModule,
        RadioComponent,
        MultipleChoiceComponent,
        RegexSandboxComponent,
        XPathSandboxExerciseComponent
    ],
    templateUrl: './assignment-preview.component.html',
    styleUrl: './assignment-preview.component.css'
})
export class AssignmentPreviewComponent implements OnInit {
    assignment: StudentAssignmentDetail | null = null;
    assignmentId: number = 0;

    isLoading = true;
    error = '';

    exerciseAnswers: Map<number, any> = new Map();

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private assignmentService: AssignmentService,
        private notificationService: NotificationService,
        private location: Location
    ) { }

    ngOnInit(): void {
        this.assignmentId = Number(this.route.snapshot.paramMap.get('id'));
        this.loadAssignmentPreview();
    }

    loadAssignmentPreview(): void {
        this.isLoading = true;
        this.error = '';

        this.assignmentService.previewAssignment(this.assignmentId).subscribe({
            next: (assignment) => {
                console.log('Assignment preview loaded:', assignment);
                this.assignment = assignment;
                this.prefillCorrectAnswers(assignment);
                this.isLoading = false;
            },
            error: (error) => {
                this.error = error.error?.message || 'Failed to load assignment preview';
                this.isLoading = false;
                this.notificationService.showError(this.error);
                console.error('Error loading assignment preview:', error);
            }
        });
    }

    private prefillCorrectAnswers(assignment: StudentAssignmentDetail): void {
        assignment.exercises.forEach(exercise => {
            if (!exercise.id) return;

            switch (exercise.type) {
                case ExerciseType.MULTIPLE_CHOICE:
                    const correctMultiple = exercise.options
                        ?.filter(o => o.correct)
                        .map(o => o.id) || [];
                    this.exerciseAnswers.set(exercise.id, correctMultiple);
                    break;

                case ExerciseType.RADIO:
                    const correctRadio = exercise.options?.find(o => o.correct)?.id;
                    this.exerciseAnswers.set(exercise.id, correctRadio || null);
                    break;

                case ExerciseType.REGEX:
                    this.exerciseAnswers.set(exercise.id, exercise.solution || '');
                    break;

                case ExerciseType.XPATH:
                    this.exerciseAnswers.set(exercise.id, exercise.solution || '');
                    break;
            }
        });
    }

    goBack(): void {
        this.location.back();
    }

    onExerciseAnswerChange(exerciseId: number, answer: any): void {
        // Preview mode - do nothing
    }

    getExerciseAnswer(exerciseId: number): any {
        return this.exerciseAnswers.get(exerciseId);
    }
}