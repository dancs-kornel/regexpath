import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, map } from "rxjs";
import { Sample, SampleManifest } from "../models/xpath-types";

@Injectable({
    providedIn: 'root'
})
export class SamplesService {
    private readonly basePath = '/assets/xpath-samples';
    constructor(private http: HttpClient) {}
    
    getSamples(): Observable<Sample[]> {
        return this.http.get<SampleManifest>(`${this.basePath}/manifest.json`).pipe(map(manifest => manifest.samples));
    }

    getSampleContent(sample: Sample): Observable<string> {
        return this.http.get(`${this.basePath}/${sample.file}`, {responseType: 'text'});
    }
}