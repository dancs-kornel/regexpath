import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

interface RegexToken {
    label: string;
    value: string;
    title: string;
}

@Component({
    selector: 'app-regex-builder',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './regex-builder.component.html',
    styleUrls: ['./regex-builder.component.css', '../../../shared/styles/sandbox-common.css']
})
export class RegexBuilderComponent {
    @Output() insertToken = new EventEmitter<string>();
    tokens: RegexToken[] = [
        { label: '\\d', value: '\\d', title: 'számjegy' },
        { label: '\\w', value: '\\w', title: 'betű vagy szám vagy aláhúzás' },
        { label: '.', value: '.', title: 'bármilyen karakter' },
        { label: '+', value: '+', title: 'előző minta 1 vagy több alkalommal' },
        { label: '*', value: '*', title: 'előző minta 0 vagy több alkalommal' },
        { label: '?', value: '?', title: 'előző minta opcionális' },
        { label: '[ ]', value: '[]', title: 'szögletes zárójel közé' },
        { label: '( )', value: '()', title: 'kerek zárójel közé' },
        { label: '{ }', value: '{}', title: 'görbített zárójel közé' },
    ];


    handleMouseDown(event: MouseEvent, value: string) {
        event.preventDefault();
        this.insertToken.emit(value);
    }
}