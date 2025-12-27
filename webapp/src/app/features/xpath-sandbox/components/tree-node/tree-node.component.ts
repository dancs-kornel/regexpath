
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TreeNode } from '../../models/xpath-types';

@Component({
  selector: 'app-tree-node',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tree-node.component.html',
  styleUrls: ['./tree-node.component.css']
})
export class TreeNodeComponent {
  @Input() node!: TreeNode;
  @Output() nodeToggled = new EventEmitter<void>();

  toggleExpand(): void {
    if (this.node.hasChildren) {
      this.node.isExpanded = !this.node.isExpanded;
      this.nodeToggled.emit();
    }
  }

  get depth(): number {
    return (this.node as any).depth || 0;
  }
}