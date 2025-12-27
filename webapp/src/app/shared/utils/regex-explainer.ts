export interface ExplainedNode {
  token: string;
  explanation: string;
  children?: ExplainedNode[];
}

export interface RegexExplanations {
  escapes: Record<string, string>;
  quantifiers: Record<string, string>;
  groups: Record<string, string>;
  anchors: Record<string, string>;
  special: Record<string, string>;
  ranges: Record<string, string>;
  numbers: {
    generic: string;
    [key: string]: string;
  };
  characters: {
    generic: string;
    [key: string]: string;
  };
}

export class RegexParser {
  private nodes: ExplainedNode[] = [];
  private stack: { type: string; nodes: ExplainedNode[] }[] = [];
  private current: ExplainedNode[] = this.nodes;
  private buffer = '';

  private readonly groupOpeners = new Map([
    ['[', '[]'],
    ['(', '()']
  ]);
  
  private readonly groupClosers = new Set([']', ')']);
  private readonly quantifiers = new Set(['+', '*', '?']);
  private readonly anchors = new Set(['^', '$']);
  private readonly specialChars = new Set(['.', '|']);

  constructor(
    private readonly pattern: string, 
    private readonly explanations: RegexExplanations
  ) {}

  public parse(): ExplainedNode[] {
    for (let i = 0; i < this.pattern.length; i++) {
      i = this.processCharacter(i);
    }
    
    this.flushBuffer();
    return this.nodes;
  }

  private processCharacter(i: number): number {
    const char = this.pattern[i];

    if (char === '\\') return this.handleEscape(i);
    
    const lookaround = this.checkLookaround(i);
    if (lookaround) return this.handleLookaround(lookaround);
    
    if (this.groupOpeners.has(char)) return this.handleGroupOpener(char, i);
    if (this.groupClosers.has(char)) return this.handleGroupCloser(i);
    if (char === '{') return this.handleBraceQuantifier(i);
    if (this.quantifiers.has(char)) return this.handleQuantifier(char, i);
    if (this.anchors.has(char)) return this.handleAnchor(char, i);
    if (this.specialChars.has(char)) return this.handleSpecialChar(char, i);
    
    const rangeResult = this.tryHandleRange(char, i);
    if (rangeResult !== null) return rangeResult;
    
    return this.handleRegularChar(char, i);
  }

  private handleEscape(i: number): number {
    this.flushBuffer();
    const nextChar = this.pattern[i + 1];
    const token = nextChar ? `\\${nextChar}` : '\\';
    this.addNode(token);
    return nextChar ? i + 1 : i;
  }

  private checkLookaround(i: number): { type: string; endIndex: number } | null {
    if (this.pattern[i] !== '(' || i >= this.pattern.length - 2) return null;
    
    const remaining = this.pattern.substring(i);
    const lookarounds = [
      { prefix: '(?=', type: '(?=)', length: 3 },
      { prefix: '(?!', type: '(?!)', length: 3 },
      { prefix: '(?<=', type: '(?<=)', length: 4 },
      { prefix: '(?<!', type: '(?<!)', length: 4 },
      { prefix: '(?:', type: '(?:)', length: 3 }
    ];

    for (const { prefix, type, length } of lookarounds) {
      if (remaining.startsWith(prefix)) {
        return { type, endIndex: i + length - 1 };
      }
    }
    
    return null;
  }

  private handleLookaround(lookaround: { type: string; endIndex: number }): number {
    this.flushBuffer();
    this.startGroup(lookaround.type);
    return lookaround.endIndex;
  }

  private handleGroupOpener(char: string, i: number): number {
    this.flushBuffer();
    this.startGroup(this.groupOpeners.get(char)!);
    return i;
  }

  private handleGroupCloser(i: number): number {
    this.endGroup();
    return i;
  }

  private handleQuantifier(char: string, i: number): number {
  if (this.buffer.length > 1) {
    const lastChar = this.buffer.slice(-1);
    this.buffer = this.buffer.slice(0, -1);
    this.flushBuffer();
    this.buffer = lastChar;
  }
  
  this.flushBuffer();
  this.addNode(char);
  return i;
}

  private handleAnchor(char: string, i: number): number {
    this.flushBuffer();
    this.addNode(char);
    return i;
  }

  private handleSpecialChar(char: string, i: number): number {
    this.flushBuffer();
    this.addNode(char);
    return i;
  }

  private tryHandleRange(char: string, i: number): number | null {
    if (!this.isInCharacterClass() || char !== '-') return null;
    if (this.buffer.length === 0 || i >= this.pattern.length - 1) return null;

    const prevChar = this.buffer.slice(-1);
    const nextChar = this.pattern[i + 1];
    
    if (!this.isValidRange(prevChar, nextChar)) return null;

    this.buffer = this.buffer.slice(0, -1);
    this.flushBuffer();
    this.addNode(`${prevChar}-${nextChar}`);
    return i + 1;
  }

