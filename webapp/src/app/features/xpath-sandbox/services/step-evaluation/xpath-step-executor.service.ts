
import { Injectable } from '@angular/core';
import { XPathStep, ExecutedStep, HighlightInfo } from './models/step-evaluation.models';
import { EvaluatorService } from '../evaluator.service';
import { LineMapperService } from '../line-mapper.service';
import { TreeHighlighterService } from '../tree-highlighter.service';
import { IframeNodeMapperService } from '../iframe-node-mapper.service';

@Injectable({
  providedIn: 'root'
})
export class XPathStepExecutorService {

  constructor(
    private evaluatorService: EvaluatorService,
    private lineMapperService: LineMapperService,
    private treeHighlighterService: TreeHighlighterService,
    private iframeNodeMapperService: IframeNodeMapperService
  ) {}

  executeSteps(steps: XPathStep[], sourceLines: string[]): ExecutedStep[] {
    const executedSteps: ExecutedStep[] = [];

    for (const step of steps) {
      const executed = this.executeStep(step, sourceLines);
      executedSteps.push(executed);
    }

    return executedSteps;
  }

  private executeStep(step: XPathStep, sourceLines: string[]): ExecutedStep {
    if (step.stepType === 'root') {
      return {
        ...step,
        matchedNodes: [],
        matchCount: 0,
        highlightInfo: this.createEmptyHighlightInfo()
      };
    }

    if (!step.xpathFragment) {
      return {
        ...step,
        matchedNodes: [],
        matchCount: 0,
        highlightInfo: this.createEmptyHighlightInfo()
      };
    }

    try {
      const result = this.evaluatorService.evaluate(step.xpathFragment);

      if (result.error) {
        console.warn(`Step ${step.stepIndex} evaluation error:`, result.error);
        return {
          ...step,
          matchedNodes: [],
          matchCount: 0,
          highlightInfo: this.createEmptyHighlightInfo()
        };
      }

      const matchedNodes = result.matches.map(m => m.node);

      const highlightInfo = this.buildHighlightInfo(matchedNodes, sourceLines);

      return {
        ...step,
        matchedNodes,
        matchCount: matchedNodes.length,
        highlightInfo
      };
    } catch (error) {
      console.error(`Failed to execute step ${step.stepIndex}:`, error);
      return {
        ...step,
        matchedNodes: [],
        matchCount: 0,
        highlightInfo: this.createEmptyHighlightInfo()
      };
    }
  }

  private createEmptyHighlightInfo(): HighlightInfo {
    return {
      sourceLines: [],
      treeElements: [],
      renderedNodes: []
    };
  }

  private buildHighlightInfo(nodes: Node[], sourceLines: string[]): HighlightInfo {
    const sourceLineNumbers = new Set<number>();
    const treeElements = new Set<Element>();

    for (const node of nodes) {
      const lines = this.lineMapperService.getNodeLineNumbers(node, sourceLines);
      lines.forEach(line => sourceLineNumbers.add(line));

      if (node.nodeType === Node.ELEMENT_NODE) {
        treeElements.add(node as Element);
      }
    }

    const renderedNodes = nodes;

    return {
      sourceLines: Array.from(sourceLineNumbers).sort((a, b) => a - b),
      treeElements: Array.from(treeElements),
      renderedNodes
    };
  }

  getStepSummary(step: ExecutedStep): string {
    if (step.stepType === 'root') {
      return 'Starting point';
    }

    if (step.matchCount === 0) {
      return 'No matches';
    }

    if (step.matchCount === 1) {
      return '1 match';
    }

    return `${step.matchCount} matches`;
  }
}