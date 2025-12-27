import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GroupService } from '../../../core/services/group.service';
import { Group } from '../../../core/models/group.models';

@Component({
    selector: 'app-join-group',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './join-group.component.html',
    styleUrls: ['./join-group.component.css', '../../../../styles.css']
})
export class JoinGroupComponent {
    joinGroupForm: FormGroup;
    errorMessage = '';
    isLoading = false;
    joinedGroup: Group | null = null;

    constructor(
        private fb: FormBuilder,
        private groupService: GroupService,
        private router: Router
    ) {
        this.joinGroupForm = this.fb.group({
            inviteCode: ['', [Validators.required, Validators.pattern(/^[A-Z0-9]{6}$/)]]
        });
    }

    onSubmit(): void {
        if (this.joinGroupForm.invalid) return;

        this.isLoading = true;
        this.errorMessage = '';

        const inviteCode = this.joinGroupForm.value.inviteCode.toUpperCase();

        this.groupService.joinGroup({ inviteCode }).subscribe({
            next: (group) => {
                this.joinedGroup = group;
                this.isLoading = false;
            },
            error: (error) => {
                this.errorMessage = error.error?.message || 'Failed to join group';
                this.isLoading = false;
            }
        });
    }

    goToGroups(): void {
        this.router.navigate(['/groups']);
    }

    joinAnother(): void {
        this.joinedGroup = null;
        this.joinGroupForm.reset();
    }

    cancel(): void {
        this.router.navigate(['/groups']);
    }

    formatInviteCode(event: any): void {
        const input = event.target;
        input.value = input.value.toUpperCase();
        this.joinGroupForm.patchValue({ inviteCode: input.value });
    }
}