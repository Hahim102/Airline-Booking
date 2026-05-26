package com.example.controller;

import com.example.client.AnalyticsClient;
import com.example.client.UserClient;
import com.example.config.UserPrincipal;
import com.example.enums.StatisticType;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserResponse;
import com.example.payload.response.UserSummaryResponse;
import com.example.service.UserAnalyticsExcelExportService;
import com.example.service.UserAnalyticsPdfExportService;
import com.example.service.UserExcelExportService;
import com.example.service.UserPdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class UserExportController {
    private final UserClient userClient;
    private final AnalyticsClient analyticsClient;
    private final UserExcelExportService excelExportUsersService;
    private final UserPdfExportService pdfExportUsersService;
    private final UserAnalyticsExcelExportService excelExportAnalyticsService;
    private final UserAnalyticsPdfExportService pdfExportAnalyticsService;

    @GetMapping("/users/excel")
    public ResponseEntity<byte[]> exportUsersToExcel() {
        List<UserResponse> users = userClient
                .getUsersForExport()
                .getData();

        byte[] file = excelExportUsersService.exportUsers(users);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(file);
    }

    @GetMapping("/users/pdf")
    public ResponseEntity<byte[]> exportUsersToPdf() {
        List<UserResponse> users = userClient
                .getUsersForExport()
                .getData();

        byte[] file = pdfExportUsersService.exportUsers(users);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @GetMapping("/analytics/excel")
    public ResponseEntity<byte[]> exportUserAnalyticsExcel(
            @RequestParam(defaultValue = "DAY") StatisticType type
    ) {
        UserSummaryResponse summary =
                analyticsClient
                        .getUserSummary()
                        .getData();

        List<UserRegistrationStatsResponse> registrations =
                analyticsClient
                        .getUserRegistrations()
                        .getData();

        byte[] file = excelExportAnalyticsService.exportUserAnalytics(summary, registrations, type);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=user-analytics-" + type.name().toLowerCase() + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(file);
    }
    @GetMapping("/analytics/pdf")
    public ResponseEntity<byte[]> exportUserAnalyticsPdf(
            @RequestParam(defaultValue = "DAY") StatisticType type
    ) {
        UserSummaryResponse summary =
                analyticsClient
                        .getUserSummary()
                        .getData();

        List<UserRegistrationStatsResponse> registrations =
                analyticsClient.
                        getUserRegistrations()
                        .getData();

        byte[] file = pdfExportAnalyticsService.exportUserAnalytics(summary, registrations, type);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=user-analytics-" + type.name().toLowerCase() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}
