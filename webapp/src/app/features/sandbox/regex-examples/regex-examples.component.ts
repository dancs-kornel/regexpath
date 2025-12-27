import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { RegexExampleService, RegexExample } from '../regex-example.service';

@Component({
  selector: 'app-regex-examples',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './regex-examples.component.html',
  styleUrls: ['./regex-examples.component.css']
})
export class RegexExamplesComponent implements OnChanges, OnDestroy {
  @Input() pattern: string = '';
  @Input() isValidPattern: boolean = true;
  @Input() flagsI: boolean = false;
  @Input() flagsM: boolean = false;

  positiveExamples: RegexExample[] = [];
  negativeExamples: RegexExample[] = [];
  errorMessage: string | null = null;
  isGenerating: boolean = false;

  private subscriptions = new Subscription();

  constructor(private exampleService: RegexExampleService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['pattern'] ||
      changes['flagsI'] ||
      changes['flagsM'] ||
      changes['isValidPattern']
    ) {
      this.clearExamples();
    }
  }

  get isDisabled(): boolean {
    return !this.pattern || !this.isValidPattern || this.isGenerating;
  }

  get hasAnyExamples(): boolean {
    return (
      (this.positiveExamples?.length ?? 0) > 0 ||
      (this.negativeExamples?.length ?? 0) > 0
    );
  }

  generateExamples(): void {
    if (this.isDisabled) return;

    this.isGenerating = true;
    this.errorMessage = null;

    this.subscriptions.add(
      this.exampleService
        .generateExamples(this.pattern, this.flagsI, this.flagsM)
        .subscribe({
          next: (response) => {
            if (response.errorMessage) {
              this.errorMessage = response.errorMessage;
              this.positiveExamples = [];
              this.negativeExamples = [];
            } else {
              this.positiveExamples = response.positiveExamples;
              this.negativeExamples = response.negativeExamples;
              this.errorMessage = null;
            }
            this.isGenerating = false;
          },
          error: (err) => {
            console.error('Error generating examples:', err);
            this.errorMessage =
              'Failed to generate examples. Please try again.';
            this.isGenerating = false;
          }
        })
    );
  }

  private clearExamples(): void {
    this.positiveExamples = [];
    this.negativeExamples = [];
    this.errorMessage = null;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
