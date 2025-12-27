import { Injectable } from '@angular/core';
import { EvaluationResult, TreeNode } from '../models/xpath-types';
import { ExecutedStep } from './step-evaluation/models/step-evaluation.models';
import { CodeHighlighterService } from './code-highlighter.service';
import { LineMapperService } from './line-mapper.service';
import { TreeHighlighterService } from './tree-highlighter.service';
import { RenderedHighlighterService } from './rendered-highlighter.service';
import { IframeNodeMapperService } from './iframe-node-mapper.service';

@Injectable({
  providedIn: 'root'
})
export class HighlightCoordinatorService {
  constructor(
    private codeHighlighter: CodeHighlighterService,
    private lineMapper: LineMapperService,
    private treeHighlighter: TreeHighlighterService,
    private renderedHighlighter: RenderedHighlighterService,
    private iframeNodeMapper: IframeNodeMapperService
  ) {}

  updateHighlightsFromResults(
    evaluationResult: EvaluationResult | null,
    sourceLines: string[],
    treeNodes: TreeNode[],
    iframeDoc: Document | null
  ): { highlightedLines: Set<number> } {
    const highlightedLines = this.getHighlightedLinesFromResults(evaluationResult, sourceLines);
    const matchedElements = this.getMatchedElementsFromResults(evaluationResult);
    
    this.codeHighlighter.applyLineHighlights(highlightedLines, null);
    
    this.treeHighlighter.updateHighlights(treeNodes, matchedElements, null);
    
    this.updateRenderedHighlightsFromResults(evaluationResult, iframeDoc);
    
    return { highlightedLines };
  }


  updateHoverHighlights(
    hoveredElement: Element | null,
    evaluationResult: EvaluationResult | null,
    sourceLines: string[],
    treeNodes: TreeNode[],
    iframeDoc: Document | null,
    currentHighlightedLines: Set<number>
  ): { hoveredLineNumber: number | null } {
    const hoveredLineNumber = hoveredElement 
      ? this.lineMapper.getNodeLineNumbers(hoveredElement, sourceLines)[0] || null 
      : null;
    
    const matchedElements = this.getMatchedElementsFromResults(evaluationResult);
    
    this.codeHighlighter.applyLineHighlights(currentHighlightedLines, hoveredLineNumber);
    
    this.treeHighlighter.updateHighlights(treeNodes, matchedElements, hoveredElement);
    
    this.updateRenderedHighlightsWithHover(evaluationResult, iframeDoc, hoveredElement);
    
    return { hoveredLineNumber };
  }

  updateHighlightsForStep(
    step: ExecutedStep,
    sourceLines: string[],
    treeNodes: TreeNode[],
    iframeDoc: Document | null
  ): { highlightedLines: Set<number> } {
    const highlightedLines = new Set<number>();
    
    step.highlightInfo.sourceLines.forEach((line: number) => {
      highlightedLines.add(line);
    });
    
    this.codeHighlighter.applyLineHighlights(highlightedLines, null);
    
    const matchedElements = new Set<Element>(step.highlightInfo.treeElements);
    this.treeHighlighter.updateHighlights(treeNodes, matchedElements, null);
    
    this.updateRenderedHighlightsForStep(step, iframeDoc);
    
    return { highlightedLines };
  }

  clearAllHighlights(
    treeNodes: TreeNode[],
    iframeDoc: Document | null
  ): void {
    this.codeHighlighter.clearLineHighlights();
    
    this.treeHighlighter.clearHighlights(treeNodes);
    
    this.renderedHighlighter.clearHighlights(iframeDoc);
  }

  private getHighlightedLinesFromResults(
    evaluationResult: EvaluationResult | null,
    sourceLines: string[]
  ): Set<number> {
    const lines = new Set<number>();
    
    for (const match of evaluationResult?.matches ?? []) {
      const nodeLines = this.lineMapper.getNodeLineNumbers(match.node, sourceLines);
      nodeLines.forEach(line => lines.add(line));
    }
    
    return lines;
  }

  private getMatchedElementsFromResults(
    evaluationResult: EvaluationResult | null
  ): Set<Element> {
    const matchedElements = new Set<Element>();
    
    for (const match of evaluationResult?.matches ?? []) {
      if (match.node.nodeType === Node.ELEMENT_NODE) {
        matchedElements.add(match.node as Element);
      }
    }
    
    return matchedElements;
  }

  private updateRenderedHighlightsFromResults(
    evaluationResult: EvaluationResult | null,
    iframeDoc: Document | null
  ): void {
    if (!iframeDoc) return;

    const xpathNodes = evaluationResult?.matches.map(m => m.node) ?? [];
    const iframeNodes = this.iframeNodeMapper.findCorrespondingNodes(xpathNodes, iframeDoc);
    
    this.renderedHighlighter.applyHighlights(iframeDoc, iframeNodes, null);
  }

  private updateRenderedHighlightsWithHover(
    evaluationResult: EvaluationResult | null,
    iframeDoc: Document | null,
    hoveredElement: Element | null
  ): void {
    if (!iframeDoc) return;

    const xpathNodes = evaluationResult?.matches.map(m => m.node) ?? [];
    const iframeNodes = this.iframeNodeMapper.findCorrespondingNodes(xpathNodes, iframeDoc);
    
    let hoveredIframeNode: Node | null = null;
    if (hoveredElement) {
      const hoveredNodes = this.iframeNodeMapper.findCorrespondingNodes([hoveredElement], iframeDoc);
      hoveredIframeNode = hoveredNodes.values().next().value || null;
    }
    
    this.renderedHighlighter.applyHighlights(iframeDoc, iframeNodes, hoveredIframeNode);
  }

  private updateRenderedHighlightsForStep(
    step: ExecutedStep,
    iframeDoc: Document | null
  ): void {
    if (!iframeDoc) return;

    const iframeNodes = this.iframeNodeMapper.findCorrespondingNodes(
      step.matchedNodes,
      iframeDoc
    );

    this.renderedHighlighter.applyHighlights(iframeDoc, iframeNodes, null);
  }
}