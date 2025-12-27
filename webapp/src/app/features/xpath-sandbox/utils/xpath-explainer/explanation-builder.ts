
import { XPathExplanations, XPathCategory } from '../../models/xpath-explanations.models';

export class ExplanationBuilder {
  
  constructor(private explanations: XPathExplanations) {}


  generateMainExplanation(axis: string, nodeTest: string): string {
    const parts: string[] = [];

    if (axis) {
      const axisExplanation = this.explanations.axes[axis];
      if (axisExplanation) {
        parts.push(axisExplanation);
      }
    }

    if (nodeTest) {
      const nodeTestExplanation = this.explanations.nodeTests[nodeTest];
      if (nodeTestExplanation) {
        parts.push(nodeTestExplanation);
      } else if (nodeTest !== '*') {
        parts.push(`'${nodeTest}' elem`);
      }
    }

    if (parts.length === 0) {
      return 'XPath kifejezés';
    }

    return this.combineMainParts(parts, axis, nodeTest);
  }

  combineMainParts(parts: string[], axis: string, nodeTest: string): string {
    if (axis === '@') {
      if (nodeTest) {
        return `${nodeTest} attribútum`;
      }
      return parts.join(' ');
    }

    if (axis === '//' && nodeTest && nodeTest !== '*') {
      return `Minden ${parts.slice(1).join(' ')}`;
    }

    if (axis === '/' && nodeTest && nodeTest !== '*') {
      return parts.slice(1).join(' ');  
    }

    return parts.join(' ');
  }

  determineCategory(axis: string, nodeTest: string, predicates: string[]): XPathCategory {
    if (axis && nodeTest) return 'combined';
    if (axis) return 'axis';
    if (nodeTest) return 'nodeTest';
    if (predicates.length > 0) return 'predicate';
    return 'combined';
  }
}