import { Injectable } from '@angular/core';
import { MatchInfo, findRegexMatches } from '../../shared/utils/regex-utils';

@Injectable({ providedIn: 'root' })
export class RegexMatchService {
  isValidPattern(pattern: string): boolean {
    if (!pattern) return true;
    try {
      new RegExp(pattern);
      return true;
    } catch {
      return false;
    }
  }

  getMatches(text: string, pattern: string, flags: string = 'g'): MatchInfo[] {
    if (!this.isValidPattern(pattern)) return [];
    return findRegexMatches(text, pattern, flags);
  }
}
