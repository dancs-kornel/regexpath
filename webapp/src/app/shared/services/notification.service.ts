import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Notification {
    id: string;
    type: 'success' | 'error' | 'info' | 'warning';
    message: string;
    duration?: number; 
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
    private notificationsSubject = new BehaviorSubject<Notification[]>([]);
    public notifications$: Observable<Notification[]> = this.notificationsSubject.asObservable();

    showSuccess(message: string, duration: number = 3000): void {
        this.show({ type: 'success', message, duration });
    }

    showError(message: string, duration: number = 5000): void {
        this.show({ type: 'error', message, duration });
    }

    showInfo(message: string, duration: number = 3000): void {
        this.show({ type: 'info', message, duration });
    }

    showWarning(message: string, duration: number = 4000): void {
        this.show({ type: 'warning', message, duration });
    }

    private show(notification: Omit<Notification, 'id'>): void {
        const id = this.generateId();
        const newNotification: Notification = { id, ...notification };
        
        const current = this.notificationsSubject.value;
        this.notificationsSubject.next([...current, newNotification]);

        if (notification.duration && notification.duration > 0) {
            setTimeout(() => {
                this.remove(id);
            }, notification.duration);
        }
    }

    remove(id: string): void {
        const current = this.notificationsSubject.value;
        this.notificationsSubject.next(current.filter(n => n.id !== id));
    }

    clear(): void {
        this.notificationsSubject.next([]);
    }

    private generateId(): string {
        return `notification-${Date.now()}-${Math.random().toString(36).slice(2, 11)}`;
    }
}