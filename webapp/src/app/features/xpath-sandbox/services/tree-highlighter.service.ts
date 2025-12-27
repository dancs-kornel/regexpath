
import { Injectable } from '@angular/core';
import { TreeNode } from '../models/xpath-types';

@Injectable({
  providedIn: 'root'
})
export class TreeHighlighterService {

  updateHighlights(
    treeNodes: TreeNode[],
    matchedElements: Set<Element>,
    hoveredElement: Element | null
  ): void {
    this.traverseAndHighlight(treeNodes, matchedElements, hoveredElement);
  }

  clearHighlights(treeNodes: TreeNode[]): void {
    this.traverseAndClear(treeNodes);
  }

  private traverseAndHighlight(
    nodes: TreeNode[],
    matchedElements: Set<Element>,
    hoveredElement: Element | null
  ): void {
    for (const node of nodes) {
      const isMatched = matchedElements.has(node.element);
      const isHovered = hoveredElement === node.element;

      node.isHighlighted = isMatched;
      node.isHovered = isHovered;

      if (node.children.length > 0) {
        this.traverseAndHighlight(node.children, matchedElements, hoveredElement);
      }
    }
  }

  private traverseAndClear(nodes: TreeNode[]): void {
    for (const node of nodes) {
      node.isHighlighted = false;
      node.isHovered = false;

      if (node.children.length > 0) {
        this.traverseAndClear(node.children);
      }
    }
  }
}