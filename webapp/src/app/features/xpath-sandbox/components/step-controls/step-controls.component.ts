
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlaybackState } from '../../services/step-evaluation/models/step-evaluation.models';

@Component({
  selector: 'app-step-controls',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './step-controls.component.html',
  styleUrls: ['./step-controls.component.css']
})
export class StepControlsComponent {
  @Input() enabled = false;
  @Input() state: PlaybackState | null = null;
  @Input() currentStepDescription = '';
  @Input() currentStepMatches = '';
  @Input() isFirstStep = true;
  @Input() isLastStep = false;
  @Input() speedMultiplier = '1x';

  @Output() toggleMode = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();
  @Output() previous = new EventEmitter<void>();
  @Output() playPause = new EventEmitter<void>();
  @Output() reset = new EventEmitter<void>();
  @Output() speedChange = new EventEmitter<number>();

  get currentStepIndex(): number {
    return this.state?.currentStepIndex ?? 0;
  }

  get totalSteps(): number {
    return this.state?.steps.length ?? 0;
  }

  get isPlaying(): boolean {
    return this.state?.isPlaying ?? false;
  }

  get playbackSpeed(): number {
    return this.state?.speed ?? 1000;
  }

  onSpeedChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const speed = parseInt(target.value, 10);
    this.speedChange.emit(speed);
  }
}