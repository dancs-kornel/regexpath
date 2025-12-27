import { Injectable } from '@angular/core';
import { EvaluationResult } from '../models/xpath-types';
import { EvaluatorService } from './evaluator.service';
import { XPathStepDecomposerService } from './step-evaluation/xpath-step-decomposer.service';
import { XPathStepExecutorService } from './step-evaluation/xpath-step-executor.service';
import { XPathStepPlaybackService } from './step-evaluation/xpath-step-playback.service';
import { ExecutedStep } from './step-evaluation/models/step-evaluation.models';

export type EvaluationMode = 'normal' | 'step-by-step';

export interface EvaluationContext {
  expression: string;
  sourceLines: string[];
  mode: EvaluationMode;
}

export interface NormalEvaluationResult {
  mode: 'normal';
  result: EvaluationResult;
}

export interface StepEvaluationResult {
  mode: 'step-by-step';
  steps: ExecutedStep[];
}

export type EvaluationOutput = NormalEvaluationResult | StepEvaluationResult | null;


@Injectable({
  providedIn: 'root'
})
export class EvaluationOrchestratorService {
  constructor(
    private evaluator: EvaluatorService,
    private stepDecomposer: XPathStepDecomposerService,
    private stepExecutor: XPathStepExecutorService,
    private stepPlayback: XPathStepPlaybackService
  ) {}


  async evaluate(context: EvaluationContext): Promise<EvaluationOutput> {
    const expr = context.expression.trim();
    
    if (!expr) {
      return null;
    }

    if (context.mode === 'step-by-step') {
      return await this.evaluateStepByStep(expr, context.sourceLines);
    } else {
      return this.evaluateNormal(expr);
    }
  }

  switchMode(mode: EvaluationMode): void {
    this.stepPlayback.setEnabled(mode === 'step-by-step');
  }

  isEmptyExpression(expression: string): boolean {
    return expression.trim() === '';
  }

  private evaluateNormal(expression: string): NormalEvaluationResult {
    const result = this.evaluator.evaluate(expression);
    return {
      mode: 'normal',
      result
    };
  }

  private async evaluateStepByStep(expression: string, sourceLines: string[]): Promise<StepEvaluationResult | NormalEvaluationResult> {
    const decomposition = await this.stepDecomposer.decomposeXPath(expression);
    
    if (!decomposition.isValid) {
      return {
        mode: 'normal',
        result: {
          matches: [],
          error: decomposition.error
        }
      };
    }

    const executedSteps = this.stepExecutor.executeSteps(
      decomposition.steps,
      sourceLines
    );

    this.stepPlayback.loadSteps(executedSteps);

    return {
      mode: 'step-by-step',
      steps: executedSteps
    };
  }
}