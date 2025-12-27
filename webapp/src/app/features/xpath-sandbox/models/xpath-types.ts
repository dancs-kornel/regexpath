export interface Sample {
    id: string;
    name: string;
    type: 'xml' | 'html';
    file: string;
    description?: string;
}

export interface SampleManifest {
    samples: Sample[];
}

export interface XPathResult {
    node: Node;
    type: 'element' | 'attribute' | 'text' | 'comment';
    tagName?: string;
    path: string;
    textContent: string;
    index: number;
}

export interface EvaluationResult {
    matches: XPathResult[];
    error?: string;
    executionTime?: number;
}

export interface TreeNode {
    element: Element;
    tagName: string;
    siblingIndex: number;
    id?: string;
    depth: number;
    hasChildren: boolean;
    children: TreeNode[];
    isExpanded: boolean;
    isHighlighted: boolean;
    isHovered: boolean;
}