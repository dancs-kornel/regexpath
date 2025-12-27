import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExplainedNode } from '../../../shared/utils/regex-explainer';
import { RegexExplanationService } from '../../../shared/services/regex-explanation.service';

@Component({
  selector: 'app-regex-explanation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './regex-explanation.component.html',
  styleUrls: ['./regex-explanation.component.css']
})
export class RegexExplanationComponent implements OnChanges {
  @Input() pattern: string = '';
  @Input() indentStep = 12;
  
  nodes: ExplainedNode[] = [];

  constructor(private explanationService: RegexExplanationService) {}

  ngOnChanges(): void {
    this.updateExplanation();
  }

  private updateExplanation(): void {

    if (this.pattern) {
      this.nodes = this.explanationService.explainPatternSync(this.pattern);
    } else {
      this.nodes = [];
    }
  }
}