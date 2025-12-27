import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DifficultyLevel } from '../../../core/models/difficulty.models';

@Component({
  selector: 'app-difficulty-prompt-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './difficulty-prompt-modal.component.html',
  styleUrl: './difficulty-prompt-modal.component.css'
})
export class DifficultyPromptModalComponent {
  @Input() promptType: 'INCREASE' | 'DECREASE' = 'INCREASE';
  @Input() currentLevel: DifficultyLevel = DifficultyLevel.MEDIUM;
  @Input() suggestedLevel: DifficultyLevel = DifficultyLevel.HARD;
  @Input() message: string = '';
  
  @Output() accept = new EventEmitter<void>();
  @Output() dismiss = new EventEmitter<void>();

  onAccept() {
    this.accept.emit();
  }

  onDismiss() {
    this.dismiss.emit();
  }
}