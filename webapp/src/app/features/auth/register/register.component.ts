import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { UserRole } from "../../../core/models/auth.models";
import { AuthService } from "../../../core/services/auth.service";
import { Router, RouterLink } from "@angular/router";

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './register.component.html',
    styleUrl: './register.component.css'
})
export class RegisterComponent {
    registerForm: FormGroup;
    errorMessage = '';
    isLoading = false;
    roles = Object.values(UserRole).filter(role => role !== UserRole.GUEST);

    constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
        this.registerForm = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            role: [UserRole.STUDENT, Validators.required]
        });
    }

    onSubmit(): void {
        if (this.registerForm.invalid) return;
        this.isLoading = true;
        this.errorMessage = '';

        this.authService.register(this.registerForm.value).subscribe({
            next: () => {
                this.router.navigate(['/']);
            },
            error: (error) => {
                this.isLoading = false;
                if (error.error?.details) this.errorMessage = error.error.details.join(', ');
                else this.errorMessage = error.error?.message || 'Registration failed';
            }
        });
    }
}