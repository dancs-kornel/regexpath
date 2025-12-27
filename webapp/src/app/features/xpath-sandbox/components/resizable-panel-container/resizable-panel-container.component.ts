import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface PanelConfig {
  id: string;
  title: string;
  visible: boolean;
  width: number;
  resizable: boolean;
  contentTemplate?: TemplateRef<any> | null;
}

@Component({
  selector: 'app-resizable-panel-container',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './resizable-panel-container.component.html',
  styleUrls: ['./resizable-panel-container.component.css']
})
export class ResizablePanelContainerComponent implements OnInit, OnDestroy {
  @Input() panels: PanelConfig[] = [];
  @Output() widthsChanged = new EventEmitter<{ [panelId: string]: number }>();

  private isResizing = false;
  private resizingPanelIndex: number | null = null;
  private startX = 0;
  private startWidths: number[] = [];

  ngOnInit(): void {
    this.setupResizeListeners();
  }

  ngOnDestroy(): void {
    this.removeResizeListeners();
  }

  recalculateWidths(): void {
    const visiblePanels = this.panels.filter(p => p.visible);
    
    if (visiblePanels.length === 0) return;

    const equalWidth = 100 / visiblePanels.length;
    
    this.panels.forEach(panel => {
      if (panel.visible) {
        panel.width = equalWidth;
      }
    });

    this.emitWidthChanges();
  }

  startResize(event: MouseEvent, panelIndex: number): void {
    event.preventDefault();
    this.isResizing = true;
    this.resizingPanelIndex = panelIndex;
    this.startX = event.clientX;
    this.startWidths = this.panels.map(p => p.width);
    
    document.body.classList.add('resizing');
  }

  private handleMouseMove = (event: MouseEvent): void => {
    if (!this.isResizing || this.resizingPanelIndex === null) return;

    const container = document.querySelector('.view-container') as HTMLElement;
    if (!container) return;

    const containerWidth = container.offsetWidth;
    const deltaX = event.clientX - this.startX;
    const deltaPercent = (deltaX / containerWidth) * 100;

    const visiblePanels = this.panels.filter(p => p.visible);
    const resizingPanelGlobalIndex = this.resizingPanelIndex;
    const resizingPanel = this.panels[resizingPanelGlobalIndex];

    let nextVisibleIndex = -1;
    for (let i = resizingPanelGlobalIndex + 1; i < this.panels.length; i++) {
      if (this.panels[i].visible) {
        nextVisibleIndex = i;
        break;
      }
    }

    if (nextVisibleIndex === -1) return;

    const newResizingWidth = Math.max(10, Math.min(80, this.startWidths[resizingPanelGlobalIndex] + deltaPercent));
    const diff = newResizingWidth - this.startWidths[resizingPanelGlobalIndex];

    const remainingVisiblePanels = visiblePanels.slice(
      visiblePanels.findIndex(p => p.id === this.panels[nextVisibleIndex].id) + 1
    );

    if (remainingVisiblePanels.length > 0) {
      const totalOtherWidth = this.startWidths[nextVisibleIndex] + 
        remainingVisiblePanels.reduce((sum, p) => {
          const idx = this.panels.findIndex(panel => panel.id === p.id);
          return sum + this.startWidths[idx];
        }, 0);

      const nextPanelRatio = this.startWidths[nextVisibleIndex] / totalOtherWidth;

      this.panels[resizingPanelGlobalIndex].width = newResizingWidth;
      
      this.panels[nextVisibleIndex].width = Math.max(10, this.startWidths[nextVisibleIndex] - (diff * nextPanelRatio));

      remainingVisiblePanels.forEach(p => {
        const idx = this.panels.findIndex(panel => panel.id === p.id);
        const ratio = this.startWidths[idx] / totalOtherWidth;
        this.panels[idx].width = Math.max(10, this.startWidths[idx] - (diff * ratio));
      });
    } else {
      this.panels[resizingPanelGlobalIndex].width = newResizingWidth;
      this.panels[nextVisibleIndex].width = Math.max(10, this.startWidths[nextVisibleIndex] - diff);
    }

    this.emitWidthChanges();
  };

  private handleMouseUp = (): void => {
    if (this.isResizing) {
      this.isResizing = false;
      this.resizingPanelIndex = null;
      document.body.classList.remove('resizing');
    }
  };

  private setupResizeListeners(): void {
    document.addEventListener('mousemove', this.handleMouseMove);
    document.addEventListener('mouseup', this.handleMouseUp);
  }

  private removeResizeListeners(): void {
    document.removeEventListener('mousemove', this.handleMouseMove);
    document.removeEventListener('mouseup', this.handleMouseUp);
  }

  private emitWidthChanges(): void {
    const widths: { [panelId: string]: number } = {};
    this.panels.forEach(panel => {
      widths[panel.id] = panel.width;
    });
    this.widthsChanged.emit(widths);
  }

  getVisiblePanels(): PanelConfig[] {
    return this.panels.filter(p => p.visible);
  }

  shouldShowResizeHandle(panelIndex: number): boolean {
    const panel = this.panels[panelIndex];
    if (!panel.resizable) return false;

    for (let i = panelIndex + 1; i < this.panels.length; i++) {
      if (this.panels[i].visible) {
        return true;
      }
    }
    return false;
  }
}