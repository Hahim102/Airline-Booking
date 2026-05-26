package com.example.service;

import com.example.enums.ErrorCode;
import com.example.enums.StatisticType;
import com.example.exception.AppException;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserSummaryResponse;
import com.example.service.util.PdfStyleProvider;
import com.lowagie.text.Document;
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

            document.add(PdfStyleProvider.createTitleParagraph("USER ANALYTICS REPORT"));
            document.add(PdfStyleProvider.createInfoParagraph("Generated At: " + LocalDateTime.now()));
            document.add(PdfStyleProvider.createInfoParagraph("Statistic Type: " + type.name()));
            document.add(PdfStyleProvider.createSpacerParagraph());

            document.add(PdfStyleProvider.createHeadingParagraph("Summary"));
            Table summaryTable = new Table(2);
            PdfStyleProvider.configureTable(summaryTable);

            summaryTable.addCell(PdfStyleProvider.createHeaderCell("Summary"));
            summaryTable.addCell(PdfStyleProvider.createHeaderCell("Value"));

            summaryTable.addCell(PdfStyleProvider.createDataCell("Total Users"));
            summaryTable.addCell(PdfStyleProvider.createDataCell(String.valueOf(summary.getTotalUsers())));

            summaryTable.addCell(PdfStyleProvider.createDataCell("Active Users"));
            summaryTable.addCell(PdfStyleProvider.createDataCell(String.valueOf(summary.getActiveUsers())));

            summaryTable.addCell(PdfStyleProvider.createDataCell("Inactive Users"));
            summaryTable.addCell(PdfStyleProvider.createDataCell(String.valueOf(summary.getInactiveUsers())));

            summaryTable.addCell(PdfStyleProvider.createDataCell("Deleted Users"));
            summaryTable.addCell(PdfStyleProvider.createDataCell(String.valueOf(summary.getDeletedUsers())));

            document.add(summaryTable);
            document.add(PdfStyleProvider.createSpacerParagraph());

            document.add(PdfStyleProvider.createHeadingParagraph("User Registrations"));
            Table registrationTable = new Table(2);
            PdfStyleProvider.configureTable(registrationTable);

            registrationTable.addCell(PdfStyleProvider.createHeaderCell("Label"));
            registrationTable.addCell(PdfStyleProvider.createHeaderCell("Total Registrations"));

            for (UserRegistrationStatsResponse item : registrations) {
                registrationTable.addCell(PdfStyleProvider.createDataCell(item.getLabel()));
                registrationTable.addCell(PdfStyleProvider.createDataCell(String.valueOf(item.getTotal())));
            }

            document.add(registrationTable);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXPORT_FAILED);
        }
    }
}