  private handleRegularChar(char: string, i: number): number {
    this.buffer += char;
    return i;
  }

  private handleBraceQuantifier(startIndex: number): number {
    this.flushBuffer();
    const { endIndex, content } = this.findBraceContent(startIndex);
    
    const groupNode: ExplainedNode = {
      token: '{}',
      explanation: this.explanations.quantifiers['{'],
      children: []
    };
    
    this.pushGroup(groupNode);
    this.parseQuantifierContent(content);
    this.popGroup();
    
    return endIndex;
  }

  private findBraceContent(startIndex: number): { endIndex: number; content: string } {
    for (let i = startIndex + 1; i < this.pattern.length; i++) {
      if (this.pattern[i] === '}') {
        return {
          endIndex: i,
          content: this.pattern.substring(startIndex + 1, i)
        };
      }
    }
    return { endIndex: startIndex, content: '' };
  }

  private parseQuantifierContent(content: string): void {
    if (!content.includes(',')) {
      this.addNode(content);
      return;
    }

    const parts = content.split(',');
    parts.forEach((part, index) => {
      if (part.trim()) this.addNode(part.trim());
      if (index < parts.length - 1) this.addNode(',');
    });
  }

  private startGroup(type: string): void {
    const groupNode: ExplainedNode = {
      token: type,
      explanation: this.explanations.groups[type] || `Ismeretlen csoport: ${type}`,
      children: []
    };
    this.pushGroup(groupNode);
  }

  private pushGroup(groupNode: ExplainedNode): void {
    this.current.push(groupNode);
    this.stack.push({ type: groupNode.token, nodes: this.current });
    this.current = groupNode.children!;
  }

  private endGroup(): void {
    this.flushBuffer();
    this.popGroup();
  }

  private popGroup(): void {
    const popped = this.stack.pop();
    if (popped) {
      this.current = popped.nodes;
    }
  }

  private addNode(token: string): void {
    this.current.push({
      token,
      explanation: this.getExplanation(token)
    });
  }

  private getExplanation(token: string): string {
    if (this.explanations.escapes[token]) return this.explanations.escapes[token];
    if (this.explanations.quantifiers[token]) return this.explanations.quantifiers[token];
    if (this.explanations.anchors[token]) return this.explanations.anchors[token];
    if (this.explanations.special[token]) return this.explanations.special[token];
    
    if (this.isInCharacterClass() && this.isCharacterRange(token)) {
      return this.explainCharacterRange(token);
    }
    
    if (this.explanations.ranges[token]) return this.explanations.ranges[token];
    if (/^\d+$/.test(token)) return `${this.explanations.numbers.generic}: ${token}`;
    if (token === ',') return 'Elválasztó vessző';
    
    if (token === '-') {
      return this.isInCharacterClass() 
        ? 'Tartomány jelölő (kötőjel)'
        : `${this.explanations.characters.generic}: ${token}`;
    }

    return `${this.explanations.characters.generic}: ${token}`;
  }

  private isValidRange(start: string, end: string): boolean {
    if (!start || !end) return false;
    return start.charCodeAt(0) < end.charCodeAt(0) && this.isSameCharacterType(start, end);
  }

  private isCharacterRange(token: string): boolean {
    const match = token.match(/^(.)-(.)$/);
    if (!match) return false;
    
    const [, start, end] = match;
    return this.isValidRange(start, end);
  }

  private isSameCharacterType(char1: string, char2: string): boolean {
    const getType = (char: string) => {
      if (char >= 'a' && char <= 'z') return 'lower';
      if (char >= 'A' && char <= 'Z') return 'upper';
      if (char >= '0' && char <= '9') return 'digit';
      return 'other';
    };
    
    return getType(char1) === getType(char2);
  }

  private explainCharacterRange(token: string): string {
    const match = token.match(/^(.)-(.)$/);
    if (!match) return `${this.explanations.characters.generic}: ${token}`;
    
    const [, start, end] = match;
    
    if (start >= '0' && start <= '9') return `Számjegyek ${start}-tól ${end}-ig`;
    if (start >= 'a' && start <= 'z') return `Kisbetűk ${start}-tól ${end}-ig`;
    if (start >= 'A' && start <= 'Z') return `Nagybetűk ${start}-tól ${end}-ig`;
    
    return `Karakterek ${start}-tól ${end}-ig`;
  }

  private isInCharacterClass(): boolean {
    return this.stack.some(item => item.type === '[]');
  }

  private flushBuffer(): void {
    if (!this.buffer.trim()) return;
    
    if (this.isInCharacterClass()) {
      for (const char of this.buffer) {
        this.addNode(char);
      }
    } else {
      this.addNode(this.buffer);
    }
    
    this.buffer = '';
  }
}