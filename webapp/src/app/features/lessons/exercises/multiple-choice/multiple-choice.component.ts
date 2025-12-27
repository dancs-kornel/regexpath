import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseExerciseComponent } from '../base-exercise.component';

@Component({
    selector: 'app-multiple-choice',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './multiple-choice.component.html',
    styleUrls: ['./multiple-choice.component.css', '../../../../shared/styles/exercise-common.css']
})
export class MultipleChoiceComponent extends BaseExerciseComponent {
    override previewMode = false;

    protected override disableForPreview(): void {
        // Checkboxes will be disabled via template binding
    }

    onCheckboxChange(optionId: string, event: Event) {
        if (this.previewMode) {
            return;
        }

        if (this.submitted && !this.assignmentMode) return;

        const checkbox = event.target as HTMLInputElement;

        if (checkbox?.checked) {
            if (!this.selectedOptions.includes(optionId)) {
                this.selectedOptions = [...this.selectedOptions, optionId];
            }
        } else {
            this.selectedOptions = this.selectedOptions.filter(id => id !== optionId);
        }

        if (this.assignmentMode) {
            this.emitAnswer();
        }
    }

    protected override emitAnswer(): void {
        if (this.previewMode) {
            return;
        }
        this.answerChange.emit(this.selectedOptions);
    }

    isUserCorrectSelection(optionId: string): boolean {
        if (!this.submitted || !this.result || !this.result.correctAnswers) {
            return false;
        }
        return this.result.correctAnswers.includes(optionId) &&
               this.selectedOptions.includes(optionId);
    }

    isUnselectedCorrectAnswer(optionId: string): boolean {
        if (!this.solution || !this.result || !this.result.correctAnswers) {
            return false;
        }
        return this.result.correctAnswers.includes(optionId) &&
               !this.selectedOptions.includes(optionId);
    }

    isIncorrectSelection(optionId: string): boolean {
        if (!this.submitted || !this.result || !this.result.correctAnswers) {
            return false;
        }
        return !this.result.correctAnswers.includes(optionId) &&
               this.selectedOptions.includes(optionId);
    }

}