
export interface XPathExplanationNode {
  token: string;          
  explanation: string;    
  depth?: number;        
  category?: XPathCategory;
}

export type XPathCategory = 
  | 'axis'           // Axis specifiers (/, //, @, etc.)
  | 'nodeTest'       // Node tests (element names, *, text(), etc.)
  | 'predicate'      // Predicates [...]
  | 'operator'       // Operators (=, !=, <, >, and, or)
  | 'function'       // XPath functions (position(), last(), etc.)
  | 'literal'        // String or number literals
  | 'combined';      // Combined constructs (axis + nodeTest)


export interface XPathExplanations {
  axes: Record<string, string>;         
  nodeTests: Record<string, string>;    
  operators: Record<string, string>;    
  functions: Record<string, string>;    
  predicates: {
    position: string;                   
    attribute: string;                   
    function: string;                   
    comparison: string;                  
  };
  common: Record<string, string>;      
}