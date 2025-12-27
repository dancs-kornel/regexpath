
import { Injectable } from '@angular/core';
import { EvaluatorService } from './evaluator.service';

@Injectable({
  providedIn: 'root'
})
export class LineMapperService {
  
  constructor(private evaluatorService: EvaluatorService) {}
  

  getNodeLineNumbers(node: Node, sourceLines: string[]): number[] {
  const lineNumber = this.evaluatorService.getNodeLineNumber(node);
  
  if (lineNumber !== null && lineNumber > 0) {
    return [lineNumber];
  }
  
  const fallback = this.fallbackTextSearch(node, sourceLines);
  return fallback;
}

  private fallbackTextSearch(node: Node, sourceLines: string[]): number[] {
    const searchStrings = this.getNodeSearchStrings(node);
    if (searchStrings.length === 0) return [];
    
    const lines: number[] = [];
    
    sourceLines.forEach((line, index) => {
      const lineLower = line.toLowerCase();
      const matchesAll = searchStrings.every(str => lineLower.includes(str.toLowerCase()));
      if (matchesAll) {
        lines.push(index + 1);
      }
    });

    if (lines.length > 0) {
      return [...new Set(lines)];
    }

    sourceLines.forEach((line, index) => {
      const lineLower = line.toLowerCase();
      for (const searchString of searchStrings) {
        if (lineLower.includes(searchString.toLowerCase())) {
          lines.push(index + 1);
          break;
        }
      }
    });

    return [...new Set(lines)];
  }

  private getNodeSearchStrings(node: Node): string[] {
    if (node.nodeType === Node.TEXT_NODE) {
      const text = (node.textContent || '').trim();
      return text ? [text] : [];
    } else if (node.nodeType === Node.COMMENT_NODE) {
      const text = (node.textContent || '').trim();
      return text ? [`<!--${text}-->`] : [];
    } else if (node.nodeType === Node.ATTRIBUTE_NODE) {
      const attr = node as Attr;
      return [`${attr.name}="${attr.value}"`];
    }
    
    return [];
  }
}