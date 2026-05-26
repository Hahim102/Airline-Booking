package com.example.service.util;

import com.lowagie.text.Paragraph;
import com.lowagie.text.Element;
import com.lowagie.text.Cell;
import com.lowagie.text.Phrase;
import java.awt.Color;

public class PdfStyleProvider {

    public static final Color HEADER_BACKGROUND_COLOR = new Color(52, 120, 246);


    public static Paragraph createCenteredParagraph(String text, com.lowagie.text.Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    public static Paragraph createTitleParagraph(String text) {
        Paragraph paragraph = createCenteredParagraph(text, ExportFontProvider.createPdfTitleFont());
        paragraph.setSpacingAfter(10);
        return paragraph;
    }

    public static Paragraph createSubTitleParagraph(String text) {
        Paragraph paragraph = createCenteredParagraph(text, ExportFontProvider.createPdfSubTitleFont());
        paragraph.setSpacingAfter(15);
        return paragraph;
    }

    public static Paragraph createInfoParagraph(String text) {
        Paragraph paragraph = new Paragraph(text, ExportFontProvider.createPdfInfoFont());
        paragraph.setSpacingAfter(2);
        return paragraph;
    }

    public static Paragraph createHeadingParagraph(String text) {
        Paragraph paragraph = new Paragraph(text, ExportFontProvider.createPdfHeaderFont());
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    public static Paragraph createSpacerParagraph() {
        Paragraph paragraph = new Paragraph(" ");
        paragraph.setSpacingAfter(10);
        return paragraph;
    }

    public static Cell createHeaderCell(String text) {
        Cell cell = new Cell(new Phrase(text, ExportFontProvider.createPdfHeaderFont()));
        cell.setHeader(true);
        cell.setBackgroundColor(HEADER_BACKGROUND_COLOR);
        return cell;
    }

    public static Cell createDataCell(String text) {
        return new Cell(new Phrase(text, ExportFontProvider.createPdfDataFont()));
    }

    public static void configureTable(com.lowagie.text.Table table) {
        table.setWidth(100);
        table.setPadding(4);
        table.setSpacing(0);
    }
}
