package com.example.service;

import com.example.enums.ErrorCode;
import com.example.enums.StatisticType;
import com.example.exception.AppException;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserSummaryResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserAnalyticsExcelExportService {

    public byte[] exportUserAnalytics(
            UserSummaryResponse summary,
            List<UserRegistrationStatsResponse> registrations,
            StatisticType type
    ) {
        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("User Analytics");

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("USER ANALYTICS REPORT");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            Row generatedAtRow = sheet.createRow(2);
            generatedAtRow.createCell(0).setCellValue("Generated At");
            generatedAtRow.createCell(1).setCellValue(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            Row typeRow = sheet.createRow(3);
            typeRow.createCell(0).setCellValue("Statistic Type");
            typeRow.createCell(1).setCellValue(type.name());

            Row summaryHeader = sheet.createRow(5);
            summaryHeader.createCell(0).setCellValue("Summary");
            summaryHeader.getCell(0).setCellStyle(headerStyle);

            Row totalRow = sheet.createRow(6);
            totalRow.createCell(0).setCellValue("Total Users");
            totalRow.createCell(1).setCellValue(summary.getTotalUsers());

            Row activeRow = sheet.createRow(7);
            activeRow.createCell(0).setCellValue("Active Users");
            activeRow.createCell(1).setCellValue(summary.getActiveUsers());

            Row inactiveRow = sheet.createRow(8);
            inactiveRow.createCell(0).setCellValue("Inactive Users");
            inactiveRow.createCell(1).setCellValue(summary.getInactiveUsers());

            Row deletedRow = sheet.createRow(9);
            deletedRow.createCell(0).setCellValue("Deleted Users");
            deletedRow.createCell(1).setCellValue(summary.getDeletedUsers());

            Row tableHeader = sheet.createRow(12);
            String[] columns = {"Label", "Total Registrations"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = tableHeader.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 13;
            for (UserRegistrationStatsResponse item : registrations) {
                Row row = sheet.createRow(rowIndex++);

                Cell labelCell = row.createCell(0);
                labelCell.setCellValue(item.getLabel());
                labelCell.setCellStyle(dataStyle);

                Cell totalCell = row.createCell(1);
                totalCell.setCellValue(item.getTotal());
                totalCell.setCellStyle(dataStyle);
            }

            for (int i = 0; i <= 3; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new AppException(ErrorCode.EXPORT_FAILED);
        }
    }
}
