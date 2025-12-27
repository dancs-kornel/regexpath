export interface MatchInfo {
    text: string;
    start: number;
    end: number;
}

export function findRegexMatches(text: string, pattern: string, flags: string = 'g'): MatchInfo[] {
    const matches: MatchInfo[] = [];
    if (!pattern || !text) return matches;
    if (pattern === '' || pattern === '(?:)') {
        console.warn('Empty pattern detected, no matches returned');
        return matches;
    }
    try {
        const regex = new RegExp(pattern, flags.includes('g') ? flags : flags + 'g');
        let iterationCount = 0;
        const maxIterations = 10000;

        for (const match of text.matchAll(regex)) {
            if (++iterationCount > maxIterations) {
                console.warn('Pattern match limit exceeded, stopping to prevent infinite loop');
                break;
            }
            if (match[0].length < 1) continue;
            const start = match.index ?? 0;
            matches.push({
                text: match[0],
                start,
                end: start + match[0].length
            });
        }
    } catch (error) {
        console.warn('Invalid regex pattern:', pattern, error);
    }
    return matches;
}