import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseExerciseComponent } from '../base-exercise.component';
import { EvaluatorService } from '../../../xpath-sandbox/services/evaluator.service';
import { LineMapperService } from '../../../xpath-sandbox/services/line-mapper.service';

declare const Prism: any;

@Component({
  selector: 'app-xpath-sandbox-exercise',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './xpath-sandbox-exercise.component.html',
  styleUrls: ['./xpath-sandbox-exercise.component.css', '../../../../shared/styles/exercise-common.css']
})
export class XPathSandboxExerciseComponent extends BaseExerciseComponent implements OnInit, AfterViewInit {
  @ViewChild('codeDisplay') codeDisplayRef!: ElementRef<HTMLElement>;

  xpathExpression: string = '';
  syntaxError: string = '';
  override previewMode = false;

  private expectedNodes: Node[] = [];
  private expectedLineNumbers = new Set<number>();

  private actualNodes: Node[] = [];
  private actualLineNumbers = new Set<number>();

  private parsedDocument: Document | null = null;

  private sourceLines: string[] = [];

  constructor(
    private evaluatorService: EvaluatorService,
    private lineMapperService: LineMapperService
  ) {
    super();
  }

  get shouldHighlight(): boolean {
    if (this.assignmentMode) {
      return (this.exercise as any)?.enableRealTimeHighlighting ?? true;
    }

    if (this.difficulty === 'HARD') {
      return false;
    }

    return true;
  }

  override ngOnInit(): void {
    super.ngOnInit();
    console.log('XPath Exercise Component initialized');
    if (this.exercise) {
      this.initializeExercise();
    }
  }

  ngAfterViewInit(): void {
    if (this.codeDisplayRef) {
      this.renderHighlightedCode();
    }
  }

  override ngOnChanges(changes: any): void {
    super.ngOnChanges(changes);
    if (changes['exercise'] && this.exercise) {
      this.initializeExercise();
    }
  }


  protected override disableForPreview(): void {
  }

  private initializeExercise(): void {
    const sampleDocument = (this.exercise as any)?.sampleDocument || (this.exercise as any)?.sample_document;

    if (!sampleDocument) {
      console.error('No sample document found in exercise');
      console.error('Exercise:', this.exercise);
      return;
    }

    this.syntaxError = '';

    this.sourceLines = sampleDocument.split('\n');
    this.parsedDocument = this.evaluatorService.parseSource(
      sampleDocument,
      'html' 
    );

    if (!this.parsedDocument) {
      console.error('Failed to parse sample document');
      this.syntaxError = 'Failed to parse sample document. Please contact support.';
      return;
    }

    const solution = this.exercise?.solution;
    if (solution) {
      this.evaluateSolution(solution);
    }

    this.renderHighlightedCode();
  }

  private evaluateSolution(solution: string): void {
    const result = this.evaluatorService.evaluate(solution);

    if (result.error) {
      console.error('Error evaluating solution:', result.error);
      console.error('Solution XPath:', solution);
      return;
    }

    this.expectedNodes = result.matches.map(match => match.node);

    this.expectedLineNumbers.clear();
    this.expectedNodes.forEach(node => {
      const lines = this.lineMapperService.getNodeLineNumbers(node, this.sourceLines);
      lines.forEach(line => this.expectedLineNumbers.add(line));
    });
  }

  onXPathChange(): void {
    if (this.previewMode) {
      return;
    }

    this.syntaxError = '';

    if (!this.xpathExpression.trim()) {
      this.actualNodes = [];
      this.actualLineNumbers.clear();
      this.updateHighlighting();

      if (this.assignmentMode) {
        this.emitAnswer();
      }
      return;
    }

    const result = this.evaluatorService.evaluate(this.xpathExpression);

    if (result.error) {
      this.syntaxError = this.formatSyntaxError(result.error);
      this.actualNodes = [];
      this.actualLineNumbers.clear();
    } else {
      this.actualNodes = result.matches.map(match => match.node);

      this.actualLineNumbers.clear();
      this.actualNodes.forEach(node => {
        const lines = this.lineMapperService.getNodeLineNumbers(node, this.sourceLines);
        lines.forEach(line => this.actualLineNumbers.add(line));
      });
    }

    this.updateHighlighting();

    if (this.assignmentMode) {
      this.emitAnswer();
    }
  }

  private formatSyntaxError(error: string): string {
    if (error.includes('did not match the expected pattern')) {
      return 'Invalid XPath syntax. Check your expression for missing operators or brackets.';
    }
    if (error.includes('Invalid expression')) {
      return 'Invalid XPath expression. Please check your syntax.';
    }
    return 'XPath syntax error. Please check your expression.';
  }

