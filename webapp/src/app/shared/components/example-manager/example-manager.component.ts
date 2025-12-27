
import { Component, EventEmitter, Input, Output, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SandboxContentList, SandboxContent, ContentType, ContentSource } from '../../../core/models/sandbox-content.models';

import { SandboxContentService } from '../../../core/services/sandbox-content.service';
import { AuthService } from '../../../core/services/auth.service';

import { Sample } from '../../../features/xpath-sandbox/models/xpath-types';

@Component({
    selector: 'app-example-manager',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './example-manager.component.html',
    styleUrls: ['./example-manager.component.css']
})
export class ExampleManagerComponent implements OnInit {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    @Input() currentText: string = '';
    @Input() hasUnsavedChanges: boolean = false;
    @Input() isAuthenticated: boolean = false;
    @Input() samples: Sample[] = [];
    @Input() selectedSample: Sample | null = null;
    @Input() uploadAccept: string = '.txt';
    @Input() contentTypes: ContentType[] = [ContentType.REGEX_TEXT];

    @Output() contentSelected = new EventEmitter<number | null>();
    @Output() sampleSelected = new EventEmitter<Sample>();

    @Output() contentLoaded = new EventEmitter<{
        text: string;
        content: SandboxContent | null;
        selectedId: number | null;
    }>();

    @Output() contentCleared = new EventEmitter<void>();

    @Output() contentPersisted = new EventEmitter<{
        content: SandboxContent | null;
        selectedId: number | null;
        text: string;
    }>();
    @Output() fileSelected = new EventEmitter<File>();

    builtinExamples: SandboxContentList[] = [];
    userContents: SandboxContentList[] = [];

    selectedContentId: number | null = null;
    currentContent: SandboxContent | null = null;

    isLoadingContent: boolean = false;
    isUploading: boolean = false;
    isSaving: boolean = false;
    contentError: string | null = null;

    ContentSource = ContentSource;

    constructor(
        private contentService: SandboxContentService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.loadBuiltinExamplesForTypes(this.contentTypes);

        if (this.isAuthenticatedNow()) {
            this.loadUserContentsForTypes(this.contentTypes);
        }
    }

    isAuthenticatedNow(): boolean {
        return this.authService?.isAuthenticated
            ? this.authService.isAuthenticated()
            : this.isAuthenticated;
    }

    onSelectChange(event: Event): void {
        const selectElement = event.target as HTMLSelectElement;
        const rawVal = selectElement.value;

        if (!rawVal) {
            if (this.guardUnsavedChanges() === false) {
                selectElement.value = this.selectedOptionValue();
                return;
            }

            this.selectedContentId = null;
            this.currentContent = null;

            const blankText = this.getDefaultText();

            this.contentLoaded.emit({
                text: blankText,
                content: null,
                selectedId: null
            });
            this.contentCleared.emit();
            this.contentSelected.emit(null);
            return;
        }

        if (rawVal.startsWith('sample-')) {
            const indexStr = rawVal.replace('sample-', '');
            const index = Number(indexStr);

            if (index >= 0 && index < this.samples.length) {
                if (this.guardUnsavedChanges() === false) {
                    selectElement.value = this.selectedOptionValue();
                    return;
                }

                const chosenSample = this.samples[index];
                this.sampleSelected.emit(chosenSample);
                this.contentLoaded.emit({
                    text: (chosenSample as any).content ?? '',
                    content: null,
                    selectedId: null
                });
                this.contentSelected.emit(null);
                this.selectedContentId = null;
                this.currentContent = null;
            }

            return;
        }
        const newId = Number(rawVal);
        if (Number.isNaN(newId)) {
            console.warn('Unknown select value:', rawVal);
            return;
        }

        if (this.guardUnsavedChanges() === false) {
            selectElement.value = this.selectedOptionValue();
            return;
        }

        this.fetchContentById(newId);
    }

    selectedOptionValue(): string {
        if (this.selectedContentId != null) {
            return String(this.selectedContentId);
        }
        if (this.selectedSample) {
            const idx = this.samples.indexOf(this.selectedSample);
            if (idx !== -1) {
                return 'sample-' + idx;
            }
        }
        return '';
    }

