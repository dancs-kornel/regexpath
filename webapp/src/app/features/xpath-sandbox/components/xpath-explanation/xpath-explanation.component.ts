
import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { XPathExplanationNode } from '../../models/xpath-explanations.models';
import { XPathExplanationService } from '../../services/xpath-explanation.service';

@Component({
  selector: 'app-xpath-explanation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './xpath-explanation.component.html',
  styleUrls: ['./xpath-explanation.component.css']
})
export class XpathExplanationComponent implements OnChanges, OnDestroy {
  @Input() xpathExpression: string = '';

  explanationNodes: XPathExplanationNode[] = [];
  error: string | null = null;
  isEmpty: boolean = true;

  private destroy$ = new Subject<void>();

  constructor(private explanationService: XPathExplanationService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['xpathExpression']) {
      this.updateExplanation();
    }
  }

  private updateExplanation(): void {
    const expression = this.xpathExpression?.trim();

    if (!expression) {
      this.isEmpty = true;
      this.explanationNodes = [];
      this.error = null;
      return;
    }

    this.isEmpty = false;

    this.explanationService.explainExpression(expression)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (nodes) => {
          this.explanationNodes = nodes;

          if (nodes.length === 1 && nodes[0].explanation === 'Hibás XPath kifejezés') {
            this.error = 'Hibás XPath kifejezés';
          } else {
            this.error = null;
          }
        },
        error: (err) => {
          console.error('Error getting explanation:', err);
          this.error = 'Hiba a magyarázat generálása során';
          this.explanationNodes = [];
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  getNodeClass(node: XPathExplanationNode): string {
    const classes = ['explanation-item'];
    
    if (node.category) {
      classes.push(`category-${node.category}`);
    }
    
    return classes.join(' ');
  }

  getIndentStyle(node: XPathExplanationNode): { [key: string]: string } {
    const depth = node.depth ?? 0;
    return {
      'padding-left': `${depth * 20}px`
    };
  }
}