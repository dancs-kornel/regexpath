import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RegexExampleRequest {
  pattern: string;
  caseInsensitive: boolean;
  multiline: boolean;
}

export interface RegexExample {
  text: string;
  reason?: string; // Only for negative examples
}

export interface RegexExampleResponse {
  positiveExamples: RegexExample[];
  negativeExamples: RegexExample[];
  errorMessage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RegexExampleService {
  private baseUrl = `${environment.apiUrl}/sandbox`;

  constructor(private http: HttpClient) {}

  generateExamples(pattern: string, caseInsensitive: boolean, multiline: boolean): Observable<RegexExampleResponse> {
    const request: RegexExampleRequest = {
      pattern,
      caseInsensitive,
      multiline
    };

    return this.http.post<RegexExampleResponse>(`${this.baseUrl}/regex/examples`, request);
  }
}