    private guardUnsavedChanges(): boolean {
        if (!this.hasUnsavedChanges) return true;
        return confirm('You have unsaved changes. Do you want to discard them?');
    }

    private fetchContentById(id: number): void {
        this.isLoadingContent = true;
        this.contentError = null;

        this.contentService.getContentById(id).subscribe({
            next: (content) => {
                this.isLoadingContent = false;
                this.currentContent = content;
                this.selectedContentId = id;

                this.contentLoaded.emit({
                    text: content.content,
                    content,
                    selectedId: id
                });
                this.contentSelected.emit(id);
            },
            error: (err) => {
                this.isLoadingContent = false;
                console.error('Failed to load content:', err);
                this.contentError = err.error?.message || 'Failed to load content';
            }
        });
    }

    private loadBuiltinExamplesForTypes(types: ContentType[]): void {
        this.isLoadingContent = true;
        this.contentError = null;
        this.builtinExamples = [];

        let pending = types.length;
        if (pending === 0) {
            this.isLoadingContent = false;
            return;
        }

        types.forEach(t => {
            this.contentService.listBuiltinExamples(t).subscribe({
                next: (examplesForType) => {
                    this.builtinExamples = [
                        ...this.builtinExamples,
                        ...examplesForType
                    ];
                    pending--;
                    if (pending === 0) {
                        this.isLoadingContent = false;
                    }
                },
                error: (err) => {
                    console.error('Failed to load built-in examples for type', t, err);
                    pending--;
                    if (pending === 0) {
                        this.isLoadingContent = false;
                    }
                    if (!this.builtinExamples.length) {
                        this.contentError = 'Failed to load examples';
                    }
                }
            });
        });
    }


    private loadUserContentsForTypes(types: ContentType[]): void {
        this.userContents = [];
        let pending = types.length;
        if (pending === 0) {
            return;
        }

        types.forEach(t => {
            this.contentService.listUserContents(t).subscribe({
                next: (contentsForType) => {
                    this.userContents = [
                        ...this.userContents,
                        ...contentsForType
                    ];
                    pending--;
                },
                error: (err) => {
                    console.error('Failed to load user contents for type', t, err);
                    pending--;
                }
            });
        });
    }
    triggerFileUpload(): void {
        if (!this.isAuthenticatedNow()) {
            alert('Log in to upload files');
            return;
        }
        if (this.fileInput) {
            this.fileInput.nativeElement.click();
        }
    }

    onFileInputChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files || input.files.length === 0) {
            return;
        }

        const file = input.files[0];
        this.fileSelected.emit(file);

  
        if (this.isAuthenticatedNow()) {
            if (this.uploadAccept.includes('.txt')) {
                if (!file.name.endsWith('.txt')) {
                    alert('Please select a .txt file');
                    input.value = '';
                    return;
                }

                if (file.size > 1048576) {
                    alert('File size exceeds 1MB limit');
                    input.value = '';
                    return;
                }

                const rawName = file.name.replace(/\.txt$/i, '');
                const name = prompt('Enter a name for this content:', rawName);
                if (!name) {
                    input.value = '';
                    return;
                }

                input.value = '';
                this.uploadFile(file, name);
            } else {
                input.value = '';
            }
        } else {
            input.value = '';
        }
    }

    private uploadFile(file: File, name: string): void {
        this.isUploading = true;
        this.contentError = null;

        this.contentService
            .uploadContent(file, {
                type: ContentType.REGEX_TEXT,
                name: name
            })
            .subscribe({
                next: (content) => {
                    this.isUploading = false;

                    const listItem = this.toListDto(content);
                    this.userContents = [listItem, ...this.userContents];

                    this.currentContent = content;
                    this.selectedContentId = content.id;

                    this.contentLoaded.emit({
                        text: content.content,
                        content,
                        selectedId: content.id
                    });

                    this.contentPersisted.emit({
                        content,
                        selectedId: content.id,
                        text: content.content
                    });
                },
                error: (err) => {
                    console.error('Upload failed:', err);
                    this.contentError =
                        err.error?.message || 'Failed to upload file';
                    this.isUploading = false;
                }
            });
    }

    get canSaveForLater(): boolean {
        if (!this.isAuthenticatedNow()) return false;
        if (!this.currentContent) return false;
        const original = this.currentContent.content;
        return this.currentText !== original;
    }

    onSaveClicked(): void {
        if (!this.canSaveForLater) return;
        if (!this.isAuthenticatedNow()) {
            alert('Please log in to save content');
            return;
        }
        if (!this.currentContent) {
            alert('No content selected');
            return;
        }

        this.isSaving = true;
        this.contentError = null;

        if (this.currentContent.source === ContentSource.BUILTIN) {
            this.forkContent();
        } else {
            this.updateContent();
        }
    }

    private forkContent(): void {
        if (!this.currentContent) return;

        this.contentService
            .forkContent(this.currentContent.id, {
                content: this.currentText
            })
            .subscribe({
                next: (forked) => {
                    this.isSaving = false;

                    this.currentContent = forked;
                    this.selectedContentId = forked.id;

                    const listItem = this.toListDto(forked);
                    this.userContents = [listItem, ...this.userContents];

                    alert('Content saved to your library!');

                    this.contentPersisted.emit({
                        content: forked,
                        selectedId: forked.id,
                        text: this.currentText
                    });
                },
                error: (err) => {
                    console.error('Fork failed:', err);
                    this.contentError =
                        err.error?.message || 'Failed to save content';
                    this.isSaving = false;
                }
            });
    }

    private updateContent(): void {
        if (!this.currentContent) return;

        this.contentService
            .updateContent(this.currentContent.id, {
                name: this.currentContent.name,
                description: this.currentContent.description,
                content: this.currentText,
                category: this.currentContent.category
            })
            .subscribe({
                next: (updated) => {
                    this.isSaving = false;

                    this.currentContent = updated;

                    const idx = this.userContents.findIndex(
                        (c) => c.id === updated.id
                    );
                    if (idx !== -1) {
                        this.userContents[idx] = this.toListDto(updated);
                    }

                    alert('Content updated!');

                    this.contentPersisted.emit({
                        content: updated,
                        selectedId: updated.id,
                        text: this.currentText
                    });
                },
                error: (err) => {
                    console.error('Update failed:', err);
                    this.contentError =
                        err.error?.message || 'Failed to update content';
                    this.isSaving = false;
                }
            });
    }


    get canDelete(): boolean {
        if (!this.isAuthenticatedNow()) return false;
        if (!this.currentContent) return false;
        return this.currentContent.source !== ContentSource.BUILTIN;
    }

    onDeleteClicked(): void {
        if (!this.canDelete) return;
        if (!this.currentContent) return;

        if (this.currentContent.source === ContentSource.BUILTIN) {
            alert('Cannot delete built-in content');
            return;
        }

        const confirmed = confirm(
            `Are you sure you want to delete "${this.currentContent.name}"?`
        );
        if (!confirmed) return;

        this.contentService.deleteContent(this.currentContent.id).subscribe({
            next: () => {
                this.userContents = this.userContents.filter(
                    (c) => c.id !== this.currentContent!.id
                );

                this.currentContent = null;
                this.selectedContentId = null;
                alert('Content deleted');

                const blankText = this.getDefaultText();

                this.contentLoaded.emit({
                    text: blankText,
                    content: null,
                    selectedId: null
                });

                this.contentPersisted.emit({
                    content: null,
                    selectedId: null,
                    text: blankText
                });
            },
            error: (err) => {
                console.error('Delete failed:', err);
                alert(err.error?.message || 'Failed to delete content');
            }
        });
    }


    private toListDto(c: SandboxContent): SandboxContentList {
        return {
            id: c.id,
            type: c.type,
            name: c.name,
            description: c.description,
            source: c.source,
            category: c.category,
            difficultyLevel: c.difficultyLevel,
            createdAt: c.createdAt,
            updatedAt: c.updatedAt,
            contentLength: c.content.length
        };
    }

    private getDefaultText(): string {
        return 'Ez egy példa 1234, benne különböző karakterekkel: @#$%!';
    }
}
