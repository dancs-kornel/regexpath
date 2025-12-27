import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PrerequisiteInfo } from '../../../core/models/lesson-progress.models';

@Component({
  selector: 'app-prerequisite-warning-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './prerequisite-warning-modal.component.html',
  styleUrl: './prerequisite-warning-modal.component.css'
})
export class PrerequisiteWarningModalComponent {
  @Input() prerequisites: PrerequisiteInfo[] = [];
  @Input() lessonTitle: string = '';
  @Output() continueAnyway = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();

  constructor(private router: Router) {}

  onContinueAnyway() {
    this.continueAnyway.emit();
  }

  onClose() {
    this.close.emit();
  }

  goToPrerequisite(lessonId: string) {
    this.close.emit();
    this.router.navigate(['/lessons', lessonId]);
  }
}