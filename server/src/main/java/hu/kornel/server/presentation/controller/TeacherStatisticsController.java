package hu.kornel.server.presentation.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.assignments.AssignmentStatisticsResponse;
import hu.kornel.server.application.dto.assignments.GroupStatisticsResponse;
import hu.kornel.server.application.dto.assignments.StudentProgressResponse;
import hu.kornel.server.application.dto.assignments.TeacherAttemptViewResponse;
import hu.kornel.server.application.usecase.assignments.ExportGroupStatisticsUseCase;
import hu.kornel.server.application.usecase.assignments.GetAssignmentStatisticsUseCase;
import hu.kornel.server.application.usecase.assignments.GetGroupStatisticsUseCase;
import hu.kornel.server.application.usecase.assignments.GetStudentProgressUseCase;
import hu.kornel.server.application.usecase.assignments.GetTeacherAttemptViewUseCase;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherStatisticsController {

    private final GetAssignmentStatisticsUseCase getAssignmentStatisticsUseCase;
    private final GetGroupStatisticsUseCase getGroupStatisticsUseCase;
    private final GetStudentProgressUseCase getStudentProgressUseCase;
    private final GetTeacherAttemptViewUseCase getTeacherAttemptViewUseCase;
    private final ExportGroupStatisticsUseCase exportGroupStatisticsUseCase;

    @GetMapping("/groups/{groupId}/assignments/{assignmentId}/statistics")
    public ResponseEntity<AssignmentStatisticsResponse> getAssignmentStatistics(
            @PathVariable Long groupId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/teacher/groups/{}/assignments/{}/statistics - Fetching assignment statistics", 
                groupId, assignmentId);

        AssignmentStatisticsResponse response = getAssignmentStatisticsUseCase.execute(
                assignmentId, groupId, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/statistics")
    public ResponseEntity<GroupStatisticsResponse> getGroupStatistics(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/teacher/groups/{}/statistics - Fetching group statistics", groupId);

        GroupStatisticsResponse response = getGroupStatisticsUseCase.execute(
                groupId, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/students/{studentId}/progress")
    public ResponseEntity<StudentProgressResponse> getStudentProgress(
            @PathVariable Long groupId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/teacher/groups/{}/students/{}/progress - Fetching student progress", 
                groupId, studentId);

        StudentProgressResponse response = getStudentProgressUseCase.execute(
                groupId, studentId, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts/{attemptId}/view")
    public ResponseEntity<TeacherAttemptViewResponse> viewStudentAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/teacher/attempts/{}/view - Teacher viewing student attempt", attemptId);

        TeacherAttemptViewResponse response = getTeacherAttemptViewUseCase.execute(
                attemptId, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/statistics/export")
    public ResponseEntity<byte[]> exportGroupStatistics(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/teacher/groups/{}/statistics/export - Exporting group statistics", groupId);

        byte[] csvData = exportGroupStatisticsUseCase.execute(groupId, userPrincipal.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "group-" + groupId + "-statistics.csv");
        headers.setContentLength(csvData.length);

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}