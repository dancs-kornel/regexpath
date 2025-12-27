
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { StatisticsService } from '../../../../core/services/statistics.service';
import { AssignmentStatistics } from '../../../../core/models/statistics.models';

@Component({
    selector: 'app-assignment-statistics',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './assignment-statistics.component.html',
    styleUrl: './assignment-statistics.component.css'
})
export class AssignmentStatisticsComponent implements OnInit {
    statistics: AssignmentStatistics | null = null;
    groupId: number = 0;
    assignmentId: number = 0;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private statisticsService: StatisticsService
    ) {}

    ngOnInit(): void {
        this.groupId = Number(this.route.snapshot.paramMap.get('groupId'));
        this.assignmentId = Number(this.route.snapshot.paramMap.get('assignmentId'));
        
        if (this.groupId && this.assignmentId) {
            this.loadStatistics();
        }
    }

    loadStatistics(): void {
        this.isLoading = true;
        this.error = '';

        this.statisticsService.getAssignmentStatistics(this.groupId, this.assignmentId).subscribe({
            next: (stats) => {
                this.statistics = stats;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = err.error?.message || 'Failed to load statistics';
                this.isLoading = false;
                console.error('Error loading statistics:', err);
            }
        });
    }

    viewStudentProgress(studentId: number): void {
        this.router.navigate(['/teacher/groups', this.groupId, 'students', studentId, 'progress']);
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