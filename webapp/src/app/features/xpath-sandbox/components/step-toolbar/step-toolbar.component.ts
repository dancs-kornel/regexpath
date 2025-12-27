import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-step-toolbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './step-toolbar.component.html',
  styleUrls: ['./step-toolbar.component.css'],
})
export class StepToolbarComponent {
  @Input() enabled!: boolean;
  @Input() state: any; // PlaybackState | null
  @Input() isFirstStep!: boolean;
  @Input() isLastStep!: boolean;
  @Input() currentStepIndex!: number;
  @Input() totalSteps!: number;
  @Input() isPlaying!: boolean;
  @Input() playbackSpeed!: number;
  @Input() speedMultiplier!: string;
  @Input() currentStepDescription!: string;
  @Input() currentStepMatches!: string;

  @Output() previous = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();
  @Output() playPause = new EventEmitter<void>();
  @Output() reset = new EventEmitter<void>();
  @Output() onSpeedChangeEvent = new EventEmitter<number>();

  onSpeedChange(event: Event) {
    const v = Number((event.target as HTMLInputElement).value);
    this.onSpeedChangeEvent.emit(v);
  }
}
