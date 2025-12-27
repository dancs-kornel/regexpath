export interface Group {
    id: number;
    name: string;
    description: string;
    inviteCode: string;
    teacherId: number;
    teacherName: string;
    memberCount: number;
    createdAt: string;
    updatedAt: string;
    active: boolean;
    upcomingAssignments?: UpcomingAssignment[];
}

export interface CreateGroupRequest {
    name: string;
    description: string;
}

export interface JoinGroupRequest {
    inviteCode: string;
}

export interface GroupMember {
    id: number;
    userId: number;
    username: string;
    email: string;
    joinedAt: string;
}

export interface UpcomingAssignment {
    id: number;
    title: string;
    dueDate: string;
    totalPoints: number;
}