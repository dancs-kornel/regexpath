import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
    ContentType,
    SandboxContent,
    SandboxContentList,
    UploadContentRequest,
    ForkContentRequest,
    UpdateContentRequest
} from '../models/sandbox-content.models';

@Injectable({
    providedIn: 'root'
})
export class SandboxContentService {
    private baseUrl = `${environment.apiUrl}/sandbox/examples`;

    constructor(private http: HttpClient) {}

    listBuiltinExamples(type: ContentType): Observable<SandboxContentList[]> {
        const params = new HttpParams().set('type', type);
        return this.http.get<SandboxContentList[]>(`${this.baseUrl}/builtin`, { params });
    }

    listUserContents(type: ContentType): Observable<SandboxContentList[]> {
        const params = new HttpParams().set('type', type);
        return this.http.get<SandboxContentList[]>(`${this.baseUrl}/user`, { params });
    }

    getContentById(id: number): Observable<SandboxContent> {
        return this.http.get<SandboxContent>(`${this.baseUrl}/${id}`);
    }

    uploadContent(file: File, request: UploadContentRequest): Observable<SandboxContent> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('type', request.type);
        formData.append('name', request.name);
        if (request.description) {
            formData.append('description', request.description);
        }
        if (request.category) {
            formData.append('category', request.category);
        }

        return this.http.post<SandboxContent>(`${this.baseUrl}/upload`, formData);
    }

    forkContent(id: number, request: ForkContentRequest): Observable<SandboxContent> {
        return this.http.post<SandboxContent>(`${this.baseUrl}/${id}/fork`, request);
    }

    updateContent(id: number, request: UpdateContentRequest): Observable<SandboxContent> {
        return this.http.put<SandboxContent>(`${this.baseUrl}/${id}`, request);
    }

    deleteContent(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}