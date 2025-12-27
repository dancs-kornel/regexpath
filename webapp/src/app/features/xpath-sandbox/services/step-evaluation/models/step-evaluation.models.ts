
export interface XPathStep {
  stepIndex: number;
  description: string;        
  xpathFragment: string;       
  stepType: StepType;
  details?: string;            
}


export type StepType = 
  | 'root'                     
  | 'descendant-selection'     
  | 'child-selection'        
  | 'attribute-selection'     
  | 'predicate-filter';       

export interface ExecutedStep extends XPathStep {
  matchedNodes: Node[];
  matchCount: number;
  highlightInfo: HighlightInfo;
}


export interface HighlightInfo {
  sourceLines: number[];
  treeElements: Element[];
  renderedNodes: Node[];
}


export interface XPathDecomposition {
  original: string;
  steps: XPathStep[];
  isValid: boolean;
  error?: string;
}


export interface PlaybackState {
  steps: ExecutedStep[];
  currentStepIndex: number;
  isPlaying: boolean;
  speed: number;             
  isEnabled: boolean;       
}