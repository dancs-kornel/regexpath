
import { Injectable } from '@angular/core';
import { TreeNode } from '../models/xpath-types';

@Injectable({
  providedIn: 'root'
})
export class TreeBuilderService {
  private readonly DEFAULT_EXPANDED_DEPTH = 2;

  buildTree(document: Document | null): TreeNode[] {
    if (!document || !document.documentElement) {
      return [];
    }

    return this.buildNodeTree(document.documentElement, 0);
  }

  private buildNodeTree(element: Element, depth: number): TreeNode[] {
    const siblingIndex = this.calculateSiblingIndex(element);
    const children = this.getElementChildren(element);
    
    const node: TreeNode = {
      element: element,
      tagName: element.tagName.toLowerCase(),
      siblingIndex: siblingIndex,
      id: element.getAttribute('id') || undefined,
      depth: depth,
      hasChildren: children.length > 0,
      children: [],
      isExpanded: depth < this.DEFAULT_EXPANDED_DEPTH,
      isHighlighted: false,
      isHovered: false
    };

    if (children.length > 0) {
      const childDepth = depth + 1;
      children.forEach(child => {
        const childNodes = this.buildNodeTree(child, childDepth);
        node.children.push(...childNodes);
      });
    }

    return [node];
  }

  private calculateSiblingIndex(element: Element): number {
    const parent = element.parentElement;
    if (!parent) return 1;

    const siblings = Array.from(parent.children).filter(
      child => child.tagName === element.tagName
    );

    return siblings.indexOf(element) + 1;
  }

  private getElementChildren(element: Element): Element[] {
    return Array.from(element.children);
  }
}