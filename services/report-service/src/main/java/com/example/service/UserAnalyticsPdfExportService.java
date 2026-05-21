package com.example.service;

import com.example.enums.ErrorCode;
import com.example.enums.StatisticType;
import com.example.exception.AppException;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserSummaryResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAnalyticsPdfExportService {

    public byte[] exportUserAnalytics(
            UserSummaryResponse summary,
            List<UserRegistrationStatsResponse> registrations,
            StatisticType type
    ) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            document.add(new Paragraph("USER ANALYTICS REPORT"));
            document.add(new Paragraph("Generated At: " + LocalDateTime.now()));
            document.add(new Paragraph("Statistic Type: " + type.name()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Summary"));
            Table summaryTable = new Table(2);
            summaryTable.setWidth(100);

            summaryTable.addCell("Metric");
            summaryTable.addCell("Value");

            summaryTable.addCell("Total Users");
            summaryTable.addCell(String.valueOf(summary.getTotalUsers()));

            summaryTable.addCell("Active Users");
            summaryTable.addCell(String.valueOf(summary.getActiveUsers()));

            summaryTable.addCell("Inactive Users");
            summaryTable.addCell(String.valueOf(summary.getInactiveUsers()));

            summaryTable.addCell("Deleted Users");
            summaryTable.addCell(String.valueOf(summary.getDeletedUsers()));

            document.add(summaryTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("User Registrations"));
            Table registrationTable = new Table(2);
            registrationTable.setWidth(100);

            registrationTable.addCell("Label");
            registrationTable.addCell("Total Registrations");

            for (UserRegistrationStatsResponse item : registrations) {
                registrationTable.addCell(item.getLabel());
                registrationTable.addCell(String.valueOf(item.getTotal()));
            }

            document.add(registrationTable);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXPORT_FAILED);
        }
    }
}