  /**
   * Update the highlighted document display using DOM manipulation
   * Pre-submission highlighting:
   * - Light blue: expected nodes
   * - Yellow: user's actual matches
   * - Green: overlap (correct matches)
   */
  private updateHighlighting(): void {
    if (!this.shouldHighlight) {
      return;
    }

    const blueLines = new Set<number>();
    const yellowLines = new Set<number>();
    const greenLines = new Set<number>();

    this.expectedLineNumbers.forEach(line => {
      if (this.actualLineNumbers.has(line)) {
        greenLines.add(line);
      } else {
        blueLines.add(line);
      }
    });

    this.actualLineNumbers.forEach(line => {
      if (!this.expectedLineNumbers.has(line)) {
        yellowLines.add(line);
      }
    });

    this.applyLineColors(blueLines, yellowLines, greenLines);
  }


  private renderHighlightedCode(): void {
    if (!this.codeDisplayRef) {
      return;
    }

    const codeEl = this.codeDisplayRef.nativeElement;

    const fragment = document.createDocumentFragment();

    this.sourceLines.forEach((line, index) => {
      const lineNumber = index + 1;
      const lineDiv = document.createElement('div');
      lineDiv.className = 'code-line';
      lineDiv.setAttribute('data-line', String(lineNumber));

      const highlighted = this.applySyntaxHighlighting(line);
      lineDiv.innerHTML = highlighted || ' ';

      fragment.appendChild(lineDiv);
    });

    codeEl.innerHTML = '';
    codeEl.appendChild(fragment);

    this.updateHighlighting();
  }

  private applyLineColors(
    blueLines: Set<number>,
    yellowLines: Set<number>,
    greenLines: Set<number>
  ): void {
    if (!this.codeDisplayRef) {
      return;
    }

    const codeEl = this.codeDisplayRef.nativeElement;
    const lines = codeEl.querySelectorAll<HTMLDivElement>('.code-line');

    lines.forEach(line => {
      line.classList.remove('highlight-blue', 'highlight-yellow', 'highlight-green');

      const lineNumber = Number(line.getAttribute('data-line'));

      if (greenLines.has(lineNumber)) {
        line.classList.add('highlight-green');
      } else if (blueLines.has(lineNumber)) {
        line.classList.add('highlight-blue');
      } else if (yellowLines.has(lineNumber)) {
        line.classList.add('highlight-yellow');
      }
    });
  }


  private applySyntaxHighlighting(line: string): string {
    if (typeof Prism === 'undefined') {
      return this.escapeHtml(line);
    }

    try {
      return Prism.highlight(line, Prism.languages.markup, 'markup');
    } catch (e) {
      return this.escapeHtml(line);
    }
  }

  private escapeHtml(text: string): string {
    const map: Record<string, string> = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    };
    return text.replace(/[&<>"']/g, (c) => map[c]);
  }

  override submitAnswer(): void {
    if (this.previewMode) {
      return;
    }

    if (this.assignmentMode) {
      this.emitAnswer();
      return;
    }

    if (!this.exercise || !this.xpathExpression.trim() || this.loading) {
      return;
    }

    if (this.syntaxError) {
      return;
    }

    this.loading = true;
    this.subscriptions.add(
      this.lessonService
        .validateExercise(this.lessonId, this.exercise.id, 'xpath_sandbox', this.xpathExpression)
        .subscribe({
          next: (result) => {
            this.result = result;
            this.submitted = true;
            this.loading = false;

            if (result?.correct) {
              this.autoAdvanceTimeout = window.setTimeout(() => {
                this.exerciseComplete.emit();
              }, 1500);
            }
          },
          error: (err) => {
            console.error('Validation error:', err);
            this.syntaxError = 'Failed to validate exercise. Please try again.';
            this.loading = false;
          }
        })
    );
  }

  protected override emitAnswer(): void {
    if (this.previewMode) {
      return;
    }
    this.answerChange.emit(this.xpathExpression);
  }

  protected override hydrateAnswer(answer: any): void {
    if (typeof answer === 'string') {
      this.xpathExpression = answer;
      this.onXPathChange();
    }
  }

  override reset(): void {
    super.reset();
    this.xpathExpression = '';
    this.actualNodes = [];
    this.actualLineNumbers.clear();
    this.syntaxError = '';
    this.updateHighlighting();
    console.log('Exercise reset');
  }
}