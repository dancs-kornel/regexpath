
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { StatisticsService } from '../../../../core/services/statistics.service';
import { StudentProgress } from '../../../../core/models/statistics.models';

@Component({
    selector: 'app-student-progress',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './student-progress.component.html',
    styleUrl: './student-progress.component.css'
})
export class StudentProgressComponent implements OnInit {
    progress: StudentProgress | null = null;
    groupId: number = 0;
    studentId: number = 0;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private statisticsService: StatisticsService
    ) {}

    ngOnInit(): void {
        this.groupId = Number(this.route.snapshot.paramMap.get('groupId'));
        this.studentId = Number(this.route.snapshot.paramMap.get('studentId'));
        
        if (this.groupId && this.studentId) {
            this.loadProgress();
        }
    }

    loadProgress(): void {
        this.isLoading = true;
        this.error = '';

        this.statisticsService.getStudentProgress(this.groupId, this.studentId).subscribe({
            next: (progress) => {
                this.progress = progress;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = err.error?.message || 'Failed to load student progress';
                this.isLoading = false;
                console.error('Error loading progress:', err);
            }
        });
    }

    viewAttempt(attemptId: number): void {
        this.router.navigate(['/teacher/attempts', attemptId, 'view']);
    }

    goBack(): void {
        this.router.navigate(['/teacher/groups', this.groupId, 'statistics']);
    }

    getStatusClass(status: string): string {
        return status.toLowerCase().replace('_', '-');
    }

    getStatusColor(status: string): string {
        switch (status) {
            case 'COMPLETED': return '#2e7d32';
            case 'IN_PROGRESS': return '#ef6c00';
            case 'NOT_STARTED': return '#1565c0';
            default: return '#666';
        }
    }
}