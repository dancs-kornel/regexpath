import { Component, Input, Output, EventEmitter } from "@angular/core";
import { CommonModule } from "@angular/common";

@Component({
  selector: 'app-step-toggle',
  standalone: true,             
  imports: [CommonModule],      
  templateUrl: './step-toggle.component.html',
  styleUrls: ['./step-toggle.component.css']
})
export class StepToggleComponent {
  @Input() enabled!: boolean;
  @Output() toggleMode = new EventEmitter<void>();
}
