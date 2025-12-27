import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseExerciseComponent } from '../base-exercise.component';
import { HighlightPipe } from '../../../sandbox/highlight.pipe';
import { RegexMatchService } from '../../../sandbox/regex-match.service';

interface TestCase {
  text: string;
  description: string;
  expectedMatches?: string[];
}

interface TestCaseWithType extends TestCase {
  isPositive: boolean;
}

@Component({
  selector: 'app-regex-sandbox',
  standalone: true,
  imports: [CommonModule, FormsModule, HighlightPipe],
  templateUrl: './regex-sandbox.component.html',
  styleUrls: ['./regex-sandbox.component.css', '../../../../shared/styles/exercise-common.css']
})
export class RegexSandboxComponent extends BaseExerciseComponent {
  pattern: string = '';
  override previewMode = false;

  constructor(private regexMatchService: RegexMatchService) {
    super();
  }

  get shouldHighlight(): boolean {
    if (this.assignmentMode) {
      return (this.exercise as any)?.enableRealTimeHighlighting ?? true;
    }

    if (this.difficulty === 'HARD') {
      return false;
    }

    return true;
  }


  protected override disableForPreview(): void {
    // In preview mode, the input will be disabled via template binding
  }

  get allTestCases(): TestCaseWithType[] {
    const testCases: TestCaseWithType[] = [];

    // Handle both naming conventions:
    // - Assignment mode (via adapter): this.exercise.testCases
    // - Lesson mode (direct): this.exercise.test_cases
    const testCasesData = (this.exercise as any)?.testCases || (this.exercise as any)?.test_cases;

    if (testCasesData?.positive) {
      testCasesData.positive.forEach((tc: TestCase) => {
        testCases.push({ ...tc, isPositive: true });
      });
    }

    if (testCasesData?.negative) {
      testCasesData.negative.forEach((tc: TestCase) => {
        testCases.push({ ...tc, isPositive: false });
      });
    }

    return testCases;
  }

  getLocalTestResult(testCase: TestCaseWithType): boolean | null {
    if (!this.pattern) return null;
    if (!this.regexMatchService.isValidPattern(this.pattern)) return null;

    const matches = this.regexMatchService.getMatches(testCase.text, this.pattern);
    const hasMatch = matches.length > 0;

    return testCase.isPositive ? hasMatch : !hasMatch;
  }

  getTestCaseIcon(testCase: TestCaseWithType): string {
    const result = this.getLocalTestResult(testCase);
    if (result === null) return '';
    return result ? '✓' : '✗';
  }

  getTestCaseClass(testCase: TestCaseWithType): string {
    const result = this.getLocalTestResult(testCase);
    if (result === null) return '';
    return result ? 'pass' : 'fail';
  }

  onPatternChange(): void {
    if (this.previewMode) {
      return;
    }

    if (this.assignmentMode) {
      this.emitAnswer();
    }
  }

  override submitAnswer() {
    if (this.previewMode) {
      return;
    }

    if (this.assignmentMode) {
      this.emitAnswer();
      return;
    }

    if (!this.exercise || !this.pattern || this.loading) return;

    this.loading = true;
    this.subscriptions.add(
      this.lessonService
        .validateExercise(this.lessonId, this.exercise.id, this.exercise.type, this.pattern)
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

  protected override emitAnswer(): void {
    if (this.previewMode) {
      return;
    }
    this.answerChange.emit(this.pattern);
  }

  protected override hydrateAnswer(answer: any): void {
    if (typeof answer === 'string') {
      this.pattern = answer;
    }
  }

  override reset() {
    super.reset();
    this.pattern = '';
  }

  get filteredTestCases(): TestCaseWithType[] {
    const allCases = this.allTestCases;
    if (this.difficulty === 'EASY') {
      return allCases.filter(tc => tc.isPositive);
    }
    return allCases;
  }
}