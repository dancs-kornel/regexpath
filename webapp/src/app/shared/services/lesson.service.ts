import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class LessonService {
    private baseUrl = environment.apiUrl;
    constructor(private http: HttpClient) { }

    getAllModules(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/modules`);
    }

    getModuleById(moduleId: string): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/modules/${moduleId}`);
    }

    getLessonById(lessonId: string): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/lessons/${lessonId}`);
    }

    validateExercise(lessonId: string, exerciseId: string, exerciseType: string, userInput: string[] | string): Observable<any> {
        let requestBody: any;
        if (exerciseType === 'regex_sandbox') {
            requestBody = {
                type: 'regex_sandbox',
                pattern: userInput as string
            };
        } else if (exerciseType === 'xpath_sandbox') {
            requestBody = {
                type: 'xpath_sandbox',
                expression: userInput as string
            }
        }else {
            requestBody = {
                type: 'choice',
                selectedOptions: userInput as string[]
            };
        }

        return this.http.post<any>(`${this.baseUrl}/exercises/${lessonId}/${exerciseId}/validate`, requestBody);
    }

    getSolution(lessonId: string, exerciseId: string): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/exercises/${lessonId}/${exerciseId}/solution`);
    }

}
