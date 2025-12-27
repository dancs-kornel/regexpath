import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { Subject, takeUntil } from "rxjs";
import { LessonService } from "../../shared/services/lesson.service";
import { LessonProgressService } from "../../core/services/lesson-progress.service";
import { ModuleProgress, PrerequisiteCheckResponse } from "../../core/models/lesson-progress.models";
import { AuthService } from "../../core/services/auth.service";
import { PrerequisiteWarningModalComponent } from "../../shared/components/prerequisite-warning-modal/prerequisite-warning-modal.component";

@Component({
    selector: 'app-lessons',
    standalone: true,
    imports: [CommonModule, RouterModule, PrerequisiteWarningModalComponent],
    templateUrl: './lessons.component.html',
    styleUrl: './lessons.component.css'
})
export class LessonsComponent implements OnInit, OnDestroy {
    modules: any[] = [];
    moduleProgress: ModuleProgress[] = [];
    loading = true;
    error = '';
    isAuthenticated = false;
    
    continueLesson: any | null = null;
    loadingContinue = false;
    
    showPrerequisiteModal = false;
    prerequisiteCheckResult: PrerequisiteCheckResponse | null = null;
    pendingLessonId: string | null = null;
    pendingLessonTitle: string = '';
    
    private destroy$ = new Subject<void>();

    constructor(
        private lessonService: LessonService,
        private lessonProgressService: LessonProgressService,
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit() {
        this.isAuthenticated = this.authService.isAuthenticated();
        this.loadModules();
        
        if (this.isAuthenticated) {
            this.loadContinueLearning();
            this.lessonProgressService.progressUpdated$
                .pipe(takeUntil(this.destroy$))
                .subscribe(() => {
                    this.loadProgress();
                    this.loadContinueLearning();
                });
        }
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadModules() {
        this.lessonService.getAllModules().subscribe({
            next: (modules) => {
                this.modules = modules;
                if (this.isAuthenticated) {
                    this.loadProgress();
                } else {
                    this.loading = false;
                }
            },
            error: (err) => {
                this.error = 'Failed to load modules';
                this.loading = false;
                console.error('Error loading modules:', err);
            }
        });
    }

    loadProgress() {
        if (!this.isAuthenticated) return;
        
        this.lessonProgressService.getModuleProgress().subscribe({
            next: (progress) => {
                this.moduleProgress = progress;
                this.loading = false;
            },
            error: (err) => {
                console.error('Error loading progress:', err);
                this.loading = false;
            }
        });
    }


    onLessonClick(lessonId: string, lessonTitle: string, event: Event) {
        event.preventDefault();
        event.stopPropagation();

        this.lessonProgressService.checkPrerequisites(lessonId).subscribe({
            next: (result) => {
                if (result.unmetPrerequisites && result.unmetPrerequisites.length > 0) {
                    this.prerequisiteCheckResult = result;
                    this.pendingLessonId = lessonId;
                    this.pendingLessonTitle = lessonTitle;
                    this.showPrerequisiteModal = true;
                } else {
                    this.navigateToLesson(lessonId);
                }
            },
            error: (err) => {
                console.error('Error checking prerequisites:', err);
                this.navigateToLesson(lessonId);
            }
        });
    }

    onContinueAnyway() {
        this.showPrerequisiteModal = false;
        if (this.pendingLessonId) {
            this.navigateToLesson(this.pendingLessonId);
        }
        this.clearModalState();
    }

    onCloseModal() {
        this.showPrerequisiteModal = false;
        this.clearModalState();
    }

    private navigateToLesson(lessonId: string) {
        this.router.navigate(['/lessons', lessonId]);
    }

    private clearModalState() {
        this.prerequisiteCheckResult = null;
        this.pendingLessonId = null;
        this.pendingLessonTitle = '';
    }

    loadContinueLearning() {
        if (!this.isAuthenticated) return;
        
        this.loadingContinue = true;
        this.lessonProgressService.getContinueLearning().subscribe({
            next: (lesson) => {
                this.continueLesson = lesson;
                this.loadingContinue = false;
            },
            error: (err) => {
                console.error('Error loading continue learning:', err);
                this.continueLesson = null;
                this.loadingContinue = false;
            }
        });
    }

    continueLearning() {
        if (this.continueLesson?.lessonId) {
            this.navigateToLesson(this.continueLesson.lessonId);
        }
    }

    getModuleProgress(moduleId: string): ModuleProgress | undefined {
        return this.moduleProgress.find(m => m.moduleId === moduleId);
    }

    isLessonCompleted(lessonId: string): boolean {
        for (const module of this.moduleProgress) {
            const lesson = module.lessons.find(l => l.lessonId === lessonId);
            if (lesson) return lesson.completed;
        }
        return false;
    }

    getProgressPercentage(moduleId: string): number {
        const progress = this.getModuleProgress(moduleId);
        return progress?.progressPercentage ?? 0;
    }

    getCompletedCount(moduleId: string): string {
        const progress = this.getModuleProgress(moduleId);
        if (!progress) return '0/0';
        return `${progress.completedLessons}/${progress.totalLessons}`;
    }
}