import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseExerciseComponent } from '../base-exercise.component';

@Component({
  selector: 'app-radio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './radio.component.html',
  styleUrls: ['./radio.component.css','../../../../shared/styles/exercise-common.css']
})
export class RadioComponent extends BaseExerciseComponent {
  override previewMode = false;

  protected override disableForPreview(): void {
    // Radio buttons will be disabled via template binding
  }

  onRadioChange(optionId: string) {
    if (this.previewMode) {
      return;
    }

    if (this.submitted && !this.assignmentMode) return;
    
    this.selectedOptions = [optionId];
    
    if (this.assignmentMode) {
      this.emitAnswer();
    }
  }

  protected override emitAnswer(): void {
    if (this.previewMode) {
      return;
    }
    this.answerChange.emit(this.selectedOptions[0] || null);
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