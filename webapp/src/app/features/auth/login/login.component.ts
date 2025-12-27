import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { AuthService } from "../../../core/services/auth.service";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    loginForm: FormGroup;
    errorMessage = '';
    isLoading = false;

    constructor(private fb: FormBuilder, private authService: AuthService, private router: Router, private route: ActivatedRoute) {
        this.loginForm = this.fb.group({
            emailOrUsername: ['', Validators.required], 
            password: ['', Validators.required]
        });
    }

    onSubmit(): void {
        if (this.loginForm.invalid) return;
        this.isLoading = true;
        this.errorMessage = '';
        this.authService.login(this.loginForm.value).subscribe({
            next: () => {
                const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
                this.router.navigate([returnUrl]);
            },
            error: (error) => {
                this.isLoading = false;
                this.errorMessage = error.error?.message || 'Login failed';
            }
        });
    }
}