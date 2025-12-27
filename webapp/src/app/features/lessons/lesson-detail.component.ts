import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { LessonService } from '../../shared/services/lesson.service';
import { LessonProgressService } from '../../core/services/lesson-progress.service';
import { AuthService } from '../../core/services/auth.service';
import { ExerciseComponent } from './exercises/exercise.component';
import { PrerequisiteWarningModalComponent } from '../../shared/components/prerequisite-warning-modal/prerequisite-warning-modal.component';
import { PrerequisiteCheckResponse } from '../../core/models/lesson-progress.models';
import { DifficultyPromptModalComponent } from '../../shared/components/difficulty-prompt-modal/difficulty-prompt-modal.component';
import { DifficultyService } from '../../core/services/difficulty.service';
import { DifficultyPromptResponse, DifficultyLevel } from '../../core/models/difficulty.models';

@Component({
    selector: 'app-lesson-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, ExerciseComponent, PrerequisiteWarningModalComponent, DifficultyPromptModalComponent],
    templateUrl: './lesson-detail.component.html',
    styleUrl: './lesson-detail.component.css'
})
export class LessonDetailComponent implements OnInit, OnDestroy {
    lesson: any = null;
    loading = true;
    error = '';
    currentExerciseIndex = 0;
    isAuthenticated = false;
    isCompleted = false;
    markingComplete = false;

    showPrerequisiteModal = false;
    prerequisiteCheckResult: PrerequisiteCheckResponse | null = null;

    showDifficultyModal = false;
    difficultyPromptData: DifficultyPromptResponse | null = null;

    currentDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM;
    loadingDifficulty = false;

    DifficultyLevel = DifficultyLevel;

    private subscriptions = new Subscription();

    constructor(
        private lessonService: LessonService,
        private lessonProgressService: LessonProgressService,
        private authService: AuthService,
        private difficultyService: DifficultyService,
        private route: ActivatedRoute,
        private router: Router
    ) {}

    ngOnInit() {
        this.isAuthenticated = this.authService.isAuthenticated();
        
        if (this.isAuthenticated) {
            this.fetchCurrentDifficulty();
        }
        
        const lessonId = this.route.snapshot.paramMap.get('id');
        if (lessonId) {
            this.checkPrerequisitesAndLoad(lessonId);
        }
    }

    fetchCurrentDifficulty() {
        this.subscriptions.add(
            this.difficultyService.getCurrentDifficulty().subscribe({
                next: (response) => {
                    this.currentDifficulty = response.difficultyLevel;
                    console.log('Current difficulty loaded:', this.currentDifficulty);
                },
                error: (err) => {
                    console.error('Error fetching difficulty:', err);
                }
            })
        );
    }

    onDifficultyChange() {
        if (!this.isAuthenticated) return;

        this.loadingDifficulty = true;
        this.subscriptions.add(
            this.difficultyService.updateDifficulty(this.currentDifficulty).subscribe({
                next: () => {
                    console.log('Difficulty manually updated to:', this.currentDifficulty);
                    this.loadingDifficulty = false;
                },
                error: (err) => {
                    console.error('Error updating difficulty:', err);
                    this.loadingDifficulty = false;
                }
            })
        );
    }

    checkPrerequisitesAndLoad(lessonId: string) {
        this.subscriptions.add(
            this.lessonProgressService.checkPrerequisites(lessonId).subscribe({
                next: (result) => {
                    if (result.unmetPrerequisites && result.unmetPrerequisites.length > 0) {
                        this.prerequisiteCheckResult = result;
                        this.showPrerequisiteModal = true;
                        this.loadLesson(lessonId);
                    } else {
                        this.loadLesson(lessonId);
                    }
                },
                error: (err) => {
                    console.error('Error checking prerequisites:', err);
                    this.loadLesson(lessonId);
                }
            })
        );
    }

    loadLesson(lessonId: string) {
        this.subscriptions.add(
            this.lessonService.getLessonById(lessonId).subscribe({
                next: (lesson) => {
                    this.lesson = lesson;
                    this.loading = false;
                    if (this.isAuthenticated) {
                        this.checkCompletion(lessonId);
                        this.recordAccess(lessonId);
                    }
                },
                error: (err) => {
                    this.error = 'Failed to load lesson';
                    this.loading = false;
                    console.error('Error loading lesson:', err);
                }
            })
        );
    }

    recordAccess(lessonId: string) {
        this.subscriptions.add(
            this.lessonProgressService.recordLessonAccess(lessonId).subscribe({
                error: (err) => console.error('Error recording access:', err)
            })
        );
    }

    checkCompletion(lessonId: string) {
        this.subscriptions.add(
            this.lessonProgressService.getUserProgress().subscribe({
                next: (progressList) => {
                    this.isCompleted = this.lessonProgressService.isLessonCompleted(lessonId, progressList);
                },
                error: (err) => console.error('Error checking completion:', err)
            })
        );
    }


    onContinueAnyway() {
        this.showPrerequisiteModal = false;
        this.prerequisiteCheckResult = null;
    }


    onCloseModal() {
        this.showPrerequisiteModal = false;
        this.prerequisiteCheckResult = null;
        this.backToLessons();
    }

    markAsComplete() {
        if (!this.lesson || this.markingComplete) return;

        this.markingComplete = true;
        this.subscriptions.add(
            this.lessonProgressService.markLessonComplete(this.lesson.id).subscribe({
                next: () => {
                    this.isCompleted = true;
                    this.markingComplete = false;
                    alert('Lecke befejezve! 🎉');
                },
                error: (err) => {
                    console.error('Error marking complete:', err);
                    alert('Hiba történt a lecke befejezésekor');
                    this.markingComplete = false;
                }
            })
        );
    }

    getCurrentExercise() {
        return this.lesson?.exercises?.[this.currentExerciseIndex] || null;
    }

    nextExercise() {
        if (this.currentExerciseIndex < this.lesson.exercises.length - 1) {
            this.currentExerciseIndex++;
        }
    }

    previousExercise() {
        if (this.currentExerciseIndex > 0) {
            this.currentExerciseIndex--;
        }
    }

    isLastExercise(): boolean {
        return this.currentExerciseIndex === this.lesson?.exercises?.length - 1;
    }

    backToLessons() {
        this.router.navigate(['/lessons']);
    }

    onDifficultyPrompt(promptData: DifficultyPromptResponse) {
        console.log('Difficulty prompt received:', promptData);
        if (promptData.shouldPrompt) {
            this.difficultyPromptData = promptData;
            this.showDifficultyModal = true;
            this.subscriptions.add(
                this.difficultyService.recordPromptShown().subscribe({
                    error: (err) => console.error('Error recording prompt shown: ', err)
                })
            );
        }
    }

    onAcceptDifficultyChange() {
        if (!this.difficultyPromptData?.suggestedLevel) return;

        this.subscriptions.add(
            this.difficultyService.updateDifficulty(this.difficultyPromptData.suggestedLevel).subscribe({
                next: () => {
                    console.log('Difficulty updated to:', this.difficultyPromptData?.suggestedLevel);
                    this.currentDifficulty = this.difficultyPromptData!.suggestedLevel!;
                    this.showDifficultyModal = false;
                    this.difficultyPromptData = null;
                },
                error: (err) => {
                    console.error('Error updating difficulty:', err);
                    this.showDifficultyModal = false;
                }
            })
        );
    }

    onDismissDifficultyPrompt() {
        this.showDifficultyModal = false;
        this.difficultyPromptData = null;
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }
}