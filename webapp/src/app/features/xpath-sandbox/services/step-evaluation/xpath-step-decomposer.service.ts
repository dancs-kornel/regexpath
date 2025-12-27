
import { Injectable } from '@angular/core';
import { XPathStep, XPathDecomposition, StepType } from './models/step-evaluation.models';
import { XPathExplainer } from '../../utils/xpath-explainer/xpath-explainer';
import { XPathExplanationService } from '../xpath-explanation.service';
import { XPathExplanationNode } from '../../models/xpath-explanations.models';

@Injectable({
  providedIn: 'root'
})
export class XPathStepDecomposerService {

  constructor(private explanationService: XPathExplanationService) {}


  async decomposeXPath(xpath: string): Promise<XPathDecomposition> {
    try {
      const trimmed = xpath.trim();
      
      if (!trimmed) {
        return {
          original: xpath,
          steps: [],
          isValid: false,
          error: 'Üres XPath kifejezés'
        };
      }

      await this.explanationService.waitForReady();

      const explanationNodes = this.explanationService.explainExpressionSync(trimmed);

      if (explanationNodes.length === 1 && 
          explanationNodes[0].explanation === 'Hibás XPath kifejezés') {
        return {
          original: xpath,
          steps: [],
          isValid: false,
          error: 'Hibás XPath kifejezés'
        };
      }

      const steps = this.convertNodesToSteps(trimmed, explanationNodes);

      return {
        original: xpath,
        steps,
        isValid: true
      };
    } catch (error: any) {
      return {
        original: xpath,
        steps: [],
        isValid: false,
        error: error.message || 'Nem sikerült feldolgozni az XPath kifejezést'
      };
    }
  }

  private convertNodesToSteps(
    originalXPath: string, 
    nodes: XPathExplanationNode[]
  ): XPathStep[] {
    const steps: XPathStep[] = [];
    let stepIndex = 0;

    steps.push({
      stepIndex: stepIndex++,
      description: 'Kezdés a dokumentum gyökerétől',
      xpathFragment: '',
      stepType: 'root'
    });

    let accumulatedXPath = '';

    for (const node of nodes) {
      if (node.explanation.startsWith('Érvénytelen karakter')) {
        continue;
      }

      if (node.category === 'predicate' && node.depth && node.depth > 0) {
        accumulatedXPath += node.token;
        
        steps.push({
          stepIndex: stepIndex++,
          description: `Szűrés: ${node.explanation}`,
          xpathFragment: accumulatedXPath,
          stepType: 'predicate-filter',
          details: node.token
        });
      } else {
        accumulatedXPath += node.token;
        
        const stepType = this.determineStepType(node);
        
        steps.push({
          stepIndex: stepIndex++,
          description: this.buildStepDescription(node),
          xpathFragment: accumulatedXPath,
          stepType: stepType
        });
      }
    }

    return steps;
  }

  private determineStepType(node: XPathExplanationNode): StepType {
    if (node.token.includes('@')) {
      return 'attribute-selection';
    }

    if (node.token.startsWith('//')) {
      return 'descendant-selection';
    }

    if (node.token.startsWith('/')) {
      return 'child-selection';
    }

    if (node.token.includes('descendant::') || node.token.includes('descendant-or-self::')) {
      return 'descendant-selection';
    }

    if (node.token.includes('child::')) {
      return 'child-selection';
    }

    if (node.token.includes('attribute::')) {
      return 'attribute-selection';
    }

    return 'child-selection';
  }

  private buildStepDescription(node: XPathExplanationNode): string {
    const explanation = node.explanation;

    if (node.token.includes('@')) {
      return `Attribútum kiválasztás: ${explanation}`;
    }

    if (node.token.startsWith('//')) {
      return `Leszármazott kiválasztás: ${explanation}`;
    }

    if (node.token.startsWith('/')) {
      return `Gyermek kiválasztás: ${explanation}`;
    }

    if (node.token.includes('::')) {
      return `Tengely kiválasztás: ${explanation}`;
    }

    return `Kiválasztás: ${explanation}`;
  }
}