import {
  Component, Input, Output, EventEmitter, ViewChild, ElementRef,
  AfterViewInit, OnChanges, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

declare const Prism: any; 

@Component({
  selector: 'app-source-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './source-editor.component.html',
  styleUrls: ['./source-editor.component.css']
})
export class SourceEditorComponent implements AfterViewInit, OnChanges {
  @ViewChild('textarea') textareaElement!: ElementRef<HTMLTextAreaElement>;

  @Input() sourceText: string = '';
  @Input() readOnly: boolean = false;
  @Input() showCharacterCount: boolean = true;
  @Input() maxCharacters: number = 10000;
  @Input() placeholder: string = 'Enter your code here...';

  @Output() sourceChange = new EventEmitter<string>(); 
  @Output() textInput = new EventEmitter<string>();  

  characterCount = 0;
  isOverLimit = false;

  ngAfterViewInit(): void {
    this.updateCharacterCount();
    this.updateHighlight(true);
    setTimeout(() => this.syncScroll(), 0);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sourceText'] && !changes['sourceText'].firstChange) {
      this.updateCharacterCount();
      this.updateHighlight(true);
    }
  }

  onTextChange(): void {
    this.updateCharacterCount();
    this.sourceChange.emit(this.sourceText);
  }

  onTextInput(): void {
    this.updateCharacterCount();
    this.updateHighlight(false);
    this.textInput.emit(this.sourceText);
  }

  onScroll(): void {
    this.syncScroll();
  }

  onKeyDown(evt: KeyboardEvent): void {
    if (evt.key === 'Tab' && !this.readOnly) {
      evt.preventDefault();
      const ta = this.textareaElement.nativeElement;
      const start = ta.selectionStart ?? 0;
      const end = ta.selectionEnd ?? 0;
      const val = this.sourceText;
      this.sourceText = val.slice(0, start) + '\t' + val.slice(end);
      requestAnimationFrame(() => {
        ta.selectionStart = ta.selectionEnd = start + 1;
        this.onTextInput();
        this.onTextChange(); 
      });
    }
  }


  private updateHighlight(applyFade: boolean): void {
    const codeEl = document.getElementById('highlighting-content') as HTMLElement | null;
    if (!codeEl) return;

    let text = this.sourceText ?? '';
    if (text.endsWith('\n')) text += ' '; 
    codeEl.innerHTML = this.escapeHtml(text);

    try {
      Prism.highlightElement(codeEl);
    } catch { /* no-op if Prism not loaded yet */ }

    this.wrapPrismLines(codeEl);

    if (applyFade) {
      codeEl.classList.remove('fade-in');
      (codeEl as HTMLElement).offsetHeight;
      codeEl.classList.add('fade-in');
    }
    this.syncScroll();
  }


  private wrapPrismLines(codeEl: HTMLElement) {
    const frag = document.createDocumentFragment();
    let lineNum = 1;
    let lineContainer = this.createLineContainer(lineNum);

    const pushContainer = () => {
      frag.appendChild(lineContainer);
      lineNum++;
      lineContainer = this.createLineContainer(lineNum);
    };

    const processNode = (node: Node, currentTarget: Node) => {
      if (node.nodeType === Node.TEXT_NODE) {
        const text = node.textContent ?? '';
        if (text.indexOf('\n') === -1) {
          currentTarget.appendChild(document.createTextNode(text));
          return;
        }
        const parts = text.split('\n');
        parts.forEach((part, idx) => {
          currentTarget.appendChild(document.createTextNode(part));
          if (idx < parts.length - 1) {
            pushContainer();
            currentTarget = lineContainer;
          }
        });
        return;
      }

      if (node.nodeType === Node.ELEMENT_NODE) {
        const clone = node.cloneNode(false) as HTMLElement;
        currentTarget.appendChild(clone);
        node.childNodes.forEach(child => processNode(child, clone));
        return;
      }

      currentTarget.appendChild(node.cloneNode(true));
    };

    const originalChildren = Array.from(codeEl.childNodes);
    originalChildren.forEach(child => processNode(child, lineContainer));

    frag.appendChild(lineContainer);

    codeEl.innerHTML = '';
    codeEl.appendChild(frag);
  }

  private createLineContainer(lineNumber: number): HTMLDivElement {
    const div = document.createElement('div');
    div.className = 'code-line';
    div.setAttribute('data-line', String(lineNumber));
    if (!div.firstChild) div.appendChild(document.createTextNode(''));
    return div;
  }

  private syncScroll(): void {
    const ta = this.textareaElement?.nativeElement;
    const pre = document.getElementById('highlighting');
    if (!ta || !pre) return;
    pre.scrollTop = ta.scrollTop;
    pre.scrollLeft = ta.scrollLeft;
  }

  private escapeHtml(text: string): string {
    return text
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  private updateCharacterCount(): void {
    this.characterCount = this.sourceText?.length ?? 0;
    this.isOverLimit = this.characterCount > this.maxCharacters;
  }

  getCharacterCountClass(): string {
    if (this.isOverLimit) return 'over-limit';
    if (this.characterCount > this.maxCharacters * 0.9) return 'near-limit';
    return '';
  }
}