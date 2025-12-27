import { Component, OnInit, OnDestroy, ViewEncapsulation, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { Subject, Subscription, takeUntil } from 'rxjs';
import { Sample, XPathResult } from './models/xpath-types';
import { SamplesService } from './services/samples.service';
import { EvaluatorService } from './services/evaluator.service';
import { XPathContextService } from './services/xpath-context.service';
import { XPathStepExecutorService } from './services/step-evaluation/xpath-step-executor.service';
import { XPathStepPlaybackService } from './services/step-evaluation/xpath-step-playback.service';
import { PlaybackState, ExecutedStep } from './services/step-evaluation/models/step-evaluation.models';
import { StepControlsComponent } from './components/step-controls/step-controls.component';
import { XpathInputComponent, TokenButton } from './components/xpath-input/xpath-input.component';
import { ResultsListComponent } from './components/results-list/results-list.component';
import { EvaluationOrchestratorService } from './services/evaluation-orchestrator.service';
import { XpathSandboxStateService } from './services/xpath-sandbox-state.service';
import { XpathExplanationComponent } from './components/xpath-explanation/xpath-explanation.component';
import { ExampleManagerComponent } from '../../shared/components/example-manager/example-manager.component';
import { DocumentWorkspaceComponent } from './components/document-workspace/document-workspace.component';
import { SandboxContentService } from '../../core/services/sandbox-content.service';
import { AuthService } from '../../core/services/auth.service';
import { ContentType, SandboxContent } from '../../core/models/sandbox-content.models';
import { StepToggleComponent } from './components/step-toggle/step-toggle.component';
import { StepToolbarComponent } from './components/step-toolbar/step-toolbar.component';

@Component({
  selector: 'app-xpath-sandbox',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ScrollingModule,
    StepControlsComponent,
    XpathInputComponent,
    ResultsListComponent,
    XpathExplanationComponent,
    ExampleManagerComponent,
    DocumentWorkspaceComponent,
    StepToolbarComponent,
    StepToggleComponent
  ],
  providers: [XPathContextService, XpathSandboxStateService],
  templateUrl: './xpath-sandbox.component.html',
  styleUrls: ['./xpath-sandbox.component.css', '../../shared/styles/sandbox-common.css'],
  encapsulation: ViewEncapsulation.None
})
export class XPathSandboxComponent implements OnInit, OnDestroy {
  @ViewChild('documentWorkspaceRef') workspace?: DocumentWorkspaceComponent;

  get samples() { return this.state.getSamples(); }
  get selectedSample() { return this.state.getSelectedSample(); }
  get xpathExpression() { return this.state.getXpathExpression(); }
  get evaluationResult() { return this.state.getEvaluationResult(); }
  get loading() { return this.state.getLoading(); }

  editableSource: string = '';
  currentContent: SandboxContent | null = null;
  hasUnsavedChanges: boolean = false;

  ContentType = ContentType;

  stepByStepEnabled = false;
  currentPlaybackState: PlaybackState | null = null;
  currentStepDescription = '';
  currentStepMatches = '';
  speedMultiplier = '1x';
  isFirstStep = true;
  isLastStep = false;
  currentStepIndex = 0;
  totalSteps = 0;
  isPlaying = false;
  playbackSpeed = 1000;

  commonTokens: TokenButton[] = [
    { label: '/', value: '/' },
    { label: '//', value: '//' },
    { label: '*', value: '*' },
    { label: '@', value: '@' },
    { label: '[ ]', value: '[]' },
    { label: '.', value: '.' },
    { label: '..', value: '..' },
    { label: 'text()', value: 'text()' }
  ];

  private destroy$ = new Subject<void>();
  private subscriptions = new Subscription();

  constructor(
    private samplesService: SamplesService,
    private evaluatorService: EvaluatorService,
    private contextService: XPathContextService,
    private evaluationOrchestrator: EvaluationOrchestratorService,
    private state: XpathSandboxStateService,
    private stepExecutor: XPathStepExecutorService,
    private stepPlayback: XPathStepPlaybackService,
    private contentService: SandboxContentService,
    private authService: AuthService
  ) { }


  ngOnInit(): void {
    this.loadSamples();
    this.subscribeToPlaybackEvents();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.subscriptions.unsubscribe();
  }

  onExampleManagerContentLoaded(evt: {
    text: string;
    content: SandboxContent | null;
    selectedId: number | null;
  }): void {
    this.currentContent = evt.content;
    this.editableSource = evt.text;
    this.hasUnsavedChanges = false;

    this.workspace?.loadExternalDocument(evt.text, evt.content);

    this.evaluate();
  }

  onWorkspaceDirtyChange(isDirty: boolean): void {
    this.hasUnsavedChanges = isDirty;
  }

  onWorkspaceDocumentUpdated(): void {
    this.evaluate();
  }

  onWorkspaceTextChanged(newText: string): void {
    this.editableSource = newText;
  }

