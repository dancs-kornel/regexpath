import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { RegexMatchService } from './regex-match.service';
import { MatchInfo } from '../../shared/utils/regex-utils';

@Pipe({
    name: 'highlight',
    standalone: true,
    pure: true
})

export class HighlightPipe implements PipeTransform {
    constructor(
        private sanitizer: DomSanitizer,
        private regexMatchService: RegexMatchService
    ) { }

    transform(text: string, pattern: string, hoverIndex?: number | null, flags: string = 'g'): SafeHtml {
        if (!this.shouldHighlight(text, pattern)) {
            return this.sanitizer.bypassSecurityTrustHtml(this.escapeHtml(text));
        }

        const matches = this.regexMatchService.getMatches(text, pattern, flags);
        if (!matches.length) {
            return this.sanitizer.bypassSecurityTrustHtml(this.escapeHtml(text));
        }

        const highlightedHtml = this.buildHighlightedHtml(text, matches, hoverIndex);
        return this.sanitizer.bypassSecurityTrustHtml(highlightedHtml);
    }

    private shouldHighlight(text: string, pattern: string): boolean {
        if (!text) return false;
        if (!pattern) return false;
        return true;
    }

    private buildHighlightedHtml(text: string, matches: MatchInfo[], hoverIndex?: number | null): string {
        const parts: string[] = [];
        let lastIndex = 0;
        matches.forEach((match, index) => {
            parts.push(this.escapeHtml(text.slice(lastIndex, match.start)));
            parts.push(this.formatMatch(match, index, hoverIndex));
            lastIndex = match.end;
        });

        parts.push(this.escapeHtml(text.slice(lastIndex)));
        return parts.join('');
    }

    private formatMatch(match: MatchInfo, index: number, hoverIndex?: number | null): string {
        const escapedText = this.escapeHtml(match.text);
        const isHovered = hoverIndex !== null && hoverIndex === index;
        return isHovered
            ? `<mark style="background-color: lightblue; font-weight:bold;">${escapedText}</mark>`
            : `<mark>${escapedText}</mark>`;
    }


    private escapeHtml(unsafe: string): string {
        return unsafe
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
}