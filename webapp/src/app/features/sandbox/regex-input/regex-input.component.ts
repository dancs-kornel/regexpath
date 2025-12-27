import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RegexBuilderComponent } from '../regex-builder/regex-builder.component';

@Component({
  selector: 'app-regex-input',
  standalone: true,
  imports: [CommonModule, FormsModule, RegexBuilderComponent],
  templateUrl: './regex-input.component.html',
  styleUrls: ['./regex-input.component.css']
})
export class RegexInputComponent implements OnDestroy {
  @Input() pattern: string = '';
  @Input() isValidPattern: boolean = true;
  @Input() flagI: boolean = false;
  @Input() flagM: boolean = false;

  @Output() patternChange = new EventEmitter<string>();
  @Output() flagsChange = new EventEmitter<{ i: boolean; m: boolean }>();


  @ViewChild('patternInputField') patternInputField!: ElementRef<HTMLInputElement>;

  private caretPositionTimeout?: number;

  handlePatternInput(newValue: string) {
    this.updatePatternAndEmit(newValue);
  }

  handleToggleFlag(flag: 'i' | 'm', checked: boolean) {
    const nextI = flag === 'i' ? checked : this.flagI;
    const nextM = flag === 'm' ? checked : this.flagM;
    this.flagsChange.emit({ i: nextI, m: nextM });
  }

  handleInsertToken(token: string) {
    const inputEl = this.patternInputField?.nativeElement;

    if (!inputEl) {
      const appended = this.pattern + token;
      this.updatePatternAndEmit(appended);
      return;
    }

    const isFocused = document.activeElement === inputEl;

    if (!isFocused) {
      const appended = this.pattern + token;
      this.updatePatternAndEmit(appended);
      return;
    }

    const start = inputEl.selectionStart ?? this.pattern.length;
    const end = inputEl.selectionEnd ?? this.pattern.length;
    const selected = this.pattern.slice(start, end);
    const hasSelection = start !== end;

    if (hasSelection && ['()', '[]', '{}'].includes(token)) {
      const [open, close] = this.getBracketPair(token);
      this.replaceSelection(start, end, `${open}${selected}${close}`);
      return;
    }

    this.replaceSelection(start, end, token);
  }


  private getBracketPair(token: string): [string, string] {
    const pairs: Record<string, [string, string]> = {
      '()': ['(', ')'],
      '[]': ['[', ']'],
      '{}': ['{', '}']
    };
    return pairs[token];
  }

  private replaceSelection(start: number, end: number, replacement: string): void {
    const before = this.pattern.slice(0, start);
    const after = this.pattern.slice(end);
    const newValue = before + replacement + after;

    this.updatePatternAndEmit(newValue, start + replacement.length);
  }

  private updatePatternAndEmit(newValue: string, newCaretPos?: number): void {
    this.pattern = newValue;
    this.patternChange.emit(newValue);

    if (newCaretPos !== undefined && this.patternInputField?.nativeElement) {
      const el = this.patternInputField.nativeElement;
      // using setTimeout here lets Angular update ngModel binding first
      this.caretPositionTimeout = window.setTimeout(() => {
        el.focus();
        el.setSelectionRange(newCaretPos, newCaretPos);
      });
    }
  }

  ngOnDestroy(): void {
    if (this.caretPositionTimeout) {
      clearTimeout(this.caretPositionTimeout);
    }
  }
}
