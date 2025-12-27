
export class PredicateExplainer {
  

  explainPredicate(predicate: string): string {
    const content = predicate.substring(1, predicate.length - 1).trim();

    if (!content) {
      return 'Üres feltétel';
    }

    if (/^\d+$/.test(content)) {
      return `${content}. pozícióban`;
    }

    if (content === 'last()') {
      return 'Utolsó elem';
    }

    if (content.includes('position()')) {
      return this.explainPositionPredicate(content);
    }

    if (content.includes(' and ') || content.includes(' or ')) {
      return this.explainComplexPredicate(content);
    }

    if (content.startsWith('@')) {
      return this.explainAttributePredicate(content);
    }

    if (content.includes('contains(')) {
      const containsMatch = content.match(/contains\(\s*@?([\w-]+)\s*,\s*['"]([^'"]+)['"]\s*\)/);
      if (containsMatch) {
        return `${containsMatch[1]} tartalmazza: '${containsMatch[2]}'`;
      }
      return 'Tartalmaz szűrés';
    }

    if (content.includes('starts-with(')) {
      const startsMatch = content.match(/starts-with\(\s*@?([\w-]+)\s*,\s*['"]([^'"]+)['"]\s*\)/);
      if (startsMatch) {
        return `${startsMatch[1]} kezdődik: '${startsMatch[2]}'`;
      }
      return 'Kezdődik szűrés';
    }

    if (content.includes('not(')) {
      return 'Tagadás szerinti szűrés';
    }

    return 'Érvénytelen vagy felismerhetetlen feltétel';
  }

  private explainAttributePredicate(content: string): string {
    const attrNameMatch = content.match(/@([\w-]+)/);
    if (!attrNameMatch) {
      return 'Érvénytelen attribútum hivatkozás';
    }

    const attrName = attrNameMatch[1];
    const afterAttrName = content.substring(attrNameMatch[0].length).trim();

    if (!afterAttrName) {
      return `Van ${attrName} attribútum`;
    }

    const comparisonMatch = afterAttrName.match(/^(=|!=|<|>|<=|>=)\s*/);
    if (!comparisonMatch) {
      const invalidOpMatch = afterAttrName.match(/^([+\-*\/]+|[=!<>]{2,}|[^=!<>\s]+)/);
      if (invalidOpMatch) {
        return `Érvénytelen operátor: '@${attrName} ${invalidOpMatch[1]}'`;
      }
      return `Érvénytelen feltétel az attribútumhoz: ${attrName}`;
    }

    const operator = comparisonMatch[1];
    const afterOperator = afterAttrName.substring(comparisonMatch[0].length).trim();

    if (operator === '=') {
      const valueMatch = afterOperator.match(/^['"]([^'"]+)['"]$/);
      if (valueMatch) {
        return `${attrName} attribútum értéke '${valueMatch[1]}'`;
      }
      const numMatch = afterOperator.match(/^\d+$/);
      if (numMatch) {
        return `${attrName} attribútum értéke ${numMatch[0]}`;
      }
      return `${attrName} értéke: érvénytelen formátum`;
    }

    if (operator === '!=') {
      const valueMatch = afterOperator.match(/^['"]([^'"]+)['"]$/);
      if (valueMatch) {
        return `${attrName} nem egyenlő '${valueMatch[1]}'`;
      }
      return `${attrName} nem egyenlő: érvénytelen formátum`;
    }

    if (/^[<>]=?$/.test(operator)) {
      const numMatch = afterOperator.match(/^\d+$/);
      if (!numMatch) {
        return `${attrName} összehasonlítás: érvénytelen szám`;
      }

      const value = numMatch[0];
      switch (operator) {
        case '>': return `${attrName} nagyobb mint ${value}`;
        case '>=': return `${attrName} nagyobb vagy egyenlő ${value}`;
        case '<': return `${attrName} kisebb mint ${value}`;
        case '<=': return `${attrName} kisebb vagy egyenlő ${value}`;
      }
    }

    return `Attribútum összehasonlítás`;
  }


  private explainPositionPredicate(content: string): string {
    const lessThanMatch = content.match(/position\(\)\s*<\s*(\d+)/);
    if (lessThanMatch) {
      return `Pozíció kisebb mint ${lessThanMatch[1]}`;
    }

    const lessEqualMatch = content.match(/position\(\)\s*<=\s*(\d+)/);
    if (lessEqualMatch) {
      return `Pozíció kisebb vagy egyenlő ${lessEqualMatch[1]}`;
    }

    const greaterThanMatch = content.match(/position\(\)\s*>\s*(\d+)/);
    if (greaterThanMatch) {
      return `Pozíció nagyobb mint ${greaterThanMatch[1]}`;
    }

    const greaterEqualMatch = content.match(/position\(\)\s*>=\s*(\d+)/);
    if (greaterEqualMatch) {
      return `Pozíció nagyobb vagy egyenlő ${greaterEqualMatch[1]}`;
    }

    const equalMatch = content.match(/position\(\)\s*=\s*(\d+)/);
    if (equalMatch) {
      return `Pozíció egyenlő ${equalMatch[1]}`;
    }

    return 'Pozíció szerinti szűrés';
  }

  private explainComplexPredicate(content: string): string {
    const parts: string[] = [];
    const operators: string[] = [];

    let current = '';
    let inQuotes = false;
    let quoteChar = '';
    
    for (let i = 0; i < content.length; i++) {
      const char = content[i];
      
      if ((char === '"' || char === "'") && (i === 0 || content[i-1] !== '\\')) {
        if (!inQuotes) {
          inQuotes = true;
          quoteChar = char;
        } else if (char === quoteChar) {
          inQuotes = false;
        }
      }
      
      if (!inQuotes) {
        if (content.substring(i, i + 5) === ' and ') {
          parts.push(current.trim());
          operators.push('and');
          current = '';
          i += 4; 
          continue;
        } else if (content.substring(i, i + 4) === ' or ') {
          parts.push(current.trim());
          operators.push('or');
          current = '';
          i += 3; 
          continue;
        }
      }
      
      current += char;
    }
    
    if (current.trim()) {
      parts.push(current.trim());
    }

    const explanations = parts.map(part => this.explainSimpleCondition(part));
    
    let result = explanations[0] || '';
    for (let i = 0; i < operators.length; i++) {
      const connector = operators[i] === 'and' ? 'és' : 'vagy';
      result += ` ${connector} ${explanations[i + 1]}`;
    }
    
    return result;
  }

  private explainSimpleCondition(condition: string): string {
    condition = condition.trim();

    if (condition.startsWith('@')) {
      return this.explainAttributePredicate(condition);
    }

    if (condition.includes('position()')) {
      return this.explainPositionPredicate(condition);
    }

    if (condition.includes('contains(')) {
      const containsMatch = condition.match(/contains\(\s*@?([\w-]+)\s*,\s*['"]([^'"]+)['"]\s*\)/);
      if (containsMatch) {
        return `${containsMatch[1]} tartalmazza '${containsMatch[2]}'`;
      }
    }

    return condition;
  }
}