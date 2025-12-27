import {
  ExerciseType,
  ExerciseUnion,
  StudentAssignmentDetail,
} from '../models/assignment.models';

export type BackendExercise = {
  id: number;
  assignmentId: number;
  type: 'MULTIPLE_CHOICE' | 'RADIO' | 'REGEX_SANDBOX' | 'XPATH_SANDBOX';
  orderIndex: number;
  points: number;
  title: string;
  question: string;
  configJson: string;
  explanation?: string;
};

export type BackendAssignmentResponse = {
  id: number;
  title: string;
  description?: string;
  status: 'DRAFT' | 'SUBMITTED';
  dueDate?: string;
  timeLimitMinutes?: number;
  maxAttempts?: number;
  totalPoints?: number;
  exerciseCount?: number;
  exercises: BackendExercise[];
  createdAt?: string;
  updatedAt?: string;

  attemptsUsed?: number;
  hasActiveAttempt?: boolean;
  isExpired?: boolean;
  bestScore?: number;
};

export type AssignmentExtras = Partial<
  Pick<
    StudentAssignmentDetail,
    'attemptsUsed' | 'bestScore' | 'hasActiveAttempt' | 'isExpired'
  >
>;

export function adaptAssignment(resp: BackendAssignmentResponse): StudentAssignmentDetail {
  const exercises: ExerciseUnion[] = resp.exercises.map(adaptExercise);
  const due = resp.dueDate ? new Date(resp.dueDate) : undefined;
  const computedExpired = !!(due && due.getTime() < Date.now());
  
  return {
    id: resp.id,
    title: resp.title,
    description: resp.description,
    timeLimitMinutes: resp.timeLimitMinutes ?? undefined,
    maxAttempts: resp.maxAttempts ?? undefined,
    dueDate: resp.dueDate,
    totalPoints: resp.totalPoints ?? exercises.reduce((sum, e) => sum + (e.points ?? 0), 0),
    exerciseCount: resp.exerciseCount ?? exercises.length,
    attemptsUsed: resp.attemptsUsed ?? 0,
    bestScore: resp.bestScore,
    hasActiveAttempt: resp.hasActiveAttempt ?? false,
    isExpired: resp.isExpired ?? computedExpired,
    exercises,
  };
}

export function adaptExercise(ex: BackendExercise): ExerciseUnion {
  const cfg = safeParseJson(ex.configJson);

  switch (ex.type) {
    case 'MULTIPLE_CHOICE': {
      const options = normalizeOptions(cfg.options);
      return {
        id: ex.id,
        type: ExerciseType.MULTIPLE_CHOICE,
        orderIndex: ex.orderIndex,
        points: ex.points,
        question: ex.question,
        options,
      };
    }
    case 'RADIO': {
      const options = normalizeOptions(cfg.options);
      return {
        id: ex.id,
        type: ExerciseType.RADIO,
        orderIndex: ex.orderIndex,
        points: ex.points,
        question: ex.question,
        options,
      };
    }
    case 'REGEX_SANDBOX': {
      return {
        id: ex.id,
        type: ExerciseType.REGEX,
        orderIndex: ex.orderIndex,
        points: ex.points,
        question: ex.question,
        testCases: cfg.testCases ?? { positive: [], negative: [] },
        solution: cfg.solution ?? '',
        enableRealTimeHighlighting: cfg.enableRealTimeHighlighting ?? true
      };
    }
    case 'XPATH_SANDBOX': {
      return {
        id: ex.id,
        type: ExerciseType.XPATH,
        orderIndex: ex.orderIndex,
        points: ex.points,
        question: ex.question,
        sampleDocument: cfg.sampleDocument ?? '',
        solution: cfg.solution ?? '',
        enableRealTimeHighlighting: cfg.enableRealTimeHighlighting ?? true
      };
    }
    default: {
      return {
        id: ex.id,
        type: ex.type as unknown as ExerciseType,
        orderIndex: ex.orderIndex,
        points: ex.points,
        question: ex.question,
      } as ExerciseUnion;
    }
  }
}

function safeParseJson(value: string | undefined): any {
  if (!value) return {};
  try {
    return JSON.parse(value);
  } catch (error) {
    console.error('Failed to parse exercise configJson:', value, error);
    return {};
  }
}


function normalizeOptions(raw: any): Array<{ id: string; text: string; correct: boolean }> {
  if (!Array.isArray(raw)) return [];
  return raw.map((o: any, idx: number) =>
    typeof o === 'string'
      ? { id: String(idx), text: o, correct: false }
      : { id: String(o?.id ?? idx), text: String(o?.text ?? ''), correct: !!o?.correct }
  );
}
