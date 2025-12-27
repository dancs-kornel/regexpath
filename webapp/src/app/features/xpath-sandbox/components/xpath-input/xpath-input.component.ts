import { Component, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface TokenButton {
  label: string;
  value: string;
}

@Component({
  selector: 'app-xpath-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './xpath-input.component.html',
  styleUrls: ['./xpath-input.component.css']
})
export class XpathInputComponent {
  @ViewChild('xpathInput', { static: false }) xpathInputElement?: ElementRef<HTMLInputElement>;

  @Input() expression: string = '';
  @Input() loading: boolean = false;
  @Input() tokens: TokenButton[] = [];
  
  @Output() expressionChange = new EventEmitter<string>();
  @Output() evaluate = new EventEmitter<void>();

  onInputChange(value: string): void {
    this.expression = value;
    this.expressionChange.emit(value);
    this.evaluate.emit();
  }

  insertToken(token: string): void {
    const input = this.xpathInputElement?.nativeElement;
    if (!input) return;

    const start = input.selectionStart || 0;
    const end = input.selectionEnd || 0;
    const before = this.expression.substring(0, start);
    const after = this.expression.substring(end);

    if (token === '[]') {
      this.expression = before + token + after;
      this.expressionChange.emit(this.expression);
      
      setTimeout(() => {
        input.focus();
        input.setSelectionRange(start + 1, start + 1);
      }, 0);
    } else {
      this.expression = before + token + after;
      this.expressionChange.emit(this.expression);
      
      setTimeout(() => {
        input.focus();
        input.setSelectionRange(start + token.length, start + token.length);
      }, 0);
    }

    this.evaluate.emit();
  }
}