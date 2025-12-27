import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CodeHighlighterService {
  private readonly HIGHLIGHTING_CONTENT_ID = 'highlighting-content';
  private readonly CODE_LINE_CLASS = 'code-line';
  private readonly HIGHLIGHTED_CLASS = 'highlighted';
  private readonly HIGHLIGHTED_HOVER_CLASS = 'highlighted-hover';

  applyLineHighlights(
    highlightedLineNumbers: Set<number>,
    hoveredLineNumber: number | null
  ): void {
    const lines = this.getCodeLines();
    if (!lines) return;

    lines.forEach(line => {
      line.classList.remove(this.HIGHLIGHTED_CLASS, this.HIGHLIGHTED_HOVER_CLASS);

      const lineNumber = Number(line.getAttribute('data-line'));

      const isHighlighted = highlightedLineNumbers.has(lineNumber);
      const isHovered = hoveredLineNumber === lineNumber;

      if (isHighlighted && isHovered) {
        line.classList.add(this.HIGHLIGHTED_HOVER_CLASS);
      } else if (isHighlighted) {
        line.classList.add(this.HIGHLIGHTED_CLASS);
      }
    });
  }

  clearLineHighlights(): void {
    const lines = this.getCodeLines();
    if (!lines) return;

    lines.forEach(line => {
      line.classList.remove(this.HIGHLIGHTED_CLASS, this.HIGHLIGHTED_HOVER_CLASS);
    });
  }

  private getCodeLines(): NodeListOf<HTMLDivElement> | null {
    const codeEl = document.getElementById(this.HIGHLIGHTING_CONTENT_ID);
    if (!codeEl) return null;

    return codeEl.querySelectorAll<HTMLDivElement>(`.${this.CODE_LINE_CLASS}`);
  }
}