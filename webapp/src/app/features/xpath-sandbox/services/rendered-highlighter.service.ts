
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RenderedHighlighterService {
  private readonly HIGHLIGHT_CLASS = 'xpath-highlight';
  private readonly HOVER_CLASS = 'xpath-hover';
  private readonly MARK_TAG = 'mark';
  private readonly STYLE_ID = 'xpath-highlight-styles';
  private readonly HIGHLIGHT_COLOR = 'yellow';
  private readonly HOVER_COLOR = 'lightblue';
  private readonly OUTLINE_WIDTH = '3px';
  private readonly OUTLINE_OFFSET = '2px';

  private highlightedElements: Set<Element> = new Set();
  private markedTextNodes: Map<Node, HTMLElement> = new Map();

  applyHighlights(
    iframeDoc: Document | null,
    matchedNodes: Set<Node>,
    hoveredNode: Node | null
  ): void {
    if (!iframeDoc) return;

    this.clearHighlights(iframeDoc);

    this.injectStyles(iframeDoc);

    for (const node of matchedNodes) {
      if (node.nodeType === Node.ELEMENT_NODE) {
        this.highlightElement(node as Element, node === hoveredNode);
      } else if (node.nodeType === Node.TEXT_NODE) {
        this.highlightTextNode(node, node === hoveredNode);
      }
    }
  }

  clearHighlights(iframeDoc: Document | null): void {
    if (!iframeDoc) return;

    for (const element of this.highlightedElements) {
      element.classList.remove(this.HIGHLIGHT_CLASS, this.HOVER_CLASS);
    }
    this.highlightedElements.clear();

    for (const [textNode, markElement] of this.markedTextNodes) {
      const parent = markElement.parentNode;
      if (parent) {
        parent.replaceChild(textNode, markElement);
      }
    }
    this.markedTextNodes.clear();
  }

  private highlightElement(element: Element, isHovered: boolean): void {
    element.classList.add(this.HIGHLIGHT_CLASS);
    if (isHovered) {
      element.classList.add(this.HOVER_CLASS);
    }
    this.highlightedElements.add(element);
  }

  private highlightTextNode(textNode: Node, isHovered: boolean): void {
    const parent = textNode.parentNode;
    if (!parent) return;

    const doc = parent.ownerDocument;
    if (!doc) return;

    const mark = doc.createElement(this.MARK_TAG);
    mark.textContent = textNode.textContent;
    mark.classList.add(this.HIGHLIGHT_CLASS);
    if (isHovered) {
      mark.classList.add(this.HOVER_CLASS);
    }

    parent.replaceChild(mark, textNode);
    this.markedTextNodes.set(textNode, mark);
  }

  private injectStyles(iframeDoc: Document): void {
    if (iframeDoc.getElementById(this.STYLE_ID)) return;

    const style = iframeDoc.createElement('style');
    style.id = this.STYLE_ID;
    style.textContent = `
      .${this.HIGHLIGHT_CLASS} {
        outline: ${this.OUTLINE_WIDTH} solid ${this.HIGHLIGHT_COLOR} !important;
        outline-offset: ${this.OUTLINE_OFFSET};
      }

      .${this.HOVER_CLASS} {
        outline: ${this.OUTLINE_WIDTH} solid ${this.HOVER_COLOR} !important;
        outline-offset: ${this.OUTLINE_OFFSET};
      }

      mark.${this.HIGHLIGHT_CLASS} {
        background-color: ${this.HIGHLIGHT_COLOR};
        outline: none;
      }

      mark.${this.HOVER_CLASS} {
        background-color: ${this.HOVER_COLOR};
        outline: none;
      }
    `;

    iframeDoc.head.appendChild(style);
  }
}