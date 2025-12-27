
import { XPathExplanationNode, XPathExplanations } from '../../models/xpath-explanations.models';
import { XPathParser } from './xpath-parser';
import { PredicateExplainer } from './predicate-explainer';
import { ExplanationBuilder } from './explanation-builder';

export class XPathExplainer {
  private parser: XPathParser;
  private predicateExplainer: PredicateExplainer;
  private explanationBuilder: ExplanationBuilder;
  private currentDepth = 0;

  constructor(expression: string, explanations: XPathExplanations) {
    this.parser = new XPathParser(expression);
    this.predicateExplainer = new PredicateExplainer();
    this.explanationBuilder = new ExplanationBuilder(explanations);
  }

  parse(): XPathExplanationNode[] {
    if (!this.parser.getExpression()) {
      return [];
    }

    try {
      return this.parseExpression();
    } catch (error) {
      console.error('Error parsing XPath:', error);
      return [{
        token: this.parser.getExpression(),
        explanation: 'Hibás XPath kifejezés',
        category: 'combined'
      }];
    }
  }


  private parseExpression(): XPathExplanationNode[] {
    const nodes: XPathExplanationNode[] = [];
    
    while (!this.parser.isComplete()) {
      const stepNodes = this.parseStep();
      if (stepNodes && stepNodes.length > 0) {
        nodes.push(...stepNodes);
      }
    }

    return nodes;
  }

  private parseStep(): XPathExplanationNode[] {
    const startPos = this.parser.getPosition();
    const nodes: XPathExplanationNode[] = [];
    
    const axis = this.parser.parseAxis();
    const nodeTest = this.parser.parseNodeTest();
    const predicates = this.parser.parsePredicates();

    
    if (axis && !nodeTest && predicates.length === 0 && (axis === '/' || axis === '//')) {
      return [];
    }

    const mainEndPos = this.parser.getPosition() - predicates.reduce((sum, p) => sum + p.length, 0);
    const mainToken = this.parser.substring(startPos, mainEndPos);
    
    if (!mainToken && predicates.length === 0) {
      const skipped = this.parser.skipInvalidChar();
      if (skipped) {
        return [{
          token: skipped,
          explanation: `Érvénytelen karakter: '${skipped}'`,
          depth: this.currentDepth,
          category: 'combined'
        }];
      }
      return [];
    }

    if (mainToken) {
      const mainExplanation = this.explanationBuilder.generateMainExplanation(axis, nodeTest);
      nodes.push({
        token: mainToken.trim(),
        explanation: mainExplanation,
        depth: this.currentDepth,
        category: this.explanationBuilder.determineCategory(axis, nodeTest, [])
      });
    }

    if (predicates.length > 0) {
      predicates.forEach(pred => {
        const predExplanation = this.predicateExplainer.explainPredicate(pred);
        if (predExplanation) {
          nodes.push({
            token: pred,
            explanation: predExplanation,
            depth: this.currentDepth + 1,  
            category: 'predicate'
          });
        }
      });
    }

    return nodes;
  }
}