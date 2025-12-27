import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GroupService } from '../../../core/services/group.service';
import { Group } from '../../../core/models/group.models';

@Component({
    selector: 'app-create-group',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './create-group.component.html',
    styleUrls: ['./create-group.component.css', '../../../../styles.css']
})
export class CreateGroupComponent {
    createGroupForm: FormGroup;
    errorMessage = '';
    isLoading = false;
    createdGroup: Group | null = null;

    constructor(
        private fb: FormBuilder,
        private groupService: GroupService,
        private router: Router
    ) {
        this.createGroupForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
            description: ['', [Validators.maxLength(500)]]
        });
    }

    onSubmit(): void {
        if (this.createGroupForm.invalid) return;

        this.isLoading = true;
        this.errorMessage = '';

        this.groupService.createGroup(this.createGroupForm.value).subscribe({
            next: (group) => {
                this.createdGroup = group;
                this.isLoading = false;
            },
            error: (error) => {
                this.errorMessage = error.error?.message || 'Failed to create group';
                this.isLoading = false;
            }
        });
    }

    goToGroups(): void {
        this.router.navigate(['/groups']);
    }

    createAnother(): void {
        this.createdGroup = null;
        this.createGroupForm.reset();
    }

    cancel(): void {
        this.router.navigate(['/groups']);
    }
}