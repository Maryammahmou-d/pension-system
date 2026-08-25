package com.rubix.pension.employee_company.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rubix.pension.employee_company.dto.TerminationFundEmployeeRow;
import com.rubix.pension.employee_company.dto.TerminationFundReportDto;
import com.rubix.pension.reports.pdf.ArabicTextSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Renders the company-wide, per-fund termination roll-up
 * (Company_Termination_{companyNumber}_F{fund}.pdf). Stateless: built entirely from an
 * already-computed {@link TerminationFundReportDto}. Values are shown as absolute
 * entitlement magnitudes (matching the legacy Access report), not signed transactional deltas.
 */
@Service
public class TerminationFundReportPdfService {

    private static final Rectangle PAGE_SIZE = PageSize.A4.rotate();
    private static final float MARGIN = 28f;
    private static final int ROWS_PER_PAGE = 28;

    private static final float[] COL_WIDTHS = {10f, 24f, 12f, 13f, 13f, 13f, 15f};

    private static final Color HEADER_BG = new Color(245, 245, 245);
    private static final Color ALT_ROW_BG = new Color(240, 240, 240);
    private static final Color TOTALS_BG = new Color(225, 225, 225);

    private final BaseFont helvetica;
    private final BaseFont helveticaBold;
    private final BaseFont arabicRegular;
    private final byte[] logoBytes;
    private final DecimalFormat numberFormat;

    public TerminationFundReportPdfService() {
        try {
            helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            arabicRegular = loadFont("reports/fonts/NotoSansArabic-Regular.ttf");
            logoBytes = readAllBytes(new ClassPathResource("reports/kaf-logo.jpg"));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load PDF resources", ex);
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        numberFormat = new DecimalFormat("#,##0.00", symbols);
    }

    public byte[] build(TerminationFundReportDto fundReport) throws IOException {
        List<TerminationFundEmployeeRow> employees = fundReport.getEmployees() == null
                ? List.of()
                : fundReport.getEmployees();
        List<List<TerminationFundEmployeeRow>> pages = paginate(employees);
        int pageCount = Math.max(pages.size(), 1);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PAGE_SIZE, MARGIN, MARGIN, MARGIN, MARGIN);
            PdfWriter.getInstance(document, out);
            document.open();

            for (int i = 0; i < pages.size(); i += 1) {
                if (i > 0) {
                    document.newPage();
                }
                if (i == 0) {
                    document.add(buildHeader(fundReport));
                    document.add(spacer(10f));
                }
                document.add(buildEmployeeTable(pages.get(i), i == 0));
                if (i == pages.size() - 1) {
                    document.add(spacer(10f));
                    document.add(buildTotalsTable(fundReport));
                }
                document.add(spacer(6f));
                document.add(buildFooter(i + 1, pageCount));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IOException("Failed to build company termination PDF", ex);
        }
    }

    public static String fileName(TerminationFundReportDto fundReport) {
        String companyNumber = fundReport.getCompanyNumber() == null ? "" : fundReport.getCompanyNumber().trim();
        return "Company_Termination_" + companyNumber + "_F" + fundReport.getFund() + ".pdf";
    }

    private PdfPTable buildHeader(TerminationFundReportDto fundReport) throws IOException {
        PdfPTable header = lockedTable(new float[]{18f, 62f, 20f});

        Image logo = Image.getInstance(logoBytes);
        logo.scaleToFit(80f, 50f);
        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(logoCell);

        Font titleFont = font(helveticaBold, 16);
        PdfPCell titleCell = new PdfPCell(new Phrase("Company Termination Balance Report", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(titleCell);

        Font dateFont = font(helvetica, 10);
        PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + formatToday(), dateFont));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(dateCell);

        header.addCell(emptyCell());
        header.addCell(buildMetaBlock(fundReport));
        header.addCell(emptyCell());

        return header;
    }

