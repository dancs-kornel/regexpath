import { 
  AddExerciseRequest, 
  ExerciseType, 
  ExerciseUnion,
  MultipleChoiceExercise,
  RadioExercise,
  RegexExercise,
  XPathExercise
} from '../../../../core/models/assignment.models';

export function toAddExerciseRequest(ex: ExerciseUnion): AddExerciseRequest {
  const title = (ex as any).title ?? ex.question ?? 'Exercise';

  let config: unknown;

  switch (ex.type) {
    case ExerciseType.MULTIPLE_CHOICE:
      config = {
        options: (ex as any).options ?? [],
        correctAnswers: (ex as any).correctAnswers ?? [],
      };
      break;

    case ExerciseType.RADIO:
      config = {
        options: (ex as any).options ?? [],
        correctAnswer: (ex as any).correctAnswer,
      };
      break;

    case ExerciseType.REGEX:
      config = {
        solution: (ex as any).solution ?? '',
        testCases: (ex as any).testCases ?? { positive: [], negative: [] },
        enableRealTimeHighlighting: (ex as any).enableRealTimeHighlighting ?? true,
      };
      break;

    case ExerciseType.XPATH:
      config = {
        sampleDocument: (ex as any).sampleDocument ?? '',
        solution: (ex as any).solution ?? '',
        enableRealTimeHighlighting: (ex as any).enableRealTimeHighlighting ?? true,
      };
      break;

    default:
      config = ex;
  }

  return {
    type: ex.type,
    orderIndex: ex.orderIndex,
    points: ex.points,
    title,
    question: ex.question,
    configJson: JSON.stringify(config),
    explanation: (ex as any).explanation,
  };
}

export function fromBackendResponse(backendEx: any): ExerciseUnion {
  const base = {
    id: backendEx.id,
    orderIndex: backendEx.orderIndex,
    points: backendEx.points,
    question: backendEx.question,
    type: backendEx.type
  };

  const config = JSON.parse(backendEx.configJson);

  switch (backendEx.type) {
    case ExerciseType.MULTIPLE_CHOICE:
      return {
        ...base,
        type: ExerciseType.MULTIPLE_CHOICE,
        options: config.options || []
      } as MultipleChoiceExercise;

    case ExerciseType.RADIO:
      return {
        ...base,
        type: ExerciseType.RADIO,
        options: config.options || []
      } as RadioExercise;

    case ExerciseType.REGEX:
      return {
        ...base,
        type: ExerciseType.REGEX,
        solution: config.solution || '',
        testCases: config.testCases || { positive: [], negative: [] },
        enableRealTimeHighlighting: config.enableRealTimeHighlighting ?? true
      } as RegexExercise;

    case ExerciseType.XPATH:
      return {
        ...base,
        type: ExerciseType.XPATH,
        sampleDocument: config.sampleDocument || '',
        solution: config.solution || '',
        enableRealTimeHighlighting: config.enableRealTimeHighlighting ?? true
      } as XPathExercise;

    default:
      throw new Error(`Unknown exercise type: ${backendEx.type}`);
  }
}