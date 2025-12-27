import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { SandboxComponent } from './features/sandbox/sandbox.component';
import { LessonsComponent } from './features/lessons/lessons.component';
import { LessonDetailComponent } from './features/lessons/lesson-detail.component';
import { XPathSandboxComponent } from './features/xpath-sandbox/xpath-sandbox.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { authGuard } from './core/guards/auth.guard';
import { GroupsComponent } from './features/groups/groups.component';
import { CreateGroupComponent } from './features/groups/create-group/create-group.component';
import { roleGuard } from './core/guards/role.guard';
import { JoinGroupComponent } from './features/groups/join-group/join-group.component';
import { GroupDetailComponent } from './features/groups/group-detail/group-detail.component';
import { TeacherDashboardComponent } from './features/teacher/dashboard/teacher-dashboard.component';
import { AssignmentListComponent } from './features/teacher/assignments/assignment-list/assignment-list.component';
import { AssignmentCreateComponent } from './features/teacher/assignments/assignment-create/assignment-create.component';
import { AssignmentEditComponent } from './features/teacher/assignments/assignment-edit/assignment-edit.component';
import { AssignmentTakeComponent } from './features/student/assignment-take/assignment-take.component';
import { AttemptResultsComponent } from './features/student/attempt-results/attempt-results.component';
import { AssignmentAttemptsComponent } from './features/student/assignment-attempts/assignment-attempts.component';
import { AssignmentPreviewComponent } from './features/teacher/assignments/assignment-preview/assignment-preview.component';
import { GroupStatisticsComponent } from './features/teacher/statistics/group-statistics/group-statistics.component';
import { AssignmentStatisticsComponent } from './features/teacher/statistics/assignment-statistics/assignment-statistics.component';
import { StudentProgressComponent } from './features/teacher/statistics/student-progress/student-progress.component';
import { TeacherAttemptViewComponent } from './features/teacher/statistics/teacher-attempt-view/teacher-attempt-view.component';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'sandbox', component: SandboxComponent },
    { path: 'xpath-sandbox', component: XPathSandboxComponent },
    {
        path: 'lessons',
        children: [
            { path: '', component: LessonsComponent },
            { path: ':id', component: LessonDetailComponent }
        ]
    },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    {
        path: 'groups',
        canActivate: [authGuard],
        children: [
            { path: '', component: GroupsComponent },
            {
                path: 'create',
                component: CreateGroupComponent,
                canActivate: [roleGuard],
                data: { role: 'TEACHER' }
            },
            {
                path: 'join',
                component: JoinGroupComponent,
                canActivate: [roleGuard],
                data: { role: 'STUDENT' }
            },
            { path: ':id', component: GroupDetailComponent }
        ]
    },
    {
        path: 'teacher',
        canActivate: [authGuard, roleGuard],
        data: { role: 'TEACHER' },
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', component: TeacherDashboardComponent },
            {
                path: 'assignments',
                children: [
                    { path: '', component: AssignmentListComponent },
                    { path: 'create', component: AssignmentCreateComponent },
                    { path: ':id/preview', component: AssignmentPreviewComponent },
                    { path: ':id/edit', component: AssignmentEditComponent }
                ]
            },
            {
                path: 'groups/:id/statistics',
                component: GroupStatisticsComponent
            },
            {
                path: 'groups/:groupId/assignments/:assignmentId/statistics',
                component: AssignmentStatisticsComponent
            },
            {
                path: 'groups/:groupId/students/:studentId/progress',
                component: StudentProgressComponent
            },
            {
                path: 'attempts/:attemptId/view',
                component: TeacherAttemptViewComponent
            }
        ]
    },
    {
        path: 'assignments',
        canActivate: [authGuard, roleGuard],
        data: { role: 'STUDENT' },
        children: [
            { path: ':id/take', component: AssignmentTakeComponent },
            { path: ':id/attempts', component: AssignmentAttemptsComponent },
            { path: ':id/attempts/:attemptId', component: AttemptResultsComponent }
        ]
    }
    //{ path: '**', redirectTo: '/home' }
];