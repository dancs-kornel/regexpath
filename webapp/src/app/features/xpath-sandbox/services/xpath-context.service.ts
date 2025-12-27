import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { XPathResult } from '../models/xpath-types';

@Injectable()
export class XPathContextService {
    private hoveredResultSubject = new BehaviorSubject<XPathResult | null>(null);
    private selectedResultSubject = new BehaviorSubject<XPathResult | null>(null);

    hoveredResult$: Observable<XPathResult | null> = this.hoveredResultSubject.asObservable();
    selectedResult$: Observable<XPathResult | null> = this.selectedResultSubject.asObservable();

    setHoveredResult(result: XPathResult | null): void {
        this.hoveredResultSubject.next(result);
    }

    setSelectedResult(result: XPathResult | null): void {
        this.selectedResultSubject.next(result);
    }
}