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
import com.rubix.pension.employee_company.dto.TerminationReportDto;
import com.rubix.pension.employee_company.dto.TerminationReportFundRow;
import com.rubix.pension.reports.pdf.ArabicTextSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Renders the per-employee termination statement (Termination_{employeeNumber}.pdf), matching
 * the legacy Access "Create Employee Termination Report" (3 pages: Termination Summary,
 * Fund Manager Movement Summary [units], Funds [values]). Stateless: built entirely from an
 * already-computed {@link TerminationReportDto}.
 *
 * <p>Note: the "Client Summary" totals for lifetime Contribution / Top Up / Withdrawal amounts
 * and their charges, Admin Charges, and IMC are not modelled by {@link TerminationReportDto}
 * (they require aggregating the employee's full transaction history, not just the termination
 * transaction) and are rendered as 0.00 placeholders. Starting Funds, Transactional Value,
 * Terminated ER Value, and Surrender Charges are all derived from real termination data.</p>
 */
@Service
public class TerminationReportPdfService {

    private static final Rectangle PAGE_SIZE = PageSize.A4;
    private static final float MARGIN_LEFT = 40f;
    private static final float MARGIN_RIGHT = 40f;
    private static final float MARGIN_TOP = 36f;
    private static final float MARGIN_BOTTOM = 36f;
    private static final float CONTENT_WIDTH = PAGE_SIZE.getWidth() - MARGIN_LEFT - MARGIN_RIGHT;

    private static final Color BOX_BORDER = new Color(150, 150, 150);

    private final BaseFont helvetica;
    private final BaseFont helveticaBold;
    private final BaseFont arabicRegular;
    private final byte[] logoBytes;
    private final DecimalFormat numberFormat;

