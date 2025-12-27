import { Injectable } from '@angular/core';
import {
  ExerciseType,
  MultipleChoiceExercise,
  RadioExercise,
  RegexExercise,
  ExerciseOption
} from '../models/assignment.models';
import { DifficultyLevel } from '../models/difficulty.models';

@Injectable({
  providedIn: 'root'
})
export class ExerciseTemplateService {

  constructor() { }

  generateRadioTemplate(difficulty: DifficultyLevel): RadioExercise {
    const config = this.getRadioConfig(difficulty);
    const options = this.createShuffledOptions(config.correctCount, config.incorrectCount);

    return {
      type: ExerciseType.RADIO,
      question: 'Enter your question here',
      points: 1,
      orderIndex: 0,
      options: options
    };
  }

  generateMultipleChoiceTemplate(difficulty: DifficultyLevel): MultipleChoiceExercise {
    const config = this.getMultipleChoiceConfig(difficulty);
    const options = this.createShuffledOptions(config.correctCount, config.incorrectCount);

    return {
      type: ExerciseType.MULTIPLE_CHOICE,
      question: 'Enter your question here',
      points: 1,
      orderIndex: 0,
      options: options
    };
  }

  generateRegexTemplate(): RegexExercise {
    return {
      type: ExerciseType.REGEX,
      question: 'Enter your question here',
      points: 1,
      orderIndex: 0,
      solution: '',
      testCases: {
        positive: [
          { text: '', description: 'Positive test case 1' }
        ],
        negative: []
      },
      enableRealTimeHighlighting: true
    };
  }

  private getRadioConfig(difficulty: DifficultyLevel): { correctCount: number; incorrectCount: number } {
    switch (difficulty) {
      case DifficultyLevel.EASY:
        return { correctCount: 1, incorrectCount: 1 };
      case DifficultyLevel.MEDIUM:
        return { correctCount: 1, incorrectCount: 2 }; 
      case DifficultyLevel.HARD:
        return { correctCount: 1, incorrectCount: 3 };
      default:
        return { correctCount: 1, incorrectCount: 2 };
    }
  }

  private getMultipleChoiceConfig(difficulty: DifficultyLevel): { correctCount: number; incorrectCount: number } {
    switch (difficulty) {
      case DifficultyLevel.EASY:
        return { correctCount: 2, incorrectCount: 1 };
      case DifficultyLevel.MEDIUM:
        return { correctCount: 2, incorrectCount: 2 }; 
      case DifficultyLevel.HARD:
        return { correctCount: 3, incorrectCount: 2 }; 
      default:
        return { correctCount: 2, incorrectCount: 2 };
    }
  }


  private createShuffledOptions(correctCount: number, incorrectCount: number): ExerciseOption[] {
    const options: ExerciseOption[] = [];

    for (let i = 0; i < correctCount; i++) {
      options.push({
        id: '', // Will be assigned by exercise builder
        text: 'Correct answer',
        correct: true
      });
    }

    for (let i = 0; i < incorrectCount; i++) {
      options.push({
        id: '', // Will be assigned by exercise builder
        text: `Incorrect answer ${i + 1}`,
        correct: false
      });
    }

    return this.shuffleArray(options);
  }

  /**
   * Fisher-Yates shuffle algorithm for randomizing array order
   */
  private shuffleArray<T>(array: T[]): T[] {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }
}