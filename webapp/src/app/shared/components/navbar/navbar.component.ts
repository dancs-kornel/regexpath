import { CommonModule } from "@angular/common";
import { Component, OnDestroy } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Subscription } from "rxjs";
import { User } from "../../../core/models/auth.models";
import { AuthService } from "../../../core/services/auth.service";

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './navbar.component.html',
    styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnDestroy {
    currentUser: User | null = null;
    private subscription: Subscription;

    constructor(public authService: AuthService) {
        this.subscription = this.authService.currentUser$.subscribe(user => { this.currentUser = user; });
    }

    logout(): void { this.authService.logout(); }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}