package com.example.service;

import com.example.enums.ErrorCode;
import com.example.exception.AppException;
import com.example.payload.response.UserResponse;
import com.example.service.util.PdfStyleProvider;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserPdfExportService {

    public byte[] exportUsers(List<UserResponse> users) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);

            document.open();

            document.add(PdfStyleProvider.createTitleParagraph("AIRLINE BOOKING SYSTEM"));
            document.add(PdfStyleProvider.createSubTitleParagraph("USER REPORT EXPORT"));

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            document.add(PdfStyleProvider.createInfoParagraph(
                    "Generated At: " + LocalDateTime.now().format(formatter)
            ));

            document.add(PdfStyleProvider.createInfoParagraph(
                    "Total Users: " + users.size()
            ));

            document.add(PdfStyleProvider.createSpacerParagraph());

            Table table = new Table(7);
            PdfStyleProvider.configureTable(table);

            String[] columns = {
                    "ID",
                    "Full Name",
                    "Email",
                    "Role",
                    "Active",
                    "Deleted",
                    "Created At"
            };

            for (String column : columns) {
                table.addCell(PdfStyleProvider.createHeaderCell(column));
            }

            table.endHeaders();

            for (UserResponse user : users) {
                table.addCell(PdfStyleProvider.createDataCell(String.valueOf(user.getId())));
                table.addCell(PdfStyleProvider.createDataCell(user.getFullName()));
                table.addCell(PdfStyleProvider.createDataCell(user.getEmail()));
                table.addCell(PdfStyleProvider.createDataCell(user.getRole().toString()));
                table.addCell(PdfStyleProvider.createDataCell(user.isActive() ? "Active" : "Inactive"));
                table.addCell(PdfStyleProvider.createDataCell(user.isDeleted() ? "Deleted" : "Not Deleted"));
                table.addCell(PdfStyleProvider.createDataCell(String.valueOf(user.getCreatedAt())));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXPORT_FAILED);
        }
    }
}
