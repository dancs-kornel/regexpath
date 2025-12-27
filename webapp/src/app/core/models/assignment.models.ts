
export enum ExerciseType {
    MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
    RADIO = 'RADIO',
    REGEX = 'REGEX_SANDBOX',
    XPATH = 'XPATH_SANDBOX'
}

export enum AssignmentStatus {
    DRAFT = 'DRAFT',
    SUBMITTED = 'SUBMITTED' 
}

export interface ExerciseOption {
    id: string;
    text: string;
    correct: boolean;
}

export interface Exercise {
    id?: number;
    type: ExerciseType;
    orderIndex: number;
    points: number;
    question: string;
}

export interface MultipleChoiceExercise extends Exercise {
    type: ExerciseType.MULTIPLE_CHOICE;
    options: ExerciseOption[];
    correctAnswers?: number[]; 
}

export interface RadioExercise extends Exercise {
    type: ExerciseType.RADIO;
    options: ExerciseOption[];
    correctAnswer?: number;
}

export interface RegexExercise extends Exercise {
    type: ExerciseType.REGEX;
    testCases: RegexTestCases;
    solution: string;
    enableRealTimeHighlighting: boolean;
}

export interface XPathExercise extends Exercise {
    type: ExerciseType.XPATH;
    sampleDocument: string;
    solution: string;
    enableRealTimeHighlighting: boolean;
}

export interface RegexTestCases {
    positive: RegexTestCase[];
    negative: RegexTestCase[];
}

export interface RegexTestCase {
    text: string;
    description: string;
    expectedMatches?: string[];
}

export interface TestCase {
    input: string;
    shouldMatch: boolean;
}

export type ExerciseUnion = MultipleChoiceExercise | RadioExercise | RegexExercise | XPathExercise;

export interface Assignment {
    id?: number;
    title: string;
    description?: string;
    status: AssignmentStatus;
    dueDate?: string; // ISO date string
    timeLimitMinutes?: number | null; 
    maxAttempts?: number | null;     
    exercises: ExerciseUnion[];
    createdAt?: string;
    updatedAt?: string;
    teacherId?: number;             
    totalPoints?: number;
}

export interface CreateAssignmentRequest {
    title: string;
    description?: string;
    status: AssignmentStatus;
    dueDate?: string;
    timeLimitMinutes?: number | null;
    maxAttempts?: number | null;
    exercises: AddExerciseRequest[];
}

export interface UpdateAssignmentRequest {
    id: number;
    title: string;
    description?: string;
    status: AssignmentStatus;
    dueDate?: string;
    timeLimitMinutes?: number | null;
    maxAttempts?: number | null;
}

export interface AssignmentSummary {
    id: number;
    title: string;
    status: AssignmentStatus;
    exerciseCount: number;
    totalPoints: number;
    dueDate?: string;
    createdAt: string;
    assignedGroupsCount: number;
}

export interface AssignGroupRequest {
    assignmentId: number;
    groupIds: number[];
}

export interface StudentAssignmentSummary {
    id: number;
    title: string;
    description?: string;
    dueDate?: string;
    timeLimitMinutes?: number | null;
    maxAttempts?: number | null;
    totalPoints: number;
    exerciseCount: number;
    attemptsUsed: number;
    bestScore?: number | null;
    hasActiveAttempt: boolean;
    isExpired: boolean;
}

export interface StudentAssignmentDetail extends StudentAssignmentSummary {
    exercises: ExerciseUnion[];
}

export interface AssignmentAttempt {
    id: number;
    assignmentId: number;
    attemptNumber: number;
    score: number;
    maxScore: number;
    percentageScore: number;
    startedAt: string;
    submittedAt?: string;
    expiresAt?: string;
    completed: boolean;
    expired: boolean;
    canViewAnswers: boolean;
    answers: ExerciseAnswerResponse[];
}

export interface AttemptSummary {
    id: number;
    attemptNumber: number;
    score: number;
    maxScore: number;
    percentageScore: number;
    startedAt: string;
    submittedAt?: string;
    completed: boolean;
}

export interface ExerciseAnswerResponse {
    exerciseId: number;
    answerJson: string;
    correct?: boolean; 
    pointsEarned: number;
    validationResultJson?: string;
}

export interface ExerciseAnswerRequest {
    exerciseId: number;
    answerJson: string;
}

export interface SubmitAttemptRequest {
    answers: ExerciseAnswerRequest[];
}

export interface AddExerciseRequest {
    type: ExerciseType;
    orderIndex: number;
    points: number;

    title: string;
    question: string;
    configJson: string;

    explanation?: string;
}

export interface ExerciseChange {
    exercise: ExerciseUnion;
    action: 'create' | 'update' | 'delete';
}