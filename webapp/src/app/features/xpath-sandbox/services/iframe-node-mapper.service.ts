
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class IframeNodeMapperService {

  findCorrespondingNodes(
    xpathNodes: Node[],
    iframeDoc: Document | null
  ): Set<Node> {
    const correspondingNodes = new Set<Node>();
    
    if (!iframeDoc) return correspondingNodes;

    for (const node of xpathNodes) {
      const iframeNode = this.findCorrespondingNode(node, iframeDoc);
      if (iframeNode) {
        correspondingNodes.add(iframeNode);
      }
    }

    return correspondingNodes;
  }

  private findCorrespondingNode(node: Node, iframeDoc: Document): Node | null {
    if (node.nodeType === Node.ELEMENT_NODE) {
      return this.findCorrespondingElement(node as Element, iframeDoc);
    } else if (node.nodeType === Node.TEXT_NODE) {
      return this.findCorrespondingTextNode(node, iframeDoc);
    }
    
    return null;
  }


  private findCorrespondingElement(element: Element, iframeDoc: Document): Element | null {
    const xpath = this.buildElementXPath(element);
    
    try {
      const result = iframeDoc.evaluate(
        xpath,
        iframeDoc,
        null,
        XPathResult.FIRST_ORDERED_NODE_TYPE,
        null
      );
      
      return result.singleNodeValue as Element;
    } catch (error) {
      console.error('Error finding corresponding element:', error);
      return null;
    }
  }


  private buildElementXPath(element: Element): string {
    const parts: string[] = [];
    let current: Element | null = element;

    while (current && current.nodeType === Node.ELEMENT_NODE) {
      const tagName = current.tagName.toLowerCase();
      const index = this.getSiblingIndex(current);
      parts.unshift(`${tagName}[${index}]`);
      current = current.parentElement;
    }

    return '/' + parts.join('/');
  }


  private getSiblingIndex(element: Element): number {
    const parent = element.parentElement;
    if (!parent) return 1;

    const siblings = Array.from(parent.children).filter(
      child => child.tagName === element.tagName
    );

    return siblings.indexOf(element) + 1;
  }

  private findCorrespondingTextNode(textNode: Node, iframeDoc: Document): Node | null {
    const parent = textNode.parentNode;
    if (!parent || parent.nodeType !== Node.ELEMENT_NODE) return null;

    const iframeParent = this.findCorrespondingElement(parent as Element, iframeDoc);
    if (!iframeParent) return null;

    const textIndex = this.getTextNodeIndex(textNode);
    return this.getTextNodeAtIndex(iframeParent, textIndex);
  }


  private getTextNodeIndex(textNode: Node): number {
    const parent = textNode.parentNode;
    if (!parent) return 0;

    const textNodes = Array.from(parent.childNodes).filter(
      node => node.nodeType === Node.TEXT_NODE && node.textContent?.trim() !== ''
    ) as ChildNode[];

    return textNodes.indexOf(textNode as ChildNode);
  }

  private getTextNodeAtIndex(parent: Element, index: number): Node | null {
    const textNodes = Array.from(parent.childNodes).filter(
      node => node.nodeType === Node.TEXT_NODE && node.textContent?.trim() !== ''
    );

    return textNodes[index] || null;
  }
}