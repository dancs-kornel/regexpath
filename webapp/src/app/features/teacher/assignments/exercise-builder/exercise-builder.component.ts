import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import {
    ExerciseType,
    ExerciseUnion,
    MultipleChoiceExercise,
    RadioExercise,
    RegexExercise,
    XPathExercise,
    ExerciseOption,
    RegexTestCases
} from '../../../../core/models/assignment.models';
import { ExerciseTemplateService } from '../../../../core/services/exercise-template.service';
import { DifficultyLevel } from '../../../../core/models/difficulty.models';
import { RegexExampleService } from '../../../sandbox/regex-example.service';

@Component({
    selector: 'app-exercise-builder',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule],
    templateUrl: './exercise-builder.component.html',
    styleUrl: './exercise-builder.component.css'
})
export class ExerciseBuilderComponent implements OnInit {
    @Input() exercise: ExerciseUnion | null = null;
    @Output() save = new EventEmitter<ExerciseUnion>();
    @Output() cancel = new EventEmitter<void>();

    exerciseForm: FormGroup;
    selectedType: ExerciseType = ExerciseType.MULTIPLE_CHOICE;
    readonly exerciseTypes = ExerciseType;
    readonly difficultyLevels = DifficultyLevel;
    
    selectedDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM;
    showGenerateConfirmation = false;
    isGeneratingExamples = false;
    exampleGenerationError = '';
    
    constructor(
        private fb: FormBuilder,
        private templateService: ExerciseTemplateService,
        private regexExampleService: RegexExampleService
    ) {
        this.exerciseForm = this.createBaseForm();
    }

    ngOnInit(): void {
        if (this.exercise) {
            this.selectedType = this.exercise.type;
            this.setupFormForType(this.selectedType);
            this.populateForm(this.exercise);
        } else {
            this.setupFormForType(this.selectedType);
        }
    }

    createBaseForm(): FormGroup {
        return this.fb.group({
            type: [ExerciseType.MULTIPLE_CHOICE, Validators.required],
            question: ['', [Validators.required, Validators.minLength(5)]],
            points: [1, [Validators.required, Validators.min(1), Validators.max(100)]]
        });
    }

    onTypeChange(type: string): void {
        this.selectedType = type as ExerciseType;
        this.setupFormForType(this.selectedType);
    }

    setupFormForType(type: ExerciseType): void {
        this.exerciseForm = this.createBaseForm();
        this.exerciseForm.patchValue({ type });

        switch (type) {
            case ExerciseType.MULTIPLE_CHOICE:
                this.exerciseForm.addControl('options', this.fb.array(
                    [this.createOption(), this.createOption()],
                    Validators.required
                ));
                this.exerciseForm.addControl('correctAnswers', this.fb.control(
                    [],
                    Validators.required
                ));
                break;

            case ExerciseType.RADIO:
                this.exerciseForm.addControl('options', this.fb.array(
                    [this.createOption(), this.createOption()],
                    Validators.required
                ));
                this.exerciseForm.addControl('correctAnswer', this.fb.control(
                    null,
                    Validators.required
                ));
                break;

            case ExerciseType.REGEX:
                this.exerciseForm.addControl('solution', this.fb.control(
                    '',
                    Validators.required
                ));
                this.exerciseForm.addControl('positiveTestCases', this.fb.array(
                    [this.createTestCase()],
                    Validators.required
                ));
                this.exerciseForm.addControl('negativeTestCases', this.fb.array([]));
                this.exerciseForm.addControl('enableRealTimeHighlighting', this.fb.control(true));
                break;

            case ExerciseType.XPATH:
                this.exerciseForm.addControl('sampleDocument', this.fb.control(
                    '',
                    Validators.required
                ));
                this.exerciseForm.addControl('solution', this.fb.control(
                    '',
                    Validators.required
                ));
                this.exerciseForm.addControl('enableRealTimeHighlighting', this.fb.control(true));
                break;
        }
    }

