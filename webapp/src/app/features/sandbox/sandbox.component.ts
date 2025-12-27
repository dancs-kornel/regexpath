
import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HighlightPipe } from './highlight.pipe';
import { MatchInfo } from '../../shared/utils/regex-utils';
import { RegexMatchService } from './regex-match.service';

import { RegexExplanationComponent } from './regex-explanation/regex-explanation.component';
import { RegexInputComponent } from './regex-input/regex-input.component';
import { RegexExamplesComponent } from './regex-examples/regex-examples.component';

import { SandboxContent, ContentSource } from '../../core/models/sandbox-content.models';
import { ExampleManagerComponent } from '../../shared/components/example-manager/example-manager.component';

import { AuthService } from '../../core/services/auth.service';
import { ContentType } from '../../core/models/sandbox-content.models';

@Component({
  selector: 'app-sandbox',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HighlightPipe,
    RegexExplanationComponent,
    RegexInputComponent,
    RegexExamplesComponent,
    ExampleManagerComponent
  ],
  templateUrl: './sandbox.component.html',
  styleUrls: ['./sandbox.component.css', '../../shared/styles/sandbox-common.css']
})
export class SandboxComponent {
  @ViewChild('patternInput') patternInput!: ElementRef<HTMLInputElement>;
  @ViewChild('textareaInput') textareaInput!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('highlightLayer') highlightLayer!: ElementRef<HTMLDivElement>;

  text: string = 'Ez egy példa 1234, benne különböző karakterekkel: @#$%!';
  originalText: string = this.text;
  hasUnsavedChanges: boolean = false;

  currentContent: SandboxContent | null = null;
  selectedContentId: number | null = null;

  pattern: string = '';
  isValidPattern: boolean = true;
  flagI: boolean = false;
  flagM: boolean = false;

  matches: MatchInfo[] = [];
  hoverIndex: number | null = null;

  readonly MAX_TEXT_LENGTH = 1000;
  ContentType = ContentType;

  constructor(
    private regexMatchService: RegexMatchService,
    private authService: AuthService
  ) {}

  onContentLoaded(evt: {
    text: string;
    content: SandboxContent | null;
    selectedId: number | null;
  }): void {
    this.text = evt.text;
    this.originalText = evt.text;
    this.currentContent = evt.content;
    this.selectedContentId = evt.selectedId;
    this.hasUnsavedChanges = false;
    this.updateMatches();
  }

  onContentCleared(): void {
  }

  onContentPersisted(evt: {
    content: SandboxContent | null;
    selectedId: number | null;
    text: string;
  }): void {
    this.currentContent = evt.content;
    this.selectedContentId = evt.selectedId ?? null;
    this.originalText = evt.text;
    this.hasUnsavedChanges = false;
  }

  onPatternChange(newPattern: string) {
    this.pattern = newPattern;
    this.updateMatches();
  }

  onFlagsChange(flags: { i: boolean; m: boolean }) {
    this.flagI = flags.i;
    this.flagM = flags.m;
    this.updateMatches();
  }

  onTextChange(newText: string): void {
    if (newText.length > this.MAX_TEXT_LENGTH) {
      this.text = newText.slice(0, this.MAX_TEXT_LENGTH);
    } else {
      this.text = newText;
    }

    this.hasUnsavedChanges = this.text !== this.originalText;
    this.updateMatches();
  }

  onTextareaScroll(): void {
    if (!this.textareaInput || !this.highlightLayer) return;
    const textarea = this.textareaInput.nativeElement;
    const highlight = this.highlightLayer.nativeElement;
    highlight.scrollTop = textarea.scrollTop;
    highlight.scrollLeft = textarea.scrollLeft;
  }

  private updateMatches(): void {
    if (!this.pattern) {
      this.isValidPattern = true;
      this.matches = [];
      return;
    }

    let flags = '';
    if (this.flagI) flags += 'i';
    if (this.flagM) flags += 'm';

    this.isValidPattern = this.regexMatchService.isValidPattern(this.pattern);
    this.matches = this.regexMatchService.getMatches(this.text, this.pattern, flags);
  }

  get regexFlags(): string {
    let f = '';
    if (this.flagI) f += 'i';
    if (this.flagM) f += 'm';
    return f;
  }

  get characterCount(): string {
    return `${this.text.length} / ${this.MAX_TEXT_LENGTH}`;
  }

  get isAtCharacterLimit(): boolean {
    return this.text.length >= this.MAX_TEXT_LENGTH;
  }

  get currentContentLabel(): string {
    if (!this.currentContent) return 'Default';
    const name = this.currentContent.name || 'Untitled';
    const source =
      this.currentContent.source === ContentSource.BUILTIN
        ? 'Built-in'
        : 'My Content';
    return `${name} [${source}]`;
  }

  get isUserAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }
}
