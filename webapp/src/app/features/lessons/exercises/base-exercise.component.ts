import { Directive, EventEmitter, Input, OnChanges, OnInit, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { LessonService } from '../../../shared/services/lesson.service';
import { DifficultyService } from '../../../core/services/difficulty.service';
import { AuthService } from '../../../core/services/auth.service';
import { inject } from '@angular/core';

@Directive()
export abstract class BaseExerciseComponent implements OnChanges, OnInit, OnDestroy {
  @Input() exercise: any = null;
  @Input() lessonId: string = '';
  @Input() assignmentMode = false;
  @Input() initialAnswer: any = null;
  @Input() previewMode = false;
  @Input() difficulty: string = 'MEDIUM';
  @Output() exerciseComplete = new EventEmitter<void>();
  @Output() answerChange = new EventEmitter<any>();
  @Output() difficultyPrompt = new EventEmitter<any>();

  protected lessonService = inject(LessonService);
  protected difficultyService = inject(DifficultyService);
  protected authService = inject(AuthService);

  selectedOptions: string[] = [];
  submitted = false;
  result: any = null;
  loading = false;

  protected currentAttemptNumber = 1;

  solution: string | null = null;
  solutionLoading = false;
  solutionError: string | null = null;

  protected subscriptions = new Subscription();
  protected autoAdvanceTimeout?: number;

  ngOnInit(): void {
    if (this.previewMode) {
      this.disableForPreview();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['exercise']) {
      const curr = changes['exercise'].currentValue;
      const prev = changes['exercise'].previousValue;
      if (curr && curr !== prev) {
        this.reset();
        this.currentAttemptNumber = 1;
      }
    }
    
    if (changes['initialAnswer'] && this.assignmentMode) {
      const answer = changes['initialAnswer'].currentValue;
      if (answer !== null && answer !== undefined) {
        this.hydrateAnswer(answer);
      }
    }
  }

  protected disableForPreview(): void {

  }
  

  protected hydrateAnswer(answer: any): void {
    if (Array.isArray(answer)) {
      this.selectedOptions = answer;
    } else {
      this.selectedOptions = answer ? [answer] : [];
    }
  }

  isSelected(optionId: string): boolean {
    return this.selectedOptions.includes(optionId);
  }

  submitAnswer() {
    if (this.assignmentMode) {
      this.emitAnswer();
      return;
    }

    if (!this.exercise || this.selectedOptions.length === 0 || this.loading) return;

    this.loading = true;
    this.subscriptions.add(
      this.lessonService
        .validateExercise(this.lessonId, this.exercise.id, this.exercise.type, this.selectedOptions)
        .subscribe({
          next: (result) => {
            this.result = result;
            this.submitted = true;
            this.loading = false;

            if (!this.assignmentMode && this.authService.isAuthenticated()) {
              this.recordAttempt(result.correct);
            }

            if (result?.correct) {
              this.autoAdvanceTimeout = window.setTimeout(() => {
                this.exerciseComplete.emit();
              }, 1500);
            } else {
              this.currentAttemptNumber++;
            }
          },
          error: (err) => {
            console.error('Validation error: ', err);
            this.loading = false;
          }
        })
    );
  }

  protected recordAttempt(isCorrect: boolean): void {
    console.log('recordAttempt called:', {
      exercise: this.exercise?.id,
      lessonId: this.lessonId,
      isCorrect,
      attemptNumber: this.currentAttemptNumber
    });

    if (!this.exercise || !this.lessonId) {
      console.warn('Missing exercise or lessonId');
      return;
    }

    this.subscriptions.add(
      this.difficultyService.recordAttempt({
        lessonId: this.lessonId,
        exerciseId: this.exercise.id,
        isCorrect: isCorrect,
        attemptNumber: this.currentAttemptNumber
      }).subscribe({
        next: (response) => {
          console.log('Difficulty service response:', response);
          if (response.shouldPrompt) {
            console.log('Emitting difficultyPrompt event:', response);
            this.difficultyPrompt.emit(response);
          } else {
            console.log(' No prompt needed');
          }
        },
        error: (err) => {
          console.error('Error recording attempt:', err);
        }
      })
    );
  }

  protected emitAnswer(): void {
    this.answerChange.emit(this.selectedOptions);
  }

  canShowSolution(): boolean {
    return this.authService.isAuthenticated() &&
           !this.assignmentMode &&
           !this.previewMode &&
           this.currentAttemptNumber >= 3;
  }

  showSolution(): void {
    if (!this.exercise || !this.lessonId) return;
    if (this.solutionLoading) return;

    if (this.solution) {
      this.solution = null;
      return;
    }

    this.solutionLoading = true;
    this.solutionError = null;

    this.subscriptions.add(
      this.lessonService.getSolution(this.lessonId, this.exercise.id).subscribe({
        next: (response) => {
          this.solution = response.solution;
          this.solutionLoading = false;
          console.log('Solution loaded:', response);
        },
        error: (err) => {
          this.solutionLoading = false;
          if (err.status === 403) {
            this.solutionError = 'You need at least 3 attempts before viewing the solution.';
          } else if (err.status === 401) {
            this.solutionError = 'You must be logged in to view solutions.';
          } else {
            this.solutionError = 'Failed to load solution. Please try again.';
          }
          console.error('Error loading solution:', err);
        }
      })
    );
  }

  reset() {
    this.selectedOptions = [];
    this.submitted = false;
    this.result = null;
    this.solution = null;
    this.solutionError = null;
  }

  ngOnDestroy(): void {
    if (this.autoAdvanceTimeout) {
      clearTimeout(this.autoAdvanceTimeout);
    }
    this.subscriptions.unsubscribe();
  }
}