    populateForm(exercise: ExerciseUnion): void {
        this.exerciseForm.patchValue({
            question: exercise.question,
            points: exercise.points
        });

        switch (exercise.type) {
            case ExerciseType.MULTIPLE_CHOICE:
                const mcExercise = exercise as MultipleChoiceExercise;
                this.setOptions(mcExercise.options);
                this.setCorrectAnswers(mcExercise.options.filter(o => o.correct).map(o => o.id));
                break;

            case ExerciseType.RADIO:
                const radioExercise = exercise as RadioExercise;
                this.setOptions(radioExercise.options);
                const correctOption = radioExercise.options.findIndex(o => o.correct);
                this.exerciseForm.patchValue({ correctAnswer: correctOption >= 0 ? correctOption : null });
                break;

            case ExerciseType.REGEX:
                const regexExercise = exercise as RegexExercise;
                this.exerciseForm.patchValue({ 
                    solution: regexExercise.solution,
                    enableRealTimeHighlighting: regexExercise.enableRealTimeHighlighting ?? true
                });
                this.setTestCases(regexExercise.testCases);
                break;

            case ExerciseType.XPATH:
                const xpathExercise = exercise as XPathExercise;
                this.exerciseForm.patchValue({
                    sampleDocument: xpathExercise.sampleDocument,
                    solution: xpathExercise.solution,
                    enableRealTimeHighlighting: xpathExercise.enableRealTimeHighlighting ?? true
                });
                break;
        }
    }

    isEditMode(): boolean {
        return this.exercise !== null;
    }

    canGenerateTemplate(): boolean {
        return !this.isEditMode() && (
            this.selectedType === ExerciseType.RADIO ||
            this.selectedType === ExerciseType.MULTIPLE_CHOICE ||
            this.selectedType === ExerciseType.REGEX
        );
    }

    requestGenerateTemplate(): void {
        if (this.hasFormData()) {
            this.showGenerateConfirmation = true;
        } else {
            this.generateTemplate();
        }
    }

    confirmGenerateTemplate(): void {
        this.showGenerateConfirmation = false;
        this.generateTemplate();
    }

    cancelGenerateTemplate(): void {
        this.showGenerateConfirmation = false;
    }

    hasFormData(): boolean {
        const question = this.exerciseForm.get('question')?.value || '';
        const points = this.exerciseForm.get('points')?.value;
        
        if (question && question !== 'Enter your question here') {
            return true;
        }
        
        if (points !== 1) {
            return true;
        }

        switch (this.selectedType) {
            case ExerciseType.MULTIPLE_CHOICE:
            case ExerciseType.RADIO:
                const options = this.options.value;
                return options.some((opt: any) => opt.text && opt.text !== 'Correct answer' && !opt.text.startsWith('Incorrect answer'));
            
            case ExerciseType.REGEX:
                const solution = this.exerciseForm.get('solution')?.value;
                const testCases = this.positiveTestCases.value;
                return (solution && solution.length > 0) || 
                       testCases.some((tc: any) => tc.text && tc.text.length > 0);
        }

        return false;
    }

    generateTemplate(): void {
        let template: ExerciseUnion;

        switch (this.selectedType) {
            case ExerciseType.RADIO:
                template = this.templateService.generateRadioTemplate(this.selectedDifficulty);
                break;

            case ExerciseType.MULTIPLE_CHOICE:
                template = this.templateService.generateMultipleChoiceTemplate(this.selectedDifficulty);
                break;

            case ExerciseType.REGEX:
                template = this.templateService.generateRegexTemplate();
                break;

            default:
                return;
        }

        this.setupFormForType(this.selectedType);
        this.populateFormWithTemplate(template);
    }

    generateRegexExamples(): void {
        const solution = this.exerciseForm.get('solution')?.value;
        
        if (!solution || solution.trim() === '') {
            this.exampleGenerationError = 'Please enter a regex pattern first';
            return;
        }

        this.isGeneratingExamples = true;
        this.exampleGenerationError = '';

        this.regexExampleService.generateExamples(solution, false, false).subscribe({
            next: (response) => {
                if (response.errorMessage) {
                    this.exampleGenerationError = response.errorMessage;
                } else {
                    this.populateTestCasesFromExamples(response);
                }
                this.isGeneratingExamples = false;
            },
            error: (error) => {
                this.exampleGenerationError = error?.error?.message || 'Failed to generate examples';
                this.isGeneratingExamples = false;
            }
        });
    }

    populateTestCasesFromExamples(response: any): void {
    const positiveArray = this.exerciseForm.get('positiveTestCases') as FormArray;
    const negativeArray = this.exerciseForm.get('negativeTestCases') as FormArray;

    positiveArray.clear();
    negativeArray.clear();

    response.positiveExamples.forEach((example: any) => {
        positiveArray.push(this.fb.group({
            text: [example.text, Validators.required],
            description: ['']
        }));
    });

    if (positiveArray.length === 0) {
        positiveArray.push(this.createTestCase());
    }
}

