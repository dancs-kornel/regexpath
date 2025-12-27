
import { Component, Input, OnChanges, SimpleChanges, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rendered-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rendered-view.component.html',
  styleUrls: ['./rendered-view.component.css']
})
export class RenderedViewComponent implements OnChanges {
  @Input() htmlContent: string = '';
  @ViewChild('renderedIframe', { static: false }) iframe?: ElementRef<HTMLIFrameElement>;
  @Output() iframeLoaded = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['htmlContent'] && this.htmlContent) {
      this.loadContentIntoIframe();
    }
  }

  private loadContentIntoIframe(): void {
    setTimeout(() => {
      if (this.iframe?.nativeElement) {
        const iframeDoc = this.iframe.nativeElement.contentDocument;
        if (iframeDoc) {
          iframeDoc.open();
          iframeDoc.write(this.htmlContent);
          iframeDoc.close();
          
          setTimeout(() => {
            this.iframeLoaded.emit();
          }, 50);
        }
      }
    }, 0);
  }

  getIframeDocument(): Document | null {
    return this.iframe?.nativeElement.contentDocument || null;
  }
}