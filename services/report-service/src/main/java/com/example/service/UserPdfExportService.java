package com.example.service;

import com.example.enums.ErrorCode;
import com.example.exception.AppException;
import com.example.payload.response.UserResponse;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
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

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subTitleFont = new Font(Font.HELVETICA, 13, Font.BOLD);
            Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font dataFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            Paragraph title = new Paragraph("AIRLINE BOOKING SYSTEM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("USER REPORT EXPORT", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(15);
            document.add(subTitle);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            document.add(new Paragraph(
                    "Generated At: " + LocalDateTime.now().format(formatter),
                    infoFont
            ));

            document.add(new Paragraph(
                    "Total Users: " + users.size(),
                    infoFont
            ));

            Paragraph space = new Paragraph(" ");
            space.setSpacingAfter(10);
            document.add(space);

            Table table = new Table(7);
            table.setWidth(100);
            table.setPadding(4);
            table.setSpacing(0);

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
                Cell cell = new Cell(new Phrase(column, headerFont));
                cell.setHeader(true);
                cell.setBackgroundColor(new Color(52, 120, 246));
                table.addCell(cell);
            }

            table.endHeaders();

            for (UserResponse user : users) {
                table.addCell(new Phrase(String.valueOf(user.getId()), dataFont));
                table.addCell(new Phrase(user.getFullName(), dataFont));
                table.addCell(new Phrase(user.getEmail(), dataFont));
                table.addCell(new Phrase(user.getRole().toString(), dataFont));
                table.addCell(new Phrase(user.isActive() ? "Active" : "Inactive", dataFont));
                table.addCell(new Phrase(user.isDeleted() ? "Deleted" : "Not Deleted", dataFont));
                table.addCell(new Phrase(String.valueOf(user.getCreatedAt()), dataFont));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXPORT_FAILED);
        }
    }
}
