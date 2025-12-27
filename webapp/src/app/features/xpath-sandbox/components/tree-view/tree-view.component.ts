
import { Component, Input, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { TreeNode } from '../../models/xpath-types';
import { TreeNodeComponent } from '../tree-node/tree-node.component';

@Component({
  selector: 'app-tree-view',
  standalone: true,
  imports: [CommonModule, TreeNodeComponent, ScrollingModule],
  templateUrl: './tree-view.component.html',
  styleUrls: ['./tree-view.component.css']
})
export class TreeViewComponent implements OnChanges {
  @Input() treeNodes: TreeNode[] = [];
  
  flattenedNodes: TreeNode[] = [];
  
  constructor(private cdr: ChangeDetectorRef) {}
  
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['treeNodes']) {
      this.flattenTree();
    }
  }

  private flattenTree(): void {
    this.flattenedNodes = [];
    this.flattenNodes(this.treeNodes);
  }

  private flattenNodes(nodes: TreeNode[], depth: number = 0): void {
    for (const node of nodes) {
      (node as any).depth = depth;
      this.flattenedNodes.push(node);
      
      if (node.isExpanded && node.hasChildren && node.children && node.children.length > 0) {
        this.flattenNodes(node.children, depth + 1);
      }
    }
  }

  onNodeToggle(): void {
    this.flattenTree();
    this.cdr.detectChanges();
  }

  trackByNode(index: number, node: TreeNode): any {
    return node.element || node.tagName + node.siblingIndex || index;
  }
}