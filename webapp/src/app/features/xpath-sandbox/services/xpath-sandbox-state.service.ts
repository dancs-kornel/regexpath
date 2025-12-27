import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { SafeHtml } from '@angular/platform-browser';
import { Sample, EvaluationResult, TreeNode } from '../models/xpath-types';

export interface XPathSandboxState {
  samples: Sample[];
  selectedSample: Sample | null;
  
  xpathExpression: string;
  
  evaluationResult: EvaluationResult | null;
  
  loading: boolean;
  
  sourceLines: string[];
  highlightedCode: SafeHtml;
  highlightedLineNumbers: Set<number>;
  hoveredLineNumber: number | null;
  
  treeNodes: TreeNode[];
  
  htmlContent: string;
}

@Injectable()
export class XpathSandboxStateService {
  private static getDefaultState(): XPathSandboxState {
    return {
      samples: [],
      selectedSample: null,
      xpathExpression: '',
      evaluationResult: null,
      loading: false,
      sourceLines: [],
      highlightedCode: '',
      highlightedLineNumbers: new Set<number>(),
      hoveredLineNumber: null,
      treeNodes: [],
      htmlContent: ''
    };
  }

  private state: XPathSandboxState = XpathSandboxStateService.getDefaultState();

  private state$ = new BehaviorSubject<XPathSandboxState>(this.state);
  private selectedSample$ = new BehaviorSubject<Sample | null>(null);
  private xpathExpression$ = new BehaviorSubject<string>('');
  private evaluationResult$ = new BehaviorSubject<EvaluationResult | null>(null);
  private loading$ = new BehaviorSubject<boolean>(false);
  private highlightedCode$ = new BehaviorSubject<SafeHtml>('');

  getState(): XPathSandboxState {
    return { ...this.state };
  }

  getState$(): Observable<XPathSandboxState> {
    return this.state$.asObservable();
  }

  
  setSamples(samples: Sample[]): void {
    this.state.samples = samples;
    this.updateState();
  }

  getSamples(): Sample[] {
    return this.state.samples;
  }

  setSelectedSample(sample: Sample | null): void {
    this.state.selectedSample = sample;
    this.selectedSample$.next(sample);
    this.updateState();
  }

  getSelectedSample(): Sample | null {
    return this.state.selectedSample;
  }

  getSelectedSample$(): Observable<Sample | null> {
    return this.selectedSample$.asObservable();
  }

  
  setXpathExpression(expression: string): void {
    this.state.xpathExpression = expression;
    this.xpathExpression$.next(expression);
    this.updateState();
  }

  getXpathExpression(): string {
    return this.state.xpathExpression;
  }

  getXpathExpression$(): Observable<string> {
    return this.xpathExpression$.asObservable();
  }

  
  setEvaluationResult(result: EvaluationResult | null): void {
    this.state.evaluationResult = result;
    this.evaluationResult$.next(result);
    this.updateState();
  }

  getEvaluationResult(): EvaluationResult | null {
    return this.state.evaluationResult;
  }

  getEvaluationResult$(): Observable<EvaluationResult | null> {
    return this.evaluationResult$.asObservable();
  }

  
  setLoading(loading: boolean): void {
    this.state.loading = loading;
    this.loading$.next(loading);
    this.updateState();
  }

  getLoading(): boolean {
    return this.state.loading;
  }

  getLoading$(): Observable<boolean> {
    return this.loading$.asObservable();
  }

  
  setSourceLines(lines: string[]): void {
    this.state.sourceLines = lines;
    this.updateState();
  }

  getSourceLines(): string[] {
    return this.state.sourceLines;
  }

  setHighlightedCode(code: SafeHtml): void {
    this.state.highlightedCode = code;
    this.highlightedCode$.next(code);
    this.updateState();
  }

  getHighlightedCode(): SafeHtml {
    return this.state.highlightedCode;
  }

  getHighlightedCode$(): Observable<SafeHtml> {
    return this.highlightedCode$.asObservable();
  }

  setHighlightedLineNumbers(lines: Set<number>): void {
    this.state.highlightedLineNumbers = lines;
    this.updateState();
  }

  getHighlightedLineNumbers(): Set<number> {
    return this.state.highlightedLineNumbers;
  }

  setHoveredLineNumber(lineNumber: number | null): void {
    this.state.hoveredLineNumber = lineNumber;
    this.updateState();
  }

  getHoveredLineNumber(): number | null {
    return this.state.hoveredLineNumber;
  }

  
  setTreeNodes(nodes: TreeNode[]): void {
    this.state.treeNodes = nodes;
    this.updateState();
  }

  getTreeNodes(): TreeNode[] {
    return this.state.treeNodes;
  }

  
  setHtmlContent(content: string): void {
    this.state.htmlContent = content;
    this.updateState();
  }

  getHtmlContent(): string {
    return this.state.htmlContent;
  }


  clearEvaluationState(): void {
    this.state.evaluationResult = null;
    this.state.highlightedLineNumbers = new Set<number>();
    this.state.hoveredLineNumber = null;
    this.evaluationResult$.next(null);
    this.updateState();
  }

  updateSourceState(sourceLines: string[], highlightedCode: SafeHtml, highlightedLines: Set<number>): void {
    this.state.sourceLines = sourceLines;
    this.state.highlightedCode = highlightedCode;
    this.state.highlightedLineNumbers = highlightedLines;
    this.highlightedCode$.next(highlightedCode);
    this.updateState();
  }


  reset(): void {
    this.state = XpathSandboxStateService.getDefaultState();
    this.updateState();
    this.selectedSample$.next(null);
    this.xpathExpression$.next('');
    this.evaluationResult$.next(null);
    this.loading$.next(false);
    this.highlightedCode$.next('');
  }

  private updateState(): void {
    this.state$.next({ ...this.state });
  }
}