export enum ContentType {
    REGEX_TEXT = 'REGEX_TEXT',
    HTML = 'HTML',
    XML = 'XML'
}

export enum ContentSource {
    BUILTIN = 'BUILTIN',
    USER_UPLOADED = 'USER_UPLOADED',
    USER_EDITED = 'USER_EDITED'
}

export interface SandboxContentList {
    id: number;
    type: ContentType;
    name: string;
    description: string | null;
    source: ContentSource;
    category: string | null;
    difficultyLevel: number | null;
    createdAt: string;
    updatedAt: string;
    contentLength: number;
}

export interface SandboxContent {
    id: number;
    type: ContentType;
    name: string;
    description: string | null;
    content: string;
    source: ContentSource;
    ownerId: number | null;
    category: string | null;
    difficultyLevel: number | null;
    originalContentId: number | null;
    createdAt: string;
    updatedAt: string;
}

export interface UploadContentRequest {
    type: ContentType;
    name: string;
    description?: string;
    category?: string;
}

export interface ForkContentRequest {
    content: string;
    name?: string;
    description?: string;
}

export interface UpdateContentRequest {
    name: string;
    description: string | null;
    content: string;
    category: string | null;
}