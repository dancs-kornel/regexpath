
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Group } from '../../../../core/models/group.models';
import { GroupService } from '../../../../core/services/group.service';
import { AssignmentService } from '../../../../core/services/assignment.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { AssignmentSummary } from '../../../../core/models/assignment.models';

@Component({
    selector: 'app-assign-assignment-modal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './assign-assignment-modal.component.html',
    styleUrl: './assign-assignment-modal.component.css'
})
export class AssignAssignmentModalComponent implements OnInit {
    @Input() assignmentId: number = 0;
    @Input() assignmentTitle: string = '';
    @Input() preselectedGroupId?: number; 
    @Input() selectAssignmentMode: boolean = false;
    @Output() closeModal = new EventEmitter<void>();
    @Output() assignmentAssigned = new EventEmitter<void>();

    groups: Group[] = [];
    selectedGroupIds: number[] = [];
    isLoading = true;
    isSubmitting = false;
    error = '';

    assignments: AssignmentSummary[] = []; 
    selectedAssignmentId: number = 0;

    constructor(
        private groupService: GroupService,
        private assignmentService: AssignmentService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        if (this.selectAssignmentMode || this.assignmentId === 0) {
            this.loadAssignments();
        } else {
            this.selectedAssignmentId = this.assignmentId;
        }
        this.loadGroups();
    }

    loadAssignments(): void {
        this.assignmentService.getMyAssignments().subscribe({
            next: (assignments) => {
                this.assignments = assignments.filter(a => a.status === 'SUBMITTED');
                if (this.assignments.length > 0 && !this.assignmentId) {
                    this.selectedAssignmentId = this.assignments[0].id;
                }
            },
            error: (error) => {
                this.error = 'Failed to load assignments';
            }
        });
    }

    loadGroups(): void {
        this.isLoading = true;
        this.error = '';

        this.groupService.getUserGroups().subscribe({
            next: (groups) => {
                this.groups = groups;

                if (this.preselectedGroupId) {
                    this.selectedGroupIds = [this.preselectedGroupId];
                }

                this.isLoading = false;
            },
            error: (error) => {
                this.error = 'Failed to load groups';
                this.isLoading = false;
                console.error('Error loading groups:', error);
            }
        });
    }

    toggleGroup(groupId: number): void {
        const index = this.selectedGroupIds.indexOf(groupId);
        if (index > -1) {
            this.selectedGroupIds = this.selectedGroupIds.filter(id => id !== groupId);
        } else {
            this.selectedGroupIds = [...this.selectedGroupIds, groupId];
        }
    }

    isGroupSelected(groupId: number): boolean {
        return this.selectedGroupIds.includes(groupId);
    }

    assignToGroups(): void {
        if (this.selectedGroupIds.length === 0) {
            this.notificationService.showError('Please select at least one group');
            return;
        }

        const assignmentId = this.selectedAssignmentId || this.assignmentId;

        this.isSubmitting = true;
        this.assignmentService.assignToGroups(assignmentId, this.selectedGroupIds).subscribe({
            next: () => {
                this.notificationService.showSuccess('Assignment assigned successfully');
                this.assignmentAssigned.emit();
                this.close();
            },
            error: (error) => {
                this.error = error.error?.message || 'Failed to assign assignment';
                this.isSubmitting = false;
            }
        });
    }

    close(): void {
        this.closeModal.emit();
    }
}