    populateFormWithTemplate(template: ExerciseUnion): void {
        this.exerciseForm.patchValue({
            question: template.question,
            points: template.points
        });

        switch (template.type) {
            case ExerciseType.MULTIPLE_CHOICE:
            case ExerciseType.RADIO:
                const choiceTemplate = template as MultipleChoiceExercise | RadioExercise;
                this.setOptionsFromTemplate(choiceTemplate.options);
                
                if (template.type === ExerciseType.RADIO) {
                    const correctIndex = choiceTemplate.options.findIndex(o => o.correct);
                    this.exerciseForm.patchValue({ correctAnswer: correctIndex });
                } else {
                    const correctIndices = choiceTemplate.options
                        .map((o, i) => o.correct ? i : -1)
                        .filter(i => i >= 0);
                    this.exerciseForm.patchValue({ correctAnswers: correctIndices });
                }
                break;

            case ExerciseType.REGEX:
                const regexTemplate = template as RegexExercise;
                this.exerciseForm.patchValue({
                    solution: regexTemplate.solution,
                    enableRealTimeHighlighting: regexTemplate.enableRealTimeHighlighting
                });
                this.setTestCasesFromTemplate(regexTemplate.testCases);
                break;
        }
    }

    setOptionsFromTemplate(options: ExerciseOption[]): void {
        const optionsArray = this.exerciseForm.get('options') as FormArray;
        optionsArray.clear();
        options.forEach(opt => {
            optionsArray.push(this.fb.group({ 
                text: [opt.text, Validators.required],
                correct: [opt.correct]
            }));
        });
    }

    setTestCasesFromTemplate(testCases: RegexTestCases): void {
        const positiveArray = this.exerciseForm.get('positiveTestCases') as FormArray;
        const negativeArray = this.exerciseForm.get('negativeTestCases') as FormArray;
        
        positiveArray.clear();
        negativeArray.clear();
        
        testCases.positive.forEach(tc => {
            positiveArray.push(this.fb.group({
                text: [tc.text, Validators.required],
                description: [tc.description || '']
            }));
        });
        
        testCases.negative.forEach(tc => {
            negativeArray.push(this.fb.group({
                text: [tc.text, Validators.required],
                description: [tc.description || '']
            }));
        });
    }

    createOption(): FormGroup {
        return this.fb.group({
            text: ['', Validators.required]
        });
    }

    createTestCase(): FormGroup {
        return this.fb.group({
            text: ['', Validators.required],
            description: ['']
        });
    }

    get options(): FormArray {
        return this.exerciseForm.get('options') as FormArray;
    }

    get positiveTestCases(): FormArray {
        return this.exerciseForm.get('positiveTestCases') as FormArray;
    }

    get negativeTestCases(): FormArray {
        return this.exerciseForm.get('negativeTestCases') as FormArray;
    }

    addOption(): void {
        this.options.push(this.createOption());
    }

    removeOption(index: number): void {
        if (this.options.length > 2) {
            this.options.removeAt(index);
            if (this.selectedType === ExerciseType.MULTIPLE_CHOICE) {
                const correctAnswers = this.exerciseForm.get('correctAnswers')?.value || [];
                this.exerciseForm.patchValue({
                    correctAnswers: correctAnswers.filter((i: number) => i !== index)
                        .map((i: number) => i > index ? i - 1 : i)
                });
            } else if (this.selectedType === ExerciseType.RADIO) {
                const correctAnswer = this.exerciseForm.get('correctAnswer')?.value;
                if (correctAnswer === index) {
                    this.exerciseForm.patchValue({ correctAnswer: null });
                } else if (correctAnswer > index) {
                    this.exerciseForm.patchValue({ correctAnswer: correctAnswer - 1 });
                }
            }
        }
    }

    addPositiveTestCase(): void {
        this.positiveTestCases.push(this.createTestCase());
    }

    removePositiveTestCase(index: number): void {
        if (this.positiveTestCases.length > 1) {
            this.positiveTestCases.removeAt(index);
        }
    }

    addNegativeTestCase(): void {
        this.negativeTestCases.push(this.createTestCase());
    }

    removeNegativeTestCase(index: number): void {
        this.negativeTestCases.removeAt(index);
    }

    setOptions(options: ExerciseOption[]): void {
        const optionsArray = this.exerciseForm.get('options') as FormArray;
        optionsArray.clear();
        options.forEach(opt => {
            optionsArray.push(this.fb.group({ text: [opt.text, Validators.required] }));
        });
    }

