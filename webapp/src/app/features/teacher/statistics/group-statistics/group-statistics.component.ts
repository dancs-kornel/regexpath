
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { StatisticsService } from '../../../../core/services/statistics.service';
import { GroupStatistics } from '../../../../core/models/statistics.models';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
    selector: 'app-group-statistics',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './group-statistics.component.html',
    styleUrl: './group-statistics.component.css'
})
export class GroupStatisticsComponent implements OnInit {
    statistics: GroupStatistics | null = null;
    groupId: number = 0;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private statisticsService: StatisticsService,
        private notificationService: NotificationService
    ) {}

    ngOnInit(): void {
        this.groupId = Number(this.route.snapshot.paramMap.get('id'));
        if (this.groupId) {
            this.loadStatistics();
        }
    }

    loadStatistics(): void {
        this.isLoading = true;
        this.error = '';

        this.statisticsService.getGroupStatistics(this.groupId).subscribe({
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

    viewAssignmentStatistics(assignmentId: number): void {
        this.router.navigate(['/teacher/groups', this.groupId, 'assignments', assignmentId, 'statistics']);
    }

    exportStatistics(): void {
        this.statisticsService.exportGroupStatistics(this.groupId).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `group-${this.groupId}-statistics.csv`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.notificationService.showSuccess('Statistics exported successfully');
            },
            error: (err) => {
                console.error('Error exporting statistics:', err);
                this.notificationService.showError('Failed to export statistics');
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/groups', this.groupId]);
    }

    getCompletionColor(rate: number): string {
        if (rate >= 80) return '#2e7d32';
        if (rate >= 60) return '#ef6c00';
        return '#c62828';
    }
}