  onRenderedAvailChange(isAvailable: boolean): void {
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  handleFileSelected(file: File): void {
    const validExtensions = ['.html', '.xml', '.xhtml'];
    const hasValidExtension = validExtensions.some(ext =>
      file.name.toLowerCase().endsWith(ext)
    );

    if (!hasValidExtension) {
      alert('Please select an HTML or XML file (.html, .xml, .xhtml)');
      return;
    }

    if (file.size > 1048576) {
      alert('File size exceeds 1MB limit');
      return;
    }

    const isHtml =
      file.name.toLowerCase().endsWith('.html') ||
      file.name.toLowerCase().endsWith('.xhtml');
    const contentType = isHtml ? ContentType.HTML : ContentType.XML;

    const fileName = file.name.replace(/\.(html|xml|xhtml)$/i, '');
    const name = prompt('Enter a name for this content:', fileName);
    if (!name) return;

    this.uploadFile(file, name, contentType);
  }

  uploadFile(file: File, name: string, contentType: ContentType): void {
    this.subscriptions.add(
      this.contentService.uploadContent(file, {
        type: contentType,
        name: name
      }).subscribe({
        next: (content) => {
          this.onExampleManagerContentLoaded({
            text: content.content,
            content,
            selectedId: content.id
          });
        },
        error: (error) => {
          console.error('Upload failed:', error);
          alert(error.error?.message || 'Failed to upload file');
        }
      })
    );
  }

  private loadSamples(): void {
    this.state.setLoading(true);
    this.subscriptions.add(
      this.samplesService.getSamples().subscribe({
        next: (samples) => {
          this.state.setSamples(samples);
          if (samples.length > 0) {
            this.selectSample(samples[0]);
          } else {
            this.state.setLoading(false);
          }
        },
        error: (err) => {
          console.error('Failed to load samples:', err);
          this.state.setLoading(false);
        }
      })
    );
  }

  selectSample(sample: Sample): void {
    this.state.setSelectedSample(sample);
    this.state.setLoading(true);

    this.subscriptions.add(
      this.samplesService.getSampleContent(sample).subscribe({
        next: (contentText) => {
          this.currentContent = null;
          this.editableSource = contentText;
          this.hasUnsavedChanges = false;

          this.workspace?.loadExternalDocument(contentText, null, sample.type);

          this.state.setSourceLines(contentText.split('\n'));
          this.state.clearEvaluationState();

          this.evaluate();

          this.state.setLoading(false);
        },
        error: (err) => {
          console.error('Failed to load sample content:', err);
          this.state.setLoading(false);
        }
      })
    );
  }

  onXpathExpressionChange(expression: string): void {
    this.state.setXpathExpression(expression);

    if (this.stepByStepEnabled && this.currentPlaybackState?.isPlaying) {
      this.stepPlayback.pause();
    }

    this.evaluate().then(() => {
      if (this.stepByStepEnabled) {
        const totalSteps = this.stepPlayback.getTotalSteps();
        if (totalSteps > 0) {
          this.stepPlayback.goToStep(totalSteps - 1);
        }
      }
    });
  }

  onEvaluate(): void {
    this.evaluate();
  }

  private async evaluate(): Promise<void> {
    if (this.evaluationOrchestrator.isEmptyExpression(this.state.getXpathExpression())) {
      this.handleEmptyExpression();
      return;
    }

    const evaluationOutput = await this.evaluationOrchestrator.evaluate({
      expression: this.state.getXpathExpression(),
      sourceLines: this.state.getSourceLines(),
      mode: this.stepByStepEnabled ? 'step-by-step' : 'normal'
    });

    if (!evaluationOutput) return;

    if (evaluationOutput.mode === 'normal') {
      this.handleNormalEvaluation(evaluationOutput.result);
    }

    if (this.stepByStepEnabled) {
      const finalResult = this.evaluatorService.evaluate(this.state.getXpathExpression());
      this.handleNormalEvaluation(finalResult);
    }
  }

  private handleEmptyExpression(): void {
    this.state.clearEvaluationState();
    this.workspace?.applyEvaluationResult(null);
  }

  private handleNormalEvaluation(result: any): void {
    this.state.setEvaluationResult(result);
    this.workspace?.applyEvaluationResult(result);
  }

  toggleStepByStepMode(): void {
    this.stepByStepEnabled = !this.stepByStepEnabled;
    this.evaluationOrchestrator.switchMode(
      this.stepByStepEnabled ? 'step-by-step' : 'normal'
    );
    this.evaluate();
  }

  nextStep(): void {
    this.stepPlayback.next();
  }

  previousStep(): void {
    this.stepPlayback.previous();
  }

  togglePlayPause(): void {
    if (this.currentPlaybackState?.isPlaying) {
      this.stepPlayback.pause();
    } else {
      this.stepPlayback.play();
    }
  }

  resetSteps(): void {
    this.stepPlayback.reset();
  }

  onSpeedChange(speed: number): void {
    this.stepPlayback.setSpeed(speed);
    this.playbackSpeed = speed;
    this.speedMultiplier = this.stepPlayback.getSpeedMultiplier();
  }

  private subscribeToPlaybackEvents(): void {
    this.stepPlayback.state$
      .pipe(takeUntil(this.destroy$))
      .subscribe(state => {
        this.currentPlaybackState = state;

        this.isPlaying = state.isPlaying;
        this.currentStepIndex = state.currentStepIndex;
        this.playbackSpeed = state.speed;
        this.totalSteps = this.stepPlayback.getTotalSteps();

        this.speedMultiplier = this.stepPlayback.getSpeedMultiplier();
        this.isFirstStep = this.stepPlayback.isFirstStep();
        this.isLastStep = this.stepPlayback.isLastStep();
      });

    this.stepPlayback.currentStep$
      .pipe(takeUntil(this.destroy$))
      .subscribe(step => {
        if (step && this.stepByStepEnabled) {
          this.handleStepChange(step);
        }
      });
  }


  private handleStepChange(step: ExecutedStep): void {
    this.currentStepDescription = step.description;
    this.currentStepMatches = this.stepExecutor.getStepSummary(step);
    this.workspace?.applyStepHighlight(step);
  }


  onResultHover(result: XPathResult | null): void {
    this.contextService.setHoveredResult(result);
  }

  onResultClick(result: XPathResult): void {
    this.contextService.setSelectedResult(result);
  }
}
