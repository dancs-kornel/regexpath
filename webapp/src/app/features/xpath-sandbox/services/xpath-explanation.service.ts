
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of, firstValueFrom } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { XPathExplanations, XPathExplanationNode } from '../models/xpath-explanations.models';
import { XPathExplainer } from '../utils/xpath-explainer/xpath-explainer';

@Injectable({
  providedIn: 'root'
})
export class XPathExplanationService {
  private explanations$ = new BehaviorSubject<XPathExplanations | null>(null);
  private readonly explanationsPath = 'assets/xpath-explanations.json';
  private loadingPromise: Promise<void> | null = null;

  constructor(private http: HttpClient) {
    this.loadExplanations();
  }

  private loadExplanations(): void {
    if (this.loadingPromise) {
      return;
    }

    this.loadingPromise = firstValueFrom(
      this.http.get<XPathExplanations>(this.explanationsPath)
        .pipe(
          catchError(error => {
            console.error('Failed to load XPath explanations:', error);
            return of(this.getFallbackExplanations());
          })
        )
    ).then(explanations => {
      this.explanations$.next(explanations);
    });
  }

  public explainExpression(expression: string): Observable<XPathExplanationNode[]> {
    return this.explanations$.pipe(
      map(explanations => this.parseExpression(expression, explanations))
    );
  }

  public explainExpressionSync(expression: string): XPathExplanationNode[] {
    const explanations = this.explanations$.value;
    return this.parseExpression(expression, explanations);
  }



  public async waitForReady(): Promise<boolean> {
    if (this.explanations$.value !== null) {
      return true;
    }

    if (this.loadingPromise) {
      await this.loadingPromise;
      return this.explanations$.value !== null;
    }

    this.loadExplanations();
    if (this.loadingPromise) {
      await this.loadingPromise;
    }
    
    return this.explanations$.value !== null;
  }

  private parseExpression(expression: string, explanations: XPathExplanations | null): XPathExplanationNode[] {
    if (!explanations || !expression) {
      return [];
    }

    try {
      const explainer = new XPathExplainer(expression, explanations);
      return explainer.parse();
    } catch (error) {
      console.error('Error parsing XPath expression:', error);
      return [{
        token: expression,
        explanation: 'Hibás XPath kifejezés',
        category: 'combined'
      }];
    }
  }


  private getFallbackExplanations(): XPathExplanations {
    return {
      axes: {
        '/': 'Gyermek elem',
        '//': 'Leszármazott elem (bármilyen mélységben)',
        '@': 'Attribútum',
        '..': 'Szülő elem',
        '.': 'Aktuális elem',
        'child::': 'Gyermek tengely',
        'descendant::': 'Leszármazott tengely',
        'parent::': 'Szülő tengely',
        'ancestor::': 'Ős tengely',
        'following-sibling::': 'Következő testvér tengely',
        'preceding-sibling::': 'Előző testvér tengely',
        'attribute::': 'Attribútum tengely'
      },
      nodeTests: {
        '*': 'Bármilyen elem',
        'text()': 'Szöveges csomópont',
        'comment()': 'Megjegyzés csomópont',
        'node()': 'Bármilyen csomópont'
      },
      operators: {
        '=': 'Egyenlő',
        '!=': 'Nem egyenlő',
        '<': 'Kisebb mint',
        '>': 'Nagyobb mint',
        '<=': 'Kisebb vagy egyenlő',
        '>=': 'Nagyobb vagy egyenlő',
        'and': 'És logikai operátor',
        'or': 'Vagy logikai operátor',
        '|': 'Unió'
      },
      functions: {
        'position()': 'Elem pozíciója',
        'last()': 'Utolsó elem pozíciója',
        'count()': 'Elemek száma',
        'contains()': 'Tartalmaz szövegrészt',
        'starts-with()': 'Kezdődik szövegrésszel',
        'string-length()': 'Szöveg hossza',
        'not()': 'Logikai tagadás'
      },
      predicates: {
        position: 'Pozíció szerinti szűrés',
        attribute: 'Attribútum szerinti szűrés',
        function: 'Függvény szerinti szűrés',
        comparison: 'Összehasonlítás szerinti szűrés'
      },
      common: {
        '/*': 'Gyökér elem gyermekei',
        '//*': 'Minden elem a dokumentumban',
        '//text()': 'Minden szöveges csomópont',
        '@*': 'Minden attribútum'
      }
    };
  }
}