    public TerminationReportPdfService() {
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

    public byte[] build(TerminationReportDto report) throws IOException {
        List<TerminationReportFundRow> rows = new java.util.ArrayList<>(
                report.getRows() == null ? List.of() : report.getRows());
        while (rows.size() < 10) {
            rows.add(new TerminationReportFundRow());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PAGE_SIZE, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
            PdfWriter.getInstance(document, out);
            document.open();

            buildSummaryPage(document, report, rows);
            document.newPage();
            buildFundGridPage(document, "Fund Manager Movement Summary", rows, true);
            document.newPage();
            buildFundGridPage(document, "Funds", rows, false);
            addSurrenderChargesSection(document, report);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IOException("Failed to build termination report PDF", ex);
        }
    }

    public static String fileName(TerminationReportDto report) {
        String employeeNumber = report.getEmployeeNumber() == null ? "" : report.getEmployeeNumber().trim();
        return "Termination_Report_" + employeeNumber + ".pdf";
    }

    // ---- Page 1: Termination Summary ----------------------------------------------------

    private void buildSummaryPage(Document document, TerminationReportDto report, List<TerminationReportFundRow> rows)
            throws DocumentException, IOException {
        document.add(buildTitleHeader());
        document.add(spacer(6f));
        document.add(buildAddressAndInfoBlock(report));
        document.add(spacer(14f));
        document.add(buildClientSummaryHeader(report));
        document.add(spacer(4f));
        document.add(buildClientSummaryTable(report, rows));
        document.add(spacer(8f));
        document.add(noteParagraph());
    }

    private PdfPTable buildTitleHeader() throws IOException {
        PdfPTable header = lockedTable(new float[]{20f, 60f, 20f});

        Image logo = Image.getInstance(logoBytes);
        logo.scaleToFit(70f, 44f);
        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        header.addCell(logoCell);

        Font titleFont = font(helveticaBold, 18);
        PdfPCell titleCell = new PdfPCell(new Phrase("Termination Summary", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_TOP);
        header.addCell(titleCell);

        Phrase datePhrase = new Phrase();
        datePhrase.add(new Phrase("Date  ", font(helvetica, 9)));
        datePhrase.add(new Phrase("       " + formatToday(), font(helvetica, 9)));
        PdfPCell dateCell = new PdfPCell(datePhrase);
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_TOP);
        header.addCell(dateCell);

        return header;
    }

    private PdfPTable buildAddressAndInfoBlock(TerminationReportDto report) {
        PdfPTable outer = lockedTable(new float[]{40f, 60f});

        PdfPTable left = innerTable(new float[]{1f});
        Font labelFont = font(helveticaBold, 9);
        Font valueFont = font(helvetica, 9);
        Font arabicFont = font(arabicRegular, 9);
        left.addCell(plainCell("Address", labelFont, Element.ALIGN_LEFT, false));
        left.addCell(plainCell(ArabicTextSupport.forPdf(nullToEmpty(report.getCompanyAddress())), arabicFont, Element.ALIGN_LEFT, false));
        left.addCell(plainCell(" ", valueFont, Element.ALIGN_LEFT, false));
        left.addCell(plainCell("Phone Number", labelFont, Element.ALIGN_LEFT, false));
        left.addCell(plainCell(nullToEmpty(report.getCompanyPhone()), valueFont, Element.ALIGN_LEFT, false));
        PdfPCell leftCell = new PdfPCell(left);
        leftCell.setBorder(Rectangle.NO_BORDER);
        outer.addCell(leftCell);

        PdfPTable right = innerTable(new float[]{35f, 65f});
        addLabelValueRow(right, "For:", ArabicTextSupport.forPdf(nullToEmpty(report.getEmployeeName())), labelFont, arabicFont, true);
        addLabelValueRow(right, "National ID:", nullToEmpty(report.getNationalId()), labelFont, valueFont, false);
        addLabelValueRow(right, "Contract Number:", nullToEmpty(report.getCompanyNumber()), labelFont, valueFont, false);
        addLabelValueRow(right, "Company Name:", ArabicTextSupport.forPdf(nullToEmpty(report.getCompanyName())), labelFont, arabicFont, true);
        addLabelValueRow(right, "Category:", nullToEmpty(report.getCategory()), labelFont, valueFont, false);
        addLabelValueRow(right, "Pension Start Date:", formatDisplayDate(report.getPensionStartDate()), labelFont, valueFont, false);
        addLabelValueRow(right, "Termination Date:", formatDisplayDate(report.getTerminationDate()), labelFont, valueFont, false);
        addLabelValueRow(right, "Currency:", nullToEmpty(report.getCurrency()), labelFont, valueFont, false);
        PdfPCell rightCell = new PdfPCell(right);
        rightCell.setBorder(Rectangle.NO_BORDER);
        outer.addCell(rightCell);

        return outer;
    }

    private void addLabelValueRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, boolean rightAlignValue) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(2f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(rightAlignValue ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        valueCell.setPadding(2f);
        table.addCell(valueCell);
    }

    private PdfPTable buildClientSummaryHeader(TerminationReportDto report) {
        PdfPTable table = lockedTable(new float[]{50f, 50f});
        PdfPCell label = new PdfPCell(new Phrase("Client Summary:", font(helveticaBold, 11)));
        label.setBorder(Rectangle.NO_BORDER);
        table.addCell(label);
        PdfPCell currency = new PdfPCell(new Phrase(nullToEmpty(report.getCurrency()), font(helveticaBold, 11)));
        currency.setBorder(Rectangle.NO_BORDER);
        currency.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(currency);
        return table;
    }

    private PdfPTable buildClientSummaryTable(TerminationReportDto report, List<TerminationReportFundRow> rows) {
        PdfPTable table = lockedTable(new float[]{34f, 22f, 22f, 22f});
        Font headerFont = font(helveticaBold, 9);
        table.addCell(plainCell("", headerFont, Element.ALIGN_LEFT, false));
        table.addCell(plainCell("Employee", headerFont, Element.ALIGN_CENTER, false));
        table.addCell(plainCell("Voluntary Employee", headerFont, Element.ALIGN_CENTER, false));
        table.addCell(plainCell("Employer", headerFont, Element.ALIGN_CENTER, false));

        double startingEe = sumValue(rows, TerminationReportFundRow::getStartingEeUnits);
        double startingVee = sumValue(rows, TerminationReportFundRow::getStartingVeeUnits);
        double startingEr = sumValue(rows, TerminationReportFundRow::getStartingErUnits);

        addSummaryRow(table, "Starting Funds", startingEe, startingVee, startingEr, true, true, true);
        addSummaryRow(table, "Total Contribution", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Total Top Up", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Total Withdrawal", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Total Contribution Charges", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Total Top Up Charges", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Total Withdrawal Charges", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Total Admin Charges", 0, 0, 0, true, false, true);
        addSummaryRow(table, "Total IMC", 0, 0, 0, true, true, true);
        addSummaryRow(table, "Transactional Value",
                report.getTotalTransactionalEeValue(), report.getTotalTransactionalVeeValue(), report.getTotalTransactionalErValue(),
                true, true, true);
        addSummaryRow(table, "Terminated ER Value (Non-vested)", 0, 0, report.getTotalTerminatedErValue(), false, false, true);
        addSummaryRow(table, "Surrender Charges", report.getSurrenderChargesEe(), report.getSurrenderChargesVee(), report.getSurrenderChargesEr(),
                true, true, true);

        return table;
    }

    private void addSummaryRow(
            PdfPTable table, String label,
            double employee, double voluntaryEmployee, double employer,
            boolean showEmployee, boolean showVoluntary, boolean showEmployer
    ) {
        Font labelFont = font(helveticaBold, 9);
        Font valueFont = font(helvetica, 9);
        table.addCell(plainCell(label, labelFont, Element.ALIGN_LEFT, false));
        table.addCell(showEmployee ? boxedNumberCell(formatNum(employee), valueFont) : blankCell());
        table.addCell(showVoluntary ? boxedNumberCell(formatNum(voluntaryEmployee), valueFont) : blankCell());
        table.addCell(showEmployer ? boxedNumberCell(formatNum(employer), valueFont) : blankCell());
    }

    private Phrase noteParagraph() {
        return new Phrase("Note: Surrender charges are NOT deducted from the above fund movement", font(helveticaBold, 8.5f));
    }

    // ---- Pages 2 & 3: Fund grids ----------------------------------------------------------

    private void buildFundGridPage(Document document, String title, List<TerminationReportFundRow> rows, boolean units)
            throws DocumentException {
        PdfPCell titleCell = new PdfPCell(new Phrase(title, font(helveticaBold, 14)));
        titleCell.setBorder(Rectangle.NO_BORDER);
        PdfPTable titleTable = lockedTable(new float[]{1f});
        titleTable.addCell(titleCell);
        document.add(titleTable);
        document.add(spacer(10f));

        document.add(buildFundGrid(rows.subList(0, 5), 1, units));
        document.add(spacer(20f));
        document.add(buildFundGrid(rows.subList(5, 10), 6, units));

        if (!units) {
            document.add(spacer(10f));
            document.add(noteParagraph());
        }
    }

    private PdfPTable buildFundGrid(List<TerminationReportFundRow> fundRows, int startFund, boolean units) {
        PdfPTable table = lockedTable(new float[]{22f, 15.6f, 15.6f, 15.6f, 15.6f, 15.6f});
        Font headerFont = font(helveticaBold, 9);
        Font labelFont = font(helveticaBold, 9);
        Font valueFont = font(helvetica, 9);

        table.addCell(plainCell("", headerFont, Element.ALIGN_LEFT, false));
        for (int i = 0; i < fundRows.size(); i += 1) {
            table.addCell(plainCell("Fund " + (startFund + i), headerFont, Element.ALIGN_CENTER, false));
        }

        if (units) {
            addFundValueRow(table, "EE Units", fundRows, TerminationReportFundRow::getTransactionalEeUnits, labelFont, valueFont);
            addFundValueRow(table, "VEE Units", fundRows, TerminationReportFundRow::getTransactionalVeeUnits, labelFont, valueFont);
            addFundValueRow(table, "ER Units (Vested)", fundRows, TerminationReportFundRow::getTransactionalErUnits, labelFont, valueFont);
            addFundValueRow(table, "ER Units (Non-vested)", fundRows, TerminationReportFundRow::getTerminatedErUnits, labelFont, valueFont);
            addBlankRow(table, fundRows.size());
            addFundValueRow(table, "Total Units", fundRows, TerminationReportFundRow::getTransactionalTotalUnits, labelFont, valueFont);
            addFundValueRow(table, "Unit Price", fundRows, TerminationReportFundRow::getUnitPrice, labelFont, valueFont);
        } else {
            addFundValueRow(table, "EE Value", fundRows, TerminationReportFundRow::getTransactionalEeValue, labelFont, valueFont);
            addFundValueRow(table, "VEE Value", fundRows, TerminationReportFundRow::getTransactionalVeeValue, labelFont, valueFont);
            addFundValueRow(table, "ER Value (Vested)", fundRows, TerminationReportFundRow::getTransactionalErValue, labelFont, valueFont);
            addFundValueRow(table, "ER Value (Non-vested)", fundRows, TerminationReportFundRow::getTerminatedErValue, labelFont, valueFont);
            addBlankRow(table, fundRows.size());
            addFundValueRow(table, "Total Value", fundRows, TerminationReportFundRow::getTransactionalTotalValue, labelFont, valueFont);
        }

        return table;
    }

    private void addFundValueRow(
            PdfPTable table, String label, List<TerminationReportFundRow> fundRows,
            java.util.function.ToDoubleFunction<TerminationReportFundRow> accessor,
            Font labelFont, Font valueFont
    ) {
        table.addCell(plainCell(label, labelFont, Element.ALIGN_LEFT, false));
        for (TerminationReportFundRow row : fundRows) {
            table.addCell(boxedNumberCell(formatNum(accessor.applyAsDouble(row)), valueFont));
        }
    }

    private void addBlankRow(PdfPTable table, int fundCount) {
        table.addCell(plainCell("", font(helvetica, 4), Element.ALIGN_LEFT, false));
        for (int i = 0; i < fundCount; i += 1) {
            table.addCell(plainCell("", font(helvetica, 4), Element.ALIGN_LEFT, false));
        }
    }

    private void addSurrenderChargesSection(Document document, TerminationReportDto report) throws DocumentException {
        document.add(spacer(16f));
        PdfPCell titleCell = new PdfPCell(new Phrase("Surrender Charges", font(helveticaBold, 12)));
        titleCell.setBorder(Rectangle.NO_BORDER);
        PdfPTable titleTable = lockedTable(new float[]{1f});
        titleTable.addCell(titleCell);
        document.add(titleTable);
        document.add(spacer(6f));

        PdfPTable table = lockedTable(new float[]{35f, 25f, 40f});
        Font labelFont = font(helveticaBold, 9);
        Font valueFont = font(helvetica, 9);
        table.addCell(plainCell("Surrender Charges EE", labelFont, Element.ALIGN_LEFT, false));
        table.addCell(boxedNumberCell(formatNum(report.getSurrenderChargesEe()), valueFont));
        table.addCell(plainCell("", labelFont, Element.ALIGN_LEFT, false));
        table.addCell(plainCell("Surrender Charges VEE", labelFont, Element.ALIGN_LEFT, false));
        table.addCell(boxedNumberCell(formatNum(report.getSurrenderChargesVee()), valueFont));
        table.addCell(plainCell("", labelFont, Element.ALIGN_LEFT, false));
        table.addCell(plainCell("Surrender Charges ER", labelFont, Element.ALIGN_LEFT, false));
        table.addCell(boxedNumberCell(formatNum(report.getSurrenderChargesEr()), valueFont));
        table.addCell(plainCell("", labelFont, Element.ALIGN_LEFT, false));
        document.add(table);
    }

    // ---- Shared cell/table helpers ---------------------------------------------------------

    private double sumValue(List<TerminationReportFundRow> rows, java.util.function.ToDoubleFunction<TerminationReportFundRow> unitsAccessor) {
        double total = 0;
        for (TerminationReportFundRow row : rows) {
            total += unitsAccessor.applyAsDouble(row) * row.getUnitPrice();
        }
        return total;
    }

    private PdfPCell plainCell(String text, Font font, int align, boolean noBreak) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        cell.setNoWrap(noBreak);
        return cell;
    }

    private PdfPCell blankCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell boxedNumberCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setBackgroundColor(Color.WHITE);
        cell.setBorderColor(BOX_BORDER);
        cell.setBorderWidth(0.75f);
        return cell;
    }

    private PdfPTable lockedTable(float[] relativeWidths) {
        PdfPTable table = new PdfPTable(relativeWidths.length);
        table.setTotalWidth(CONTENT_WIDTH);
        table.setLockedWidth(true);
        try {
            table.setWidths(relativeWidths);
        } catch (DocumentException ex) {
            throw new IllegalStateException(ex);
        }
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setPadding(0);
        return table;
    }

    /** For tables nested inside a PdfPCell — sized relative to the parent cell, not the page. */
    private PdfPTable innerTable(float[] relativeWidths) {
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

    private static PdfPTable spacer(float height) {
        PdfPTable table = new PdfPTable(1);
        table.setTotalWidth(CONTENT_WIDTH);
        table.setLockedWidth(true);
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
        LocalDate today = LocalDate.now();
        return today.getMonthValue() + "/" + today.getDayOfMonth() + "/" + today.getYear();
    }

    private static String formatDisplayDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(iso);
            return date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear();
        } catch (Exception ex) {
            return iso;
        }
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