    setCorrectAnswers(answers: string[]): void {
        const indices = answers.map(id => {
            const option = (this.exercise as MultipleChoiceExercise)?.options.findIndex(o => o.id === id);
            return option >= 0 ? option : -1;
        }).filter(i => i >= 0);
        this.exerciseForm.patchValue({ correctAnswers: indices });
    }

    setTestCases(testCases: RegexTestCases): void {
        const positiveArray = this.exerciseForm.get('positiveTestCases') as FormArray;
        const negativeArray = this.exerciseForm.get('negativeTestCases') as FormArray;
        
        positiveArray.clear();
        negativeArray.clear();
        
        testCases.positive.forEach(tc => {
            positiveArray.push(this.fb.group({
                text: [tc.text, Validators.required],
                description: [tc.description || '']
            }));
        });
        
        testCases.negative.forEach(tc => {
            negativeArray.push(this.fb.group({
                text: [tc.text, Validators.required],
                description: [tc.description || '']
            }));
        });
    }

    toggleCorrectAnswer(index: number): void {
        if (this.selectedType === ExerciseType.MULTIPLE_CHOICE) {
            const correctAnswers = this.exerciseForm.get('correctAnswers')?.value || [];
            const idx = correctAnswers.indexOf(index);
            if (idx > -1) {
                correctAnswers.splice(idx, 1);
            } else {
                correctAnswers.push(index);
            }
            this.exerciseForm.patchValue({ correctAnswers });
        }
    }

    isCorrectAnswer(index: number): boolean {
        if (this.selectedType === ExerciseType.MULTIPLE_CHOICE) {
            const correctAnswers = this.exerciseForm.get('correctAnswers')?.value || [];
            return correctAnswers.includes(index);
        } else if (this.selectedType === ExerciseType.RADIO) {
            return this.exerciseForm.get('correctAnswer')?.value === index;
        }
        return false;
    }

    onSubmit(): void {
        if (this.exerciseForm.invalid) {
            Object.keys(this.exerciseForm.controls).forEach(key => {
                this.exerciseForm.get(key)?.markAsTouched();
            });
            return;
        }

        const formValue = this.exerciseForm.value;
        let exercise: ExerciseUnion;

        switch (this.selectedType) {
            case ExerciseType.MULTIPLE_CHOICE:
                const mcOptions: ExerciseOption[] = formValue.options.map((o: any, i: number) => ({
                    id: String.fromCharCode(97 + i), // a, b, c, d...
                    text: o.text,
                    correct: formValue.correctAnswers.includes(i)
                }));
                
                exercise = {
                    type: ExerciseType.MULTIPLE_CHOICE,
                    question: formValue.question,
                    points: formValue.points,
                    orderIndex: 0,
                    options: mcOptions
                } as MultipleChoiceExercise;
                break;

            case ExerciseType.RADIO:
                const radioOptions: ExerciseOption[] = formValue.options.map((o: any, i: number) => ({
                    id: String.fromCharCode(97 + i),
                    text: o.text,
                    correct: formValue.correctAnswer === i
                }));
                
                exercise = {
                    type: ExerciseType.RADIO,
                    question: formValue.question,
                    points: formValue.points,
                    orderIndex: 0,
                    options: radioOptions
                } as RadioExercise;
                break;

            case ExerciseType.REGEX:
                exercise = {
                    type: ExerciseType.REGEX,
                    question: formValue.question,
                    points: formValue.points,
                    orderIndex: 0,
                    solution: formValue.solution,
                    testCases: {
                        positive: formValue.positiveTestCases,
                        negative: formValue.negativeTestCases
                    },
                    enableRealTimeHighlighting: formValue.enableRealTimeHighlighting
                } as RegexExercise;
                break;

            case ExerciseType.XPATH:
                exercise = {
                    type: ExerciseType.XPATH,
                    question: formValue.question,
                    points: formValue.points,
                    orderIndex: 0,
                    sampleDocument: formValue.sampleDocument,
                    solution: formValue.solution,
                    enableRealTimeHighlighting: formValue.enableRealTimeHighlighting
                } as XPathExercise;
                break;

            default:
                return;
        }

        if (this.exercise?.id) {
            exercise.id = this.exercise.id;
        }

        this.save.emit(exercise);
    }

    onCancel(): void {
        this.cancel.emit();
    }
}