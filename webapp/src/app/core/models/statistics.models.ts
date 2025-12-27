export interface AssignmentStatistics {
    assignmentId: number;
    assignmentTitle: string;
    totalPoints: number;
    maxAttempts: number | null;
    dueDate: string | null;
    
    totalStudents: number;
    studentsNotStarted: number;
    studentsInProgress: number;
    studentsCompleted: number;
    
    averageScore: number;
    completionRate: number;
    
    studentStats: StudentStatisticsRow[];
}

export interface StudentStatisticsRow {
    studentId: number;
    studentName: string;
    studentEmail: string;
    
    status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
    attemptsUsed: number;
    bestScore: number | null;
    percentageScore: number | null;
    
    firstStartedAt: string | null;
    lastSubmittedAt: string | null;
    bestAttemptId: number | null;
}

export interface GroupStatistics {
    groupId: number;
    groupName: string;
    totalStudents: number;
    
    totalAssignments: number;
    overallCompletionRate: number;
    
    assignmentSummaries: AssignmentSummaryRow[];
}

export interface AssignmentSummaryRow {
    assignmentId: number;
    assignmentTitle: string;
    dueDate: string | null;
    totalPoints: number;
    
    studentsCompleted: number;
    studentsInProgress: number;
    studentsNotStarted: number;
    
    averageScore: number;
    completionRate: number;
}

export interface StudentProgress {
    studentId: number;
    studentName: string;
    studentEmail: string;
    
    groupId: number;
    groupName: string;
    
    totalAssignments: number;
    assignmentsCompleted: number;
    assignmentsInProgress: number;
    assignmentsNotStarted: number;
    
    totalPointsEarned: number;
    totalPointsPossible: number;
    completionRate: number;
    
    assignmentProgress: AssignmentProgressRow[];
}

export interface AssignmentProgressRow {
    assignmentId: number;
    assignmentTitle: string;
    dueDate: string | null;
    totalPoints: number;
    
    status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
    attemptsUsed: number;
    maxAttempts: number | null;
    bestScore: number | null;
    percentageScore: number | null;
    
    firstStartedAt: string | null;
    lastSubmittedAt: string | null;
    bestAttemptId: number | null;
}

export interface TeacherAttemptView {
    studentId: number;
    studentName: string;
    studentEmail: string;
    
    assignmentId: number;
    assignmentTitle: string;
    totalPoints: number;
    
    attemptId: number;
    attemptNumber: number;
    score: number;
    maxScore: number;
    percentageScore: number;
    
    startedAt: string;
    submittedAt: string | null;
    timeSpentMinutes: number;
    
    completed: boolean;
    expired: boolean;
    
    answers: TeacherExerciseAnswerView[];
}

export interface TeacherExerciseAnswerView {
    answerId: number;
    exerciseId: number;
    exerciseTitle: string;
    exerciseQuestion: string;
    exerciseType: string;
    
    studentAnswerJson: string;
    correct: boolean | null;
    pointsEarned: number;
    pointsPossible: number;
    
    validationResultJson: string | null;
    exerciseConfigJson: string | null;
}