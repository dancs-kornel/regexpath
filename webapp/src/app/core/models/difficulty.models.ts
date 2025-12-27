export enum DifficultyLevel {
    EASY = 'EASY',
    MEDIUM = 'MEDIUM',
    HARD = 'HARD'
}

export interface DifficultyPromptResponse {
    shouldPrompt: boolean;
    promptType?: 'INCREASE' | 'DECREASE';
    currentLevel?: DifficultyLevel;
    suggestedLevel?: DifficultyLevel;
    message?: string;
    consecutiveCorrectFirstAttempts?: number;
    consecutiveStrugglingExercises?: number;
}

export interface RecordAttemptRequest {
    lessonId: string;
    exerciseId: string;
    isCorrect: boolean;
    attemptNumber: number;
}

export interface UpdateDifficultyRequest {
    difficultyLevel: DifficultyLevel;
}

export interface DifficultyLevelResponse {
    difficultyLevel: DifficultyLevel;
}