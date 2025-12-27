import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of, firstValueFrom } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { RegexExplanations, RegexParser, ExplainedNode } from '../utils/regex-explainer';

@Injectable({
  providedIn: 'root'
})
export class RegexExplanationService {
  private explanations$ = new BehaviorSubject<RegexExplanations | null>(null);
  private readonly explanationsPath = 'assets/regex-explanations.json';
  private loadingPromise: Promise<void> | null = null;

  constructor(private http: HttpClient) {
    this.loadExplanations();
  }

  private loadExplanations(): void {
    if (this.loadingPromise) {
      return;
    }

    this.loadingPromise = firstValueFrom(
      this.http.get<RegexExplanations>(this.explanationsPath).pipe(
        tap(explanations => {
          console.log('Regex explanations loaded successfully');
        }),
        catchError(error => {
          console.error('Failed to load regex explanations:', error);
          return of(this.getFallbackExplanations());
        })
      )
    ).then(explanations => {
      this.explanations$.next(explanations || this.getFallbackExplanations());
    });
  }

  public explainPattern(pattern: string): Observable<ExplainedNode[]> {
    return this.explanations$.pipe(
      map(explanations => this.parsePattern(pattern, explanations))
    );
  }

  public explainPatternSync(pattern: string): ExplainedNode[] {
    const explanations = this.explanations$.value;
    return this.parsePattern(pattern, explanations);
  }

  public isReady(): Observable<boolean> {
    return this.explanations$.pipe(
      map(explanations => explanations !== null)
    );
  }

  public async waitForReady(): Promise<boolean> {
    try {
      const ready = await firstValueFrom(
        this.explanations$.pipe(
          map(explanations => explanations !== null),
          tap(ready => {
            if (!ready && !this.loadingPromise) {
              this.loadExplanations();
            }
          })
        )
      );

      return ready ?? false;
    } catch (error) {
      console.error('Error waiting for explanations to be ready:', error);
      return false;
    }
  }

  private parsePattern(pattern: string, explanations: RegexExplanations | null): ExplainedNode[] {
    if (!explanations || !pattern) {
      return [];
    }

    try {
      const parser = new RegexParser(pattern, explanations);
      return parser.parse();
    } catch (error) {
      console.error('Error parsing regex pattern:', error);
      return [{
        token: pattern,
        explanation: 'Hibás reguláris kifejezés'
      }];
    }
  }

  private getFallbackExplanations(): RegexExplanations {
    return {
      escapes: {
        '\\d': 'Számjegy (0-9)',
        '\\w': 'Betű, szám vagy aláhúzás',
        '\\s': 'Szóköz karakter',
        '\\.': 'Pont karakter',
      },
      quantifiers: {
        '+': 'Előző elem 1 vagy több alkalommal',
        '*': 'Előző elem 0 vagy több alkalommal',
        '?': 'Előző elem opcionális',
        '{': 'Ismétlési kvantor kezdete',
        '}': 'Ismétlési kvantor vége'
      },
      groups: {
        '[]': 'Karakterosztály',
        '()': 'Csoportosítás'
      },
      anchors: {
        '^': 'Sor kezdete',
        '$': 'Sor vége'
      },
      special: {
        '.': 'Bármilyen karakter',
        '|': 'Vagy operátor'
      },
      ranges: {
        'a-z': 'Kisbetűk a-tól z-ig',
        'A-Z': 'Nagybetűk A-tól Z-ig',
        '0-9': 'Számjegyek 0-tól 9-ig'
      },
      numbers: {
        'generic': 'Szám'
      },
      characters: {
        'generic': 'Sima karakter'
      }
    };
  }
}