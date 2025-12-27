import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ViewToggle {
  id: string;
  label: string;
  visible: boolean;
  available: boolean;
}

@Component({
  selector: 'app-view-toggle-controls',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './view-toggle-controls.component.html',
  styleUrls: ['./view-toggle-controls.component.css']
})
export class ViewToggleControlsComponent {
  @Input() toggles: ViewToggle[] = [];
  @Output() toggleChanged = new EventEmitter<{ id: string; visible: boolean }>();

  onToggleChange(toggle: ViewToggle): void {
    this.toggleChanged.emit({
      id: toggle.id,
      visible: toggle.visible
    });
  }
}