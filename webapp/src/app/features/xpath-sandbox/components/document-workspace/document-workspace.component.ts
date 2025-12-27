
import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, AfterViewInit, ViewChild, TemplateRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, takeUntil } from 'rxjs';

import { ContentType, SandboxContent } from '../../../../core/models/sandbox-content.models';

import { ResizablePanelContainerComponent, PanelConfig } from '../resizable-panel-container/resizable-panel-container.component';
import { ViewToggleControlsComponent, ViewToggle } from '../view-toggle-controls/view-toggle-controls.component';
import { TreeViewComponent } from '../tree-view/tree-view.component';
import { RenderedViewComponent } from '../rendered-view/rendered-view.component';
import { SourceEditorComponent } from '../../../../shared/components/source-editor/source-editor.component';

import { XpathSandboxStateService } from '../../services/xpath-sandbox-state.service';
import { EvaluatorService } from '../../services/evaluator.service';
import { TreeBuilderService } from '../../services/tree-builder.service';
import { HighlightCoordinatorService } from '../../services/highlight-coordinator.service';
import { XPathContextService } from '../../services/xpath-context.service';
import { XPathResult } from '../../models/xpath-types';
import { ExecutedStep } from '../../services/step-evaluation/models/step-evaluation.models';

@Component({
  selector: 'app-document-workspace',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ResizablePanelContainerComponent,
    ViewToggleControlsComponent,
    TreeViewComponent,
    RenderedViewComponent,
    SourceEditorComponent
  ],
  templateUrl: './document-workspace.component.html',
  styleUrls: ['./document-workspace.component.css']
})
export class DocumentWorkspaceComponent implements OnInit, OnDestroy, AfterViewInit {

  @Input() initialText: string = '';
  @Input() currentContent: SandboxContent | null = null;
  @Input() maxCharacters: number = 10000;

  @Output() documentUpdated = new EventEmitter<void>();
  @Output() dirtyStateChange = new EventEmitter<boolean>();
  @Output() renderAvailabilityChange = new EventEmitter<boolean>();
  @Output() textChanged = new EventEmitter<string>();

  @ViewChild(RenderedViewComponent) renderedView?: RenderedViewComponent;
  @ViewChild('sourceTemplate', { read: TemplateRef }) sourceTemplate!: TemplateRef<any>;
  @ViewChild('treeTemplate', { read: TemplateRef }) treeTemplate!: TemplateRef<any>;
  @ViewChild('renderedTemplate', { read: TemplateRef }) renderedTemplate!: TemplateRef<any>;


  editableSource: string = '';
  originalSource: string = '';

  hasUnsavedChanges: boolean = false;

  viewToggles: ViewToggle[] = [
    { id: 'source', label: 'Source Document', visible: true, available: true },
    { id: 'tree', label: 'Document Tree', visible: true, available: true },
    { id: 'rendered', label: 'Rendered View', visible: true, available: false }
  ];

  panelConfigs: PanelConfig[] = [
    { id: 'source', title: 'Source Document', visible: true, width: 33.33, resizable: true, contentTemplate: null },
    { id: 'tree', title: 'Document Tree', visible: true, width: 33.33, resizable: true, contentTemplate: null },
    { id: 'rendered', title: 'Rendered View', visible: true, width: 33.34, resizable: false, contentTemplate: null }
  ];

  private sourceEditSubject$ = new Subject<string>();
  private documentTypeHint: 'html' | 'xml' | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    public state: XpathSandboxStateService,
    private evaluatorService: EvaluatorService,
    private treeBuilderService: TreeBuilderService,
    private highlightCoordinator: HighlightCoordinatorService,
    private contextService: XPathContextService
  ) { }


  ngOnInit(): void {
    this.editableSource = this.initialText || '';
    this.originalSource = this.initialText || '';
    this.hasUnsavedChanges = false;

    this.rebuildWorkspaceFromSource(this.editableSource);
    this.subscribeToHoverEvents();

    this.sourceEditSubject$
      .pipe(debounceTime(500), takeUntil(this.destroy$))
      .subscribe(newSource => {
        this.processSourceEdit(newSource);
      });
  }

  ngAfterViewInit(): void {
    this.panelConfigs[0].contentTemplate = this.sourceTemplate;
    this.panelConfigs[1].contentTemplate = this.treeTemplate;
    this.panelConfigs[2].contentTemplate = this.renderedTemplate;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public loadExternalDocument(text: string, content: SandboxContent | null, docType?: 'html' | 'xml'): void {
    this.currentContent = content;
    this.documentTypeHint = docType || null;
    this.editableSource = text;
    this.originalSource = text;
    this.hasUnsavedChanges = false;
    this.emitDirtyState();

    this.rebuildWorkspaceFromSource(text);
  }

  public refreshHighlightsFromResults(): void {
    this.updateHighlightsFromResults();
  }

  public highlightStep(step: ExecutedStep): void {
    this.updateHighlightsForStep(step);
  }

  onSourceInput(newSource: string): void {
    this.hasUnsavedChanges = newSource !== this.originalSource;
    this.emitDirtyState();
  }

  onSourceEdit(newSource: string): void {
    this.editableSource = newSource;
    this.hasUnsavedChanges = this.editableSource !== this.originalSource;
    this.emitDirtyState();
    this.textChanged.emit(this.editableSource);
    this.sourceEditSubject$.next(newSource);
  }

  private processSourceEdit(newSource: string): void {
    this.rebuildWorkspaceFromSource(newSource);
    this.documentUpdated.emit();
  }

  private rebuildWorkspaceFromSource(sourceText: string): void {
    this.state.setSourceLines(sourceText.split('\n'));

    const detectedType =
      this.currentContent?.type === ContentType.XML ? 'xml' :
      this.currentContent?.type === ContentType.HTML ? 'html' :
      this.documentTypeHint || 'html';

    this.evaluatorService.parseSource(sourceText, detectedType);
    const document = this.evaluatorService.getDocument();

    this.state.setTreeNodes(this.treeBuilderService.buildTree(document));

    if (detectedType === 'html') {
      this.state.setHtmlContent(sourceText);
    } else {
      this.state.setHtmlContent('');
    }

    this.state.clearEvaluationState();
    this.clearHighlights();

    const renderedAvailable = detectedType === 'html';
    this.updateRenderedViewAvailability(renderedAvailable);
    this.renderAvailabilityChange.emit(renderedAvailable);
  }

  private emitDirtyState(): void {
    this.dirtyStateChange.emit(this.hasUnsavedChanges);
  }

  onViewToggleChanged(event: { id: string; visible: boolean }): void {
    const toggle = this.viewToggles.find(t => t.id === event.id);
    if (toggle) toggle.visible = event.visible;

    const panel = this.panelConfigs.find(p => p.id === event.id);
    if (panel) panel.visible = event.visible;
  }

  onPanelWidthsChanged(widths: { [panelId: string]: number }): void {
    Object.keys(widths).forEach(panelId => {
      const panel = this.panelConfigs.find(p => p.id === panelId);
      if (panel) {
        panel.width = widths[panelId];
      }
    });
  }

  private updateRenderedViewAvailability(available: boolean): void {
    const renderedToggle = this.viewToggles.find(t => t.id === 'rendered');
    const renderedPanel = this.panelConfigs.find(p => p.id === 'rendered');

    if (renderedToggle) {
      renderedToggle.available = available;
      if (!available) {
        renderedToggle.visible = false;
        if (renderedPanel) {
          renderedPanel.visible = false;
        }
      } else {
        renderedToggle.visible = true;
        if (renderedPanel) {
          renderedPanel.visible = true;
        }
      }
    }
  }

  private getIframeDocument(): Document | null {
    return this.renderedView?.getIframeDocument() ?? null;
  }

  onIframeLoaded(): void {
    this.updateHighlightsFromResults();
  }

  private clearHighlights(): void {
    this.highlightCoordinator.clearAllHighlights(
      this.state.getTreeNodes(),
      this.getIframeDocument()
    );
    this.state.setHighlightedLineNumbers(new Set<number>());
    this.state.setHoveredLineNumber(null);
  }

  private updateHighlightsFromResults(): void {
    const result = this.highlightCoordinator.updateHighlightsFromResults(
      this.state.getEvaluationResult(),
      this.state.getSourceLines(),
      this.state.getTreeNodes(),
      this.getIframeDocument()
    );

    this.state.setHighlightedLineNumbers(result.highlightedLines);
  }

  private updateHighlightsForStep(step: ExecutedStep): { highlightedLines: Set<number> } {
    const result = this.highlightCoordinator.updateHighlightsForStep(
      step,
      this.state.getSourceLines(),
      this.state.getTreeNodes(),
      this.getIframeDocument()
    );

    return result;
  }

  private subscribeToHoverEvents(): void {
    this.contextService.hoveredResult$
      .pipe(takeUntil(this.destroy$))
      .subscribe(result => {
        const hoveredElement = result?.node.nodeType === Node.ELEMENT_NODE
          ? (result.node as Element)
          : null;

        const hoverResult = this.highlightCoordinator.updateHoverHighlights(
          hoveredElement,
          this.state.getEvaluationResult(),
          this.state.getSourceLines(),
          this.state.getTreeNodes(),
          this.getIframeDocument(),
          this.state.getHighlightedLineNumbers()
        );

        this.state.setHoveredLineNumber(hoverResult.hoveredLineNumber);
      });
  }
  public applyEvaluationResult(result: any): void {
    this.state.setEvaluationResult(result);
    const highlightInfo = this.highlightCoordinator.updateHighlightsFromResults(
      this.state.getEvaluationResult(), 
      this.state.getSourceLines(),
      this.state.getTreeNodes(),
      this.getIframeDocument()
    );

    this.state.setHighlightedLineNumbers(highlightInfo.highlightedLines);
    this.state.setHoveredLineNumber(null);
  }

public applyStepHighlight(step: ExecutedStep): void {
    const highlightInfo = this.updateHighlightsForStep(step);
    this.state.setHighlightedLineNumbers(highlightInfo.highlightedLines);
    this.state.setHoveredLineNumber(null);
  }
}