    private PdfPTable buildMetaBlock(TerminationFundReportDto fundReport) {
        PdfPTable meta = lockedTable(new float[]{1f});
        Font labelFont = font(helveticaBold, 10);
        Font valueFont = font(helvetica, 10);
        Font arabicValueFont = font(arabicRegular, 10);

        meta.addCell(metaLine("Company Number:", nullToEmpty(fundReport.getCompanyNumber()), labelFont, valueFont, false));
        meta.addCell(metaLine("Company Name:", ArabicTextSupport.forPdf(nullToEmpty(fundReport.getCompanyName())), labelFont, arabicValueFont, false));
        meta.addCell(metaLine("Fund:", "Fund " + fundReport.getFund(), labelFont, valueFont, false));
        meta.addCell(metaLine("Unit Price:", formatNum(fundReport.getUnitPrice()), labelFont, valueFont, false));
        meta.addCell(metaLine("Termination Date:", nullToEmpty(fundReport.getTerminationDate()), labelFont, valueFont, false));
        meta.addCell(metaLine("Employees Terminated:", String.valueOf(fundReport.getEmployeeCount()), labelFont, valueFont, false));
        return meta;
    }

    private PdfPCell metaLine(String label, String value, Font labelFont, Font valueFont, boolean rightAlign) {
        Phrase phrase = new Phrase();
        phrase.add(new Phrase(label + "  ", labelFont));
        phrase.add(new Phrase(value, valueFont));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2f);
        cell.setHorizontalAlignment(rightAlign ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPTable buildEmployeeTable(List<TerminationFundEmployeeRow> rows, boolean firstPage) {
        PdfPTable table = lockedTable(COL_WIDTHS);
        table.setHeaderRows(1);
        table.setSplitRows(true);

        Font headerFont = font(helveticaBold, 8.5f);
        for (String label : new String[]{
                "Employee Number", "Full Name", "DOB", "Employee\nFund Value", "Vested Employer\nFund Value",
                "Non-Vested Employer\nFund Value", "Total Value"
        }) {
            table.addCell(headerCell(label, headerFont));
        }

        Font dataFont = font(helvetica, 8.5f);
        Font arabicDataFont = font(arabicRegular, 8.5f);
        int visibleIndex = 0;
        for (TerminationFundEmployeeRow row : rows) {
            Color bg = zebra(visibleIndex);
            double employeeValue = Math.abs(row.getTransactionalEeValue() + row.getTransactionalVeeValue());
            double vestedErValue = Math.abs(row.getTransactionalErValue());
            double nonVestedErValue = Math.abs(row.getTerminatedErValue());
            double totalValue = Math.abs(row.getTransactionalTotalValue());

            table.addCell(dataCell(nullToEmpty(row.getEmployeeNumber()), dataFont, Element.ALIGN_LEFT, bg));
            table.addCell(arabicCell(ArabicTextSupport.forPdf(nullToEmpty(row.getEmployeeName())), arabicDataFont, bg));
            table.addCell(dataCell(nullToEmpty(row.getDob()), dataFont, Element.ALIGN_CENTER, bg));
            table.addCell(dataCell(formatNum(employeeValue), dataFont, Element.ALIGN_RIGHT, bg));
            table.addCell(dataCell(formatNum(vestedErValue), dataFont, Element.ALIGN_RIGHT, bg));
            table.addCell(dataCell(formatNum(nonVestedErValue), dataFont, Element.ALIGN_RIGHT, bg));
            table.addCell(dataCell(formatNum(totalValue), dataFont, Element.ALIGN_RIGHT, bg));
            visibleIndex += 1;
        }

        if (rows.isEmpty() && firstPage) {
            PdfPCell noneCell = new PdfPCell(new Phrase("No employees terminated for this fund.", dataFont));
            noneCell.setColspan(COL_WIDTHS.length);
            noneCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noneCell.setPadding(6f);
            table.addCell(noneCell);
        }

        return table;
    }

    private PdfPTable buildTotalsTable(TerminationFundReportDto fundReport) {
        PdfPTable table = lockedTable(new float[]{34f, 12f, 13f, 13f, 15f});
        Font headerFont = font(helveticaBold, 8.5f);
        for (String label : new String[]{
                "", "Employee\nFund Value", "Vested Employer\nFund Value", "Non-Vested Employer\nFund Value", "Total Value"
        }) {
            table.addCell(headerCell(label, headerFont));
        }

        double employeeValue = Math.abs(fundReport.getTotalTransactionalEeValue() + fundReport.getTotalTransactionalVeeValue());
        double vestedErValue = Math.abs(fundReport.getTotalTransactionalErValue());
        double nonVestedErValue = Math.abs(fundReport.getTotalTerminatedErValue());
        double totalValue = Math.abs(fundReport.getTotalTransactionalValue());

        Font labelFont = font(helveticaBold, 9);
        Font dataFont = font(helvetica, 9);
        table.addCell(totalsLabelCell("Fund " + fundReport.getFund() + " Totals", labelFont));
        table.addCell(totalsValueCell(formatNum(employeeValue), dataFont));
        table.addCell(totalsValueCell(formatNum(vestedErValue), dataFont));
        table.addCell(totalsValueCell(formatNum(nonVestedErValue), dataFont));
        table.addCell(totalsValueCell(formatNum(totalValue), dataFont));
        return table;
    }

    private PdfPTable buildFooter(int pageIndex, int pageCount) {
        PdfPTable footer = lockedTable(new float[]{1f});
        Font font = font(helveticaBold, 9);
        PdfPCell cell = new PdfPCell(new Phrase("Page " + pageIndex + " of " + pageCount, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footer.addCell(cell);
        return footer;
    }

    private List<List<TerminationFundEmployeeRow>> paginate(List<TerminationFundEmployeeRow> rows) {
        List<List<TerminationFundEmployeeRow>> pages = new java.util.ArrayList<>();
        if (rows.isEmpty()) {
            pages.add(List.of());
            return pages;
        }
        int index = 0;
        while (index < rows.size()) {
            int end = Math.min(index + ROWS_PER_PAGE, rows.size());
            pages.add(List.copyOf(rows.subList(index, end)));
            index = end;
        }
        return pages;
    }

    private PdfPCell totalsLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(TOTALS_BG);
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell totalsValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(TOTALS_BG);
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        cell.setBackgroundColor(HEADER_BG);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell dataCell(String text, Font font, int align, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        cell.setBackgroundColor(background);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.4f);
        return cell;
    }

    private PdfPCell arabicCell(String text, Font font, Color background) {
        return dataCell(text, font, Element.ALIGN_RIGHT, background);
    }

    private static Color zebra(int rowIndex) {
        return rowIndex % 2 == 0 ? Color.WHITE : ALT_ROW_BG;
    }

    private PdfPTable lockedTable(float[] relativeWidths) {
        PdfPTable table = new PdfPTable(relativeWidths.length);
        table.setWidthPercentage(100);
        try {
            table.setWidths(relativeWidths);
        } catch (DocumentException ex) {
            throw new IllegalStateException(ex);
        }
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setPadding(0);
        return table;
    }

    private static PdfPCell emptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPTable spacer(float height) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        table.addCell(cell);
        return table;
    }

    private String formatNum(double value) {
        return numberFormat.format(value);
    }

    private static String formatToday() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Font font(BaseFont baseFont, float size) {
        return new Font(baseFont, size);
    }

    private static BaseFont loadFont(String classpathLocation) throws IOException, DocumentException {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        byte[] bytes = readAllBytes(resource);
        return BaseFont.createFont(
                classpathLocation,
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                true,
                bytes,
                null
        );
    }

    private static byte[] readAllBytes(ClassPathResource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return in.readAllBytes();
        }
    }
}
