export interface LessonProgress {
  lessonId: string;
  lessonTitle: string;
  completed: boolean;
  completedAt: string | null;
  lastAccessedAt: string | null;
}

export interface ModuleProgress {
  moduleId: string;
  moduleTitle: string;
  totalLessons: number;
  completedLessons: number;
  progressPercentage: number;
  lessons: LessonProgress[];
}

export interface ContinueLearning {
  lessonId: string;
  lessonTitle: string;
  moduleId: string;
  moduleTitle: string;
  lastAccessedAt: string;
}

export interface MarkLessonCompleteRequest {
  lessonId: string;
}

export interface PrerequisiteInfo {
  lessonId: string;
  title: string;
  description: string;
  completed: boolean;
}

export interface PrerequisiteCheckResponse {
  canAccess: boolean;
  unmetPrerequisites: PrerequisiteInfo[];
}