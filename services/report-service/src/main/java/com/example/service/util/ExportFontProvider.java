package com.example.service.util;

import com.lowagie.text.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import java.awt.Color;

public class ExportFontProvider {

    public static final short TITLE_FONT_SIZE = 18;
    public static final short SUBTITLE_FONT_SIZE = 13;
    public static final short HEADER_FONT_SIZE = 10;
    public static final short DATA_FONT_SIZE = 9;
    public static final short INFO_FONT_SIZE = 10;

    public static org.apache.poi.ss.usermodel.Font createPoiTitleFont(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(TITLE_FONT_SIZE);
        return font;
    }

    public static org.apache.poi.ss.usermodel.Font createPoiSubTitleFont(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(SUBTITLE_FONT_SIZE);
        return font;
    }

    public static org.apache.poi.ss.usermodel.Font createPoiHeaderFont(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints(HEADER_FONT_SIZE);
        return font;
    }

    public static org.apache.poi.ss.usermodel.Font createPoiDataFont(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setFontHeightInPoints(DATA_FONT_SIZE);
        return font;
    }

    public static Font createPdfTitleFont() {
        return new Font(Font.HELVETICA, TITLE_FONT_SIZE, Font.BOLD);
    }

    public static Font createPdfSubTitleFont() {
        return new Font(Font.HELVETICA, SUBTITLE_FONT_SIZE, Font.BOLD);
    }

    public static Font createPdfHeaderFont() {
        return new Font(Font.HELVETICA, HEADER_FONT_SIZE, Font.BOLD, Color.WHITE);
    }

    public static Font createPdfDataFont() {
        return new Font(Font.HELVETICA, DATA_FONT_SIZE, Font.NORMAL);
    }

    public static Font createPdfInfoFont() {
        return new Font(Font.HELVETICA, INFO_FONT_SIZE, Font.NORMAL);
    }
}
