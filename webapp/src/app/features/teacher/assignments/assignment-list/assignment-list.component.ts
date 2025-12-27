
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AssignmentService } from '../../../../core/services/assignment.service';
import { AssignmentSummary, AssignmentStatus } from '../../../../core/models/assignment.models';
import { NotificationService } from '../../../../shared/services/notification.service';
import { AssignAssignmentModalComponent } from '../assign-assignment-modal/assign-assignment-modal.component';
import { Router } from '@angular/router';

@Component({
    selector: 'app-assignment-list',
    standalone: true,
    imports: [CommonModule, RouterLink, FormsModule, AssignAssignmentModalComponent],
    templateUrl: './assignment-list.component.html',
    styleUrl: './assignment-list.component.css'
})
export class AssignmentListComponent implements OnInit {
    assignments: AssignmentSummary[] = [];
    filteredAssignments: AssignmentSummary[] = [];
    isLoading = true;
    error = '';

    statusFilter: 'all' | AssignmentStatus = 'all';
    searchTerm = '';
    sortBy: 'title' | 'createdAt' | 'dueDate' | 'points' = 'createdAt';
    sortOrder: 'asc' | 'desc' = 'desc';

    deleteConfirmation = {
        show: false,
        assignmentId: 0,
        assignmentTitle: ''
    };

    assignModal = {
        show: false,
        assignmentId: 0,
        assignmentTitle: ''
    };

    constructor(
        private assignmentService: AssignmentService,
        private notificationService: NotificationService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadAssignments();
    }

    loadAssignments(): void {
        this.isLoading = true;
        this.error = '';

        this.assignmentService.getMyAssignments().subscribe({
            next: (assignments) => {
                this.assignments = assignments;
                this.applyFilters();
                this.isLoading = false;
            },
            error: (error) => {
                this.error = 'Failed to load assignments';
                this.isLoading = false;
                console.error('Error loading assignments:', error);
            }
        });
    }

    applyFilters(): void {
        let filtered = [...this.assignments];

        if (this.statusFilter !== 'all') {
            filtered = filtered.filter(a => a.status === this.statusFilter);
        }

        if (this.searchTerm) {
            const term = this.searchTerm.toLowerCase();
            filtered = filtered.filter(a =>
                a.title.toLowerCase().includes(term)
            );
        }

        filtered.sort((a, b) => {
            let comparison = 0;

            switch (this.sortBy) {
                case 'title':
                    comparison = a.title.localeCompare(b.title);
                    break;
                case 'createdAt':
                    comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
                    break;
                case 'dueDate':
                    if (!a.dueDate && !b.dueDate) return 0;
                    if (!a.dueDate) return 1;
                    if (!b.dueDate) return -1;
                    comparison = new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
                    break;
                case 'points':
                    comparison = a.totalPoints - b.totalPoints;
                    break;
            }

            return this.sortOrder === 'asc' ? comparison : -comparison;
        });

        this.filteredAssignments = filtered;
    }

    onFilterChange(): void {
        this.applyFilters();
    }

    toggleSort(field: typeof this.sortBy): void {
        if (this.sortBy === field) {
            this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortBy = field;
            this.sortOrder = 'asc';
        }
        this.applyFilters();
    }

    openAssignModal(assignment: AssignmentSummary): void {
        this.assignModal = {
            show: true,
            assignmentId: assignment.id,
            assignmentTitle: assignment.title
        };
    }

    closeAssignModal(): void {
        this.assignModal = {
            show: false,
            assignmentId: 0,
            assignmentTitle: ''
        };
    }

    onAssignmentAssigned(): void {
        this.loadAssignments(); 
    }

    confirmDelete(assignment: AssignmentSummary): void {
        this.deleteConfirmation = {
            show: true,
            assignmentId: assignment.id,
            assignmentTitle: assignment.title
        };
    }

    cancelDelete(): void {
        this.deleteConfirmation = {
            show: false,
            assignmentId: 0,
            assignmentTitle: ''
        };
    }

    deleteAssignment(): void {
        if (!this.deleteConfirmation.assignmentId) return;

        this.assignmentService.deleteAssignment(this.deleteConfirmation.assignmentId).subscribe({
            next: () => {
                this.notificationService.showSuccess('Assignment deleted successfully');
                this.loadAssignments();
                this.cancelDelete();
            },
            error: (error) => {
                console.error('Error deleting assignment:', error);
                this.notificationService.showError('Failed to delete assignment');
                this.cancelDelete();
            }
        });
    }

    publishAssignment(id: number, event: Event): void {
        event.stopPropagation();

        this.assignmentService.publishAssignment(id).subscribe({
            next: () => {
                this.notificationService.showSuccess('Assignment published successfully');
                this.loadAssignments();
            },
            error: (error) => {
                console.error('Error publishing assignment:', error);
                this.notificationService.showError('Failed to publish assignment');
            }
        });
    }

    revertToDraft(id: number, event: Event): void {
        event.stopPropagation();

        this.assignmentService.revertToDraft(id).subscribe({
            next: () => {
                this.notificationService.showSuccess('Assignment reverted to draft');
                this.loadAssignments();
            },
            error: (error) => {
                console.error('Error reverting assignment:', error);
                this.notificationService.showError('Failed to revert assignment');
            }
        });
    }

    getStatusBadgeClass(status: AssignmentStatus | string): string {
        if (status === AssignmentStatus.SUBMITTED || status === 'SUBMITTED') {
            return 'published';
        }
        return 'draft';
    }

    previewAssignment(assignmentId: number): void {
        this.router.navigate(['/teacher/assignments', assignmentId, 'preview']);
    }
}