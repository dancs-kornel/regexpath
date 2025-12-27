
import { Injectable } from '@angular/core';
import { EvaluationResult, type XPathResult } from '../models/xpath-types';

@Injectable({
  providedIn: 'root'
})
export class EvaluatorService {
  private readonly MAX_TEXT_DISPLAY_LENGTH = 50;

  private document: Document | null = null;
  private nodeLineMap: Map<Node, number> = new Map();

  parseSource(content: string, type: 'xml' | 'html'): Document | null {
    const parser = new DOMParser();
    const mimeType = type === 'xml' ? 'application/xml' : 'text/html';
    
    try {
      this.document = parser.parseFromString(content, mimeType);
      
      const parserError = this.document.querySelector('parsererror');
      if (parserError) {
        return null;
      }
      
      this.buildNodeLineMap(content, this.document);
      
      return this.document;
    } catch (error) {
      return null;
    }
  }

  private buildNodeLineMap(source: string, doc: Document): void {
    this.nodeLineMap.clear();
    const lines = source.split('\n');
    
    const allElements: Element[] = [];
    const walker = doc.createTreeWalker(
      doc.documentElement,
      NodeFilter.SHOW_ELEMENT,
      null
    );

    let currentNode: Node | null = walker.currentNode;
    while (currentNode) {
      if (currentNode.nodeType === Node.ELEMENT_NODE) {
        allElements.push(currentNode as Element);
      }
      currentNode = walker.nextNode();
    }


    const tagOccurrences = new Map<string, number>();

    for (const element of allElements) {
      const tagName = element.tagName.toLowerCase();
      const currentOccurrence = (tagOccurrences.get(tagName) || 0) + 1;
      tagOccurrences.set(tagName, currentOccurrence);

      const lineNumber = this.findNthTagOccurrence(lines, tagName, currentOccurrence, element);
      
      if (lineNumber > 0) {
        this.nodeLineMap.set(element, lineNumber);
      }
    }

  }

  private findNthTagOccurrence(
  lines: string[], 
  tagName: string, 
  occurrence: number, 
  element: Element
): number {
  let count = 0;
  const openingPattern = new RegExp(`<${tagName}[\\s>]`, 'i');

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    if (openingPattern.test(line)) {
      count++;
      
      if (count === occurrence) {
        const matches = this.lineMatchesElement(line, element);
        
        
        if (matches) {
          return i + 1;
        }
      }
    }
  }

  return 0;
}

  private lineMatchesElement(line: string, element: Element): boolean {
    const lineLower = line.toLowerCase();

    if (element.id && lineLower.includes(`id="${element.id}"`)) {
      return true;
    }

    if (element.className && lineLower.includes(`class="${element.className}"`)) {
      return true;
    }

    const href = element.getAttribute('href');
    if (href && lineLower.includes(`href="${href}"`)) {
      return true;
    }

    const firstChild = element.children[0];
    if (firstChild) {
      const childHref = firstChild.getAttribute('href');
      if (childHref && lineLower.includes(`href="${childHref}"`)) {
        return true;
      }
    }

    return true;
  }

  getNodeLineNumber(node: Node): number | null {
    return this.nodeLineMap.get(node) || null;
  }

  evaluate(xpath: string): EvaluationResult {
    if (!this.document) {
      return {
        matches: [],
        error: 'No document loaded'
      };
    }

    if (!xpath || xpath.trim() === '') {
      return {
        matches: [],
        error: 'XPath expression is empty'
      };
    }

    const startTime = performance.now();

    try {
      const result = this.document.evaluate(
        xpath,
        this.document,
        null,
        XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,
        null
      );

      const matches: XPathResult[] = [];
      for (let i = 0; i < result.snapshotLength; i++) {
        const node = result.snapshotItem(i);
        if (node) {
          matches.push(this.createXPathResult(node, i));
        }
      }

      const executionTime = performance.now() - startTime;

      return {
        matches,
        executionTime
      };
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Invalid expression';
      return {
        matches: [],
        error: `XPath error: ${message}`
      };
    }
  }

  private createXPathResult(node: Node, index: number): XPathResult {
    const type = this.getNodeType(node);
    const tagName = this.getTagName(node);
    const path = this.buildPath(node);
    const textContent = this.getTextContent(node);

    return {
      node,
      type,
      tagName,
      path,
      textContent,
      index
    };
  }

  private getNodeType(node: Node): 'element' | 'attribute' | 'text' | 'comment' {
    switch (node.nodeType) {
      case Node.ELEMENT_NODE:
        return 'element';
      case Node.ATTRIBUTE_NODE:
        return 'attribute';
      case Node.TEXT_NODE:
        return 'text';
      case Node.COMMENT_NODE:
        return 'comment';
      default:
        return 'element';
    }
  }

  private getTagName(node: Node): string | undefined {
    if (node.nodeType === Node.ELEMENT_NODE) {
      return (node as Element).tagName.toLowerCase();
    }
    if (node.nodeType === Node.ATTRIBUTE_NODE) {
      return (node as Attr).name;
    }
    return undefined;
  }

  private getTextContent(node: Node): string {
    const text = node.textContent || '';
    return text.length > this.MAX_TEXT_DISPLAY_LENGTH
      ? text.slice(0, this.MAX_TEXT_DISPLAY_LENGTH) + '...'
      : text;
  }

  private buildPath(node: Node): string {
    const parts: string[] = [];
    let current: Node | null = node;

    if (current.nodeType === Node.ATTRIBUTE_NODE) {
      const attr = current as Attr;
      parts.unshift(`@${attr.name}`);
      current = attr.ownerElement;
    }

    if (current && current.nodeType === Node.TEXT_NODE) {
      const textIndex = this.getTextNodeIndex(current);
      parts.unshift(`text()[${textIndex}]`);
      current = current.parentNode;
    }

    while (current && current.nodeType === Node.ELEMENT_NODE) {
      const element = current as Element;
      const tagName = element.tagName.toLowerCase();
      const index = this.getElementIndex(element);
      parts.unshift(`${tagName}[${index}]`);
      current = element.parentNode;
    }

    return '//' + parts.join('/');
  }

  private getElementIndex(element: Element): number {
    const parent = element.parentNode;
    if (!parent) return 1;

    const siblings = Array.from(parent.childNodes).filter(
      node => node.nodeType === Node.ELEMENT_NODE && 
              (node as Element).tagName === element.tagName
    );

    return siblings.indexOf(element) + 1;
  }

  private getTextNodeIndex(textNode: Node): number {
    const parent = textNode.parentNode;
    if (!parent) return 1;

    const textSiblings = Array.from(parent.childNodes).filter(
      node => node.nodeType === Node.TEXT_NODE && 
              node.textContent?.trim() !== ''
    ) as ChildNode[];

    return textSiblings.indexOf(textNode as ChildNode) + 1;
  }

  getDocument(): Document | null {
    return this.document;
  }
}