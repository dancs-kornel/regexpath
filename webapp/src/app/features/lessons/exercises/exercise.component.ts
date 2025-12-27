import { Component, EventEmitter, Input, Output, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MultipleChoiceComponent } from './multiple-choice/multiple-choice.component';
import { RadioComponent } from './radio/radio.component';
import { BaseExerciseComponent } from './base-exercise.component';
import { RegexSandboxComponent } from './regex-sandbox/regex-sandbox.component';
import { XPathSandboxExerciseComponent } from './xpath-sandbox-exercise/xpath-sandbox-exercise.component';

@Component({
    selector: 'app-exercise',
    standalone: true,
    imports: [CommonModule, MultipleChoiceComponent, RadioComponent, RegexSandboxComponent, XPathSandboxExerciseComponent],
    templateUrl: './exercise.component.html',
    styleUrl: './exercise.component.css'
})
export class ExerciseComponent implements OnChanges {
    @Input() exercise: any = null;
    @Input() lessonId: string = '';
    @Input() difficulty: string = 'MEDIUM';
    @Output() exerciseComplete = new EventEmitter<void>();
    @Output() difficultyPrompt = new EventEmitter<any>();

    @ViewChild('child') child?: BaseExerciseComponent;

    hintsExpanded = false;

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['exercise'] && !changes['exercise'].firstChange) {
            this.reset();
        }
    }

    handleComplete() {
        this.exerciseComplete.emit();
    }

    handleDifficultyPrompt(promptData: any) {
        this.difficultyPrompt.emit(promptData);
    }

    submit() {
        this.child?.submitAnswer();
    }

    reset() {
        this.child?.reset();
    }

    get submitted() { return this.child?.submitted ?? false; }
    get loading() { return this.child?.loading ?? false; }
    get result() { return this.child?.result ?? null; }
    get shouldShowHints() {
        return this.difficulty !== 'HARD';
    }
    get selectedCount() {
        if (this.exercise?.type === 'regex_sandbox') {
            return (this.child as RegexSandboxComponent)?.pattern?.length ?? 0;
        }
        if (this.exercise?.type === 'xpath_sandbox') {
            return (this.child as XPathSandboxExerciseComponent)?.xpathExpression?.length ?? 0;
        }
        return this.child?.selectedOptions?.length ?? 0;
    }

    get solution() { return this.child?.solution ?? null; }
    get solutionLoading() { return this.child?.solutionLoading ?? false; }
    get solutionError() { return this.child?.solutionError ?? null; }

    canShowSolutionButton(): boolean {
        return this.child?.canShowSolution() ?? false;
    }

    showSolution(): void {
        this.child?.showSolution();
    }

    get correctAnswerTexts(): string[] {
        if (!this.result || !this.result.correctAnswers || !this.exercise?.options) {
            return [];
        }
        return this.result.correctAnswers
            .map((id: string) => {
                const option = this.exercise.options.find((opt: any) => opt.id === id);
                return option ? option.text : id;
            })
            .filter((text: string) => text); 
    }
}