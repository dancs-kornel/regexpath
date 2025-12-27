
export class XPathParser {
  private pos = 0;
  private expression: string;

  constructor(expression: string) {
    this.expression = expression.trim();
  }


  reset(expression?: string): void {
    this.pos = 0;
    if (expression !== undefined) {
      this.expression = expression.trim();
    }
  }


  getPosition(): number {
    return this.pos;
  }

  getExpression(): string {
    return this.expression;
  }

  isComplete(): boolean {
    this.skipWhitespace();
    return this.pos >= this.expression.length;
  }

  parseAxis(): string {
    this.skipWhitespace();
    
    if (this.peek(2) === '//') {
      this.pos += 2;
      return '//';
    }
    
    if (this.peek() === '/') {
      this.pos += 1;
      return '/';
    }
    
    if (this.peek() === '@') {
      this.pos += 1;
      return '@';
    }

    const axisMatch = this.expression.substring(this.pos).match(/^(child|descendant|parent|ancestor|following-sibling|preceding-sibling|following|preceding|attribute|descendant-or-self|ancestor-or-self)::/);
    if (axisMatch) {
      this.pos += axisMatch[0].length;
      return axisMatch[0];
    }

    if (this.peek() === '.') {
      if (this.peek(2) === '..') {
        this.pos += 2;
        return '..';
      }
      this.pos += 1;
      return '.';
    }

    return '';
  }

  parseNodeTest(): string {
    this.skipWhitespace();

    if (this.peek() === '*') {
      this.pos += 1;
      return '*';
    }

    const nodeTypeMatch = this.expression.substring(this.pos).match(/^(text|comment|node|processing-instruction)\(\)/);
    if (nodeTypeMatch) {
      this.pos += nodeTypeMatch[0].length;
      return nodeTypeMatch[0];
    }

 
    const elementMatch = this.expression.substring(this.pos).match(/^[a-zA-Z_][\w\-]*/);
    if (elementMatch) {
      const afterElement = this.pos + elementMatch[0].length;
      if (this.expression.substring(afterElement, afterElement + 2) === '::') {
        return '';
      }
      this.pos += elementMatch[0].length;
      return elementMatch[0];
    }

    return '';
  }

  parsePredicates(): string[] {
    const predicates: string[] = [];
    
    while (this.peek() === '[') {
      const predicate = this.parsePredicate();
      if (predicate) {
        predicates.push(predicate);
      }
    }

    return predicates;
  }


  parsePredicate(): string {
    if (this.peek() !== '[') return '';

    const startPos = this.pos;
    this.pos++; 
    
    let depth = 1;
    while (this.pos < this.expression.length && depth > 0) {
      if (this.expression[this.pos] === '[') depth++;
      if (this.expression[this.pos] === ']') depth--;
      this.pos++;
    }

    return this.expression.substring(startPos, this.pos);
  }


  skipInvalidChar(): string {
    if (this.pos >= this.expression.length) return '';
    const char = this.expression[this.pos];
    this.pos++;
    return char;
  }


  substring(start: number, end: number): string {
    return this.expression.substring(start, end);
  }

  peek(count: number = 1): string {
    return this.expression.substring(this.pos, this.pos + count);
  }

  skipWhitespace(): void {
    while (this.pos < this.expression.length && /\s/.test(this.expression[this.pos])) {
      this.pos++;
    }
  }
}