import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EvaluationResult, XPathResult } from '../../models/xpath-types';

@Component({
  selector: 'app-results-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './results-list.component.html',
  styleUrls: ['./results-list.component.css']
})
export class ResultsListComponent {
  @Input() result: EvaluationResult | null = null;
  
  @Output() resultHover = new EventEmitter<XPathResult | null>();
  @Output() resultClick = new EventEmitter<XPathResult>();

  onResultHover(result: XPathResult): void {
    this.resultHover.emit(result);
  }

  onResultLeave(): void {
    this.resultHover.emit(null);
  }

  onResultClick(result: XPathResult): void {
    this.resultClick.emit(result);
  }

  getNodeTypeLabel(type: string): string {
    switch (type) {
      case 'element': return 'Element';
      case 'attribute': return 'Attribute';
      case 'text': return 'Text';
      case 'comment': return 'Comment';
      default: return type;
    }
  }
}