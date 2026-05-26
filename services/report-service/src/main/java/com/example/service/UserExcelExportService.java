package com.example.service;

import com.example.enums.ErrorCode;
import com.example.exception.AppException;
import com.example.payload.response.UserResponse;
import com.example.service.util.ExcelCellStyleProvider;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserExcelExportService {

    public byte[] exportUsers(List<UserResponse> users) {

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet("Users");

            CellStyle titleStyle = ExcelCellStyleProvider.createTitleStyle(workbook);
            CellStyle subTitleStyle = ExcelCellStyleProvider.createSubTitleStyle(workbook);
            CellStyle headerStyle = ExcelCellStyleProvider.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCellStyleProvider.createDataStyle(workbook);

            Row titleRow = sheet.createRow(0);

            Cell titleCell = titleRow.createCell(0);

            titleCell.setCellValue("AIRLINE BOOKING SYSTEM");
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            0,
                            0,
                            0,
                            6
                    )
            );

            Row subTitleRow = sheet.createRow(1);

            Cell subTitleCell = subTitleRow.createCell(0);

            subTitleCell.setCellValue("USER REPORT EXPORT");
            subTitleCell.setCellStyle(subTitleStyle);

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            1,
                            1,
                            0,
                            6
                    )
            );

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Row generatedAtRow = sheet.createRow(3);

            generatedAtRow.createCell(0)
                    .setCellValue("Generated At:");

            generatedAtRow.createCell(1)
                    .setCellValue(
                            LocalDateTime.now().format(formatter)
                    );

            Row totalRow = sheet.createRow(4);

            totalRow.createCell(0)
                    .setCellValue("Total Users:");

            totalRow.createCell(1)
                    .setCellValue(users.size());

            Row header = sheet.createRow(6);

            String[] columns = {
                    "ID",
                    "Full Name",
                    "Email",
                    "Role",
                    "Active",
                    "Deleted",
                    "Created At"
            };

            for (int i = 0; i < columns.length; i++) {

                Cell cell = header.createCell(i);

                cell.setCellValue(columns[i]);

                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 7;

            for (UserResponse user : users) {

                Row row = sheet.createRow(rowIndex++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(user.getId());
                idCell.setCellStyle(dataStyle);

                Cell fullNameCell = row.createCell(1);
                fullNameCell.setCellValue(user.getFullName());
                fullNameCell.setCellStyle(dataStyle);

                Cell emailCell = row.createCell(2);
                emailCell.setCellValue(user.getEmail());
                emailCell.setCellStyle(dataStyle);

                Cell roleCell = row.createCell(3);
                roleCell.setCellValue(user.getRole().toString());
                roleCell.setCellStyle(dataStyle);

                Cell activeCell = row.createCell(4);
                activeCell.setCellValue(user.isActive());
                activeCell.setCellStyle(dataStyle);

                Cell deletedCell = row.createCell(5);
                deletedCell.setCellValue(user.isDeleted());
                deletedCell.setCellStyle(dataStyle);

                Cell createdAtCell = row.createCell(6);
                createdAtCell.setCellValue(
                        String.valueOf(user.getCreatedAt())
                );
                createdAtCell.setCellStyle(dataStyle);
            }

            ExcelCellStyleProvider.autoSizeColumns(sheet, 6);

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new AppException(
                    ErrorCode.EXPORT_FAILED
            );
        }
    }
}