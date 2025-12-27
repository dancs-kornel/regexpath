import { Component, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/auth.models';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements AfterViewInit, OnDestroy {
  currentUser: User | null = null;
  private subscription?: Subscription;

  constructor(
    private router: Router,
    private elementRef: ElementRef,
    public authService: AuthService
  ) {
    this.subscription = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  ngAfterViewInit() {
    const cardsContainer = this.elementRef.nativeElement.querySelector('#cards');
    if (cardsContainer) {
      cardsContainer.addEventListener('mousemove', (e: MouseEvent) => {
        const cards = this.elementRef.nativeElement.querySelectorAll('.card');
        cards.forEach((card: HTMLElement) => {
          const rect = card.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const y = e.clientY - rect.top;
          card.style.setProperty('--mouse-x', `${x}px`);
          card.style.setProperty('--mouse-y', `${y}px`);
        });
      });
    }
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}