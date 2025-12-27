
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { StatisticsService } from '../../../../core/services/statistics.service';
import { TeacherAttemptView } from '../../../../core/models/statistics.models';

@Component({
    selector: 'app-teacher-attempt-view',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './teacher-attempt-view.component.html',
    styleUrl: './teacher-attempt-view.component.css'
})
export class TeacherAttemptViewComponent implements OnInit {
    attemptView: TeacherAttemptView | null = null;
    attemptId: number = 0;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private statisticsService: StatisticsService
    ) {}

    ngOnInit(): void {
        this.attemptId = Number(this.route.snapshot.paramMap.get('attemptId'));
        
        if (this.attemptId) {
            this.loadAttempt();
        }
    }

    loadAttempt(): void {
        this.isLoading = true;
        this.error = '';

        this.statisticsService.getTeacherAttemptView(this.attemptId).subscribe({
            next: (view) => {
                this.attemptView = view;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = err.error?.message || 'Failed to load attempt';
                this.isLoading = false;
                console.error('Error loading attempt:', err);
            }
        });
    }

    goBack(): void {
        window.history.back();
    }

    getScoreClass(): string {
        if (!this.attemptView) return '';
        const percentage = this.attemptView.percentageScore;
        if (percentage >= 90) return 'excellent';
        if (percentage >= 70) return 'good';
        if (percentage >= 50) return 'average';
        return 'poor';
    }

    parseJson(json: string | null): any {
        if (!json) return {};
        try {
            return JSON.parse(json);
        } catch {
            return {};
        }
    }

    getOptionText(configJson: string | null, optionKey: any): string {
        const config = this.parseJson(configJson);
        const options = config?.options || [];
        
        if (typeof optionKey === 'number' && options[optionKey]) {
            return typeof options[optionKey] === 'string' 
                ? options[optionKey] 
                : options[optionKey].text;
        }
        
        const found = options.find((o: any) => o.id === optionKey);
        return found?.text || String(optionKey);
    }

    getUserAnswer(answerJson: string, exerciseType: string): string {
        const obj = this.parseJson(answerJson);
        
        switch (exerciseType) {
            case 'REGEX_SANDBOX':
                return obj.pattern || '';
            case 'XPATH_SANDBOX':
                return obj.expression || '';
            default:
                return obj.userAnswer || obj.value || obj.answer || '';
        }
    }

    getCorrectAnswer(configJson: string | null, exerciseType: string): string {
        const config = this.parseJson(configJson);
        
        switch (exerciseType) {
            case 'REGEX_SANDBOX':
                return config.solution || '';
            case 'XPATH_SANDBOX':
                return config.solution || '';
            case 'RADIO':
            case 'MULTIPLE_CHOICE':
                const correctOptions = (config.options || [])
                    .filter((o: any) => o.correct)
                    .map((o: any) => o.text)
                    .join(', ');
                return correctOptions || 'Not specified';
            default:
                return 'N/A';
        }
    }
}