package com.rubix.pension.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.rubix.pension.reports.dto.EmployeeBalanceReportResponse;
import com.rubix.pension.reports.dto.EmployeeBalanceRowDto;
import com.rubix.pension.reports.dto.EmployeeBalanceTotalsDto;
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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class EmployeeBalancePdfService {

    /** Access PrtDevMode = Letter; exported mediabox is landscape ~792 × 612. */
    private static final Rectangle PAGE_SIZE = new Rectangle(792.16f, 612.07f);
    private static final float MARGIN_LEFT = 22f;
    private static final float MARGIN_RIGHT = 22f;
    private static final float MARGIN_TOP = 18f;
    private static final float MARGIN_BOTTOM = 36f;
    private static final float CONTENT_WIDTH = PAGE_SIZE.getWidth() - MARGIN_LEFT - MARGIN_RIGHT;

    /** Access detail band = 438 twips ≈ 21.9 pt. */
    private static final float ROW_HEIGHT = 22f;
    private static final float HEADER_ROW_HEIGHT = 14f;
    private static final float TOTALS_ROW_HEIGHT = 16.2f;

    /** Date, Transaction, Contribution x3, Investment Return x4, Accumulated Value x4 */
    private static final float[] COL_WIDTHS = {
            8.2f, 10.2f,
            6.7f, 6.7f, 6.7f,
            6.7f, 6.7f, 6.7f, 7.4f,
            6.7f, 6.7f, 6.7f, 7.4f
    };

    private static final Color ALT_ROW_BG = new Color(230, 230, 231);
    private static final Color HEADER_BG = new Color(245, 245, 245);
    private static final Color SUNKEN_FACE = new Color(252, 252, 252);
    private static final Color SUNKEN_SHADOW = new Color(128, 128, 128);
    private static final Color SUNKEN_HIGHLIGHT = new Color(220, 220, 220);

    private final BaseFont helvetica;
    private final BaseFont helveticaBold;
    private final BaseFont helveticaBoldOblique;
    private final BaseFont arabicRegular;
    private final byte[] logoBytes;
    private final DecimalFormat numberFormat;

    public EmployeeBalancePdfService() {
        try {
            helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            helveticaBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            helveticaBoldOblique = BaseFont.createFont(BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            arabicRegular = loadFont("reports/fonts/NotoSansArabic-Regular.ttf");
            logoBytes = readAllBytes(new ClassPathResource("reports/kaf-logo.jpg"));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load PDF resources", ex);
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        numberFormat = new DecimalFormat("#,##0", symbols);
    }

    public byte[] build(EmployeeBalanceReportResponse report) throws IOException {
        List<EmployeeBalanceRowDto> rows = sortedForPdf(report.getRows());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PAGE_SIZE, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PageNumbers(helveticaBold));
            document.open();

            document.add(buildReportHeader(report));
            document.add(spacer(8f));
            document.add(buildSummaryTotalsTable(report.getTotals()));
            document.add(spacer(8f));
            document.add(buildDetailTable(rows));
            document.add(spacer(12f));
            document.add(buildLegend());

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IOException("Failed to build employee balance PDF", ex);
        }
    }

    public static String fileName(String valuationDate, String employeeNumber) {
        String ymd = valuationDate.replace("-", "");
        return "Balance_Report_" + ymd + "_" + employeeNumber + ".pdf";
    }

    private static List<EmployeeBalanceRowDto> sortedForPdf(List<EmployeeBalanceRowDto> source) {
        List<EmployeeBalanceRowDto> rows = new ArrayList<>(source == null ? List.of() : source);
        rows.sort(Comparator
                .comparing(EmployeeBalancePdfService::retroDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(EmployeeBalanceRowDto::getSerial, Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    private static LocalDate retroDate(EmployeeBalanceRowDto row) {
        String iso = row.getRetroDate();
        if (iso == null || iso.isBlank()) {
            return null;
        }
        String datePart = iso.length() >= 10 ? iso.substring(0, 10) : iso;
        return LocalDate.parse(datePart);
    }

    private PdfPTable buildReportHeader(EmployeeBalanceReportResponse report) throws IOException {
        PdfPTable header = lockedTable(new float[]{56f, CONTENT_WIDTH - 236f, 180f});

        Image logo = Image.getInstance(logoBytes);
        logo.scaleToFit(48f, 39f);
        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        logoCell.setPadding(0);
        logoCell.setPaddingLeft(4f);
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell(new Phrase("Employee Balance Report", font(helveticaBold, 18)));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPadding(0);
        titleCell.setPaddingLeft(8f);
        header.addCell(titleCell);

        PdfPCell dateCell = new PdfPCell(buildDateTimeBlock());
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_TOP);
        dateCell.setPadding(0);
        header.addCell(dateCell);

        PdfPCell metaCell = new PdfPCell(buildMetaBlock(report));
        metaCell.setColspan(3);
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setPadding(0);
        metaCell.setPaddingTop(18f);
        header.addCell(metaCell);

        return header;
    }

    private PdfPTable buildDateTimeBlock() {
        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);
        Font dateFont = font(helvetica, 11);

        PdfPCell day = new PdfPCell(new Phrase(formatToday(), dateFont));
        day.setBorder(Rectangle.NO_BORDER);
        day.setHorizontalAlignment(Element.ALIGN_RIGHT);
        day.setPadding(0);
        day.setPaddingBottom(4f);
        block.addCell(day);

        PdfPCell time = new PdfPCell(new Phrase(formatTime(), dateFont));
        time.setBorder(Rectangle.NO_BORDER);
        time.setHorizontalAlignment(Element.ALIGN_RIGHT);
        time.setPadding(0);
        block.addCell(time);
        return block;
    }

    private PdfPTable buildMetaBlock(EmployeeBalanceReportResponse report) {
        PdfPTable meta = lockedTable(new float[]{134f, CONTENT_WIDTH - 134f});
        Font labelFont = font(helveticaBold, 11);
        Font valueFont = font(helvetica, 11);

        addMetaRow(meta, "Company Number:", nullToEmpty(report.getCompanyNumber()), labelFont, valueFont);
        addNameMetaRow(meta, "Employee Name:", nullToEmpty(report.getFullName()), labelFont, valueFont);
        addMetaRow(meta, "Employee ID:", report.getEmployeeId() == null ? "" : String.valueOf(report.getEmployeeId()),
                labelFont, valueFont);
        addMetaRow(meta, "Valuation Date:", formatAccessDate(report.getValuationDate()), labelFont, valueFont);
        addMetaRow(meta, "Currency:", nullToEmpty(report.getCurrency()), labelFont, valueFont);
        return meta;
    }

    private void addMetaRow(PdfPTable meta, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(3f);
        labelCell.setPaddingBottom(3f);
        labelCell.setPaddingLeft(4f);
        labelCell.setMinimumHeight(21f);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        meta.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingTop(3f);
        valueCell.setPaddingBottom(3f);
        valueCell.setMinimumHeight(21f);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        meta.addCell(valueCell);
    }

    /**
     * Access prints the stored Full_Name as-is. English names use Helvetica;
     * Arabic names use the same Noto + shaping path as report 5.1.
     */
    private void addNameMetaRow(PdfPTable meta, String label, String value, Font labelFont, Font latinFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(3f);
        labelCell.setPaddingBottom(3f);
        labelCell.setPaddingLeft(4f);
        labelCell.setMinimumHeight(21f);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        meta.addCell(labelCell);

        boolean arabic = containsArabic(value);
        String display = arabic ? ArabicTextSupport.forPdf(value) : value;
        Font valueFont = arabic
                ? new Font(arabicRegular, 11, Font.NORMAL, Color.BLACK)
                : latinFont;

        PdfPCell valueCell = new PdfPCell(new Phrase(display, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingTop(3f);
        valueCell.setPaddingBottom(3f);
        valueCell.setMinimumHeight(21f);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        meta.addCell(valueCell);
    }

    private static boolean containsArabic(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(text.charAt(i));
            if (block == Character.UnicodeBlock.ARABIC
                    || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A
                    || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B) {
                return true;
            }
        }
        return false;
    }

    private PdfPTable buildSummaryTotalsTable(EmployeeBalanceTotalsDto totals) {
        PdfPTable table = lockedTable(COL_WIDTHS);
        table.setSplitRows(false);
        table.setSplitLate(false);
        addGroupHeader(table, true);
        addSubHeader(table);
        addTotalsRow(table, totals);
        return table;
    }

    private PdfPTable buildDetailTable(List<EmployeeBalanceRowDto> rows) {
        PdfPTable table = lockedTable(COL_WIDTHS);
        // Let OpenPDF fill each page like Access; repeat column headers on continuation pages.
        table.setHeaderRows(2);
        table.setSplitRows(false);
        table.setSplitLate(false);
        addGroupHeader(table, false);
        addSubHeader(table);
        int index = 0;
        for (EmployeeBalanceRowDto row : rows) {
            addDataRow(table, row, index);
            index += 1;
        }
        return table;
    }

    private void addGroupHeader(PdfPTable table, boolean totalsBand) {
        Font font = headerFont();
        if (totalsBand) {
            table.addCell(headerCell("", font, Element.ALIGN_LEFT, 1, 2, HEADER_ROW_HEIGHT * 2));
            table.addCell(headerCell("", font, Element.ALIGN_LEFT, 1, 2, HEADER_ROW_HEIGHT * 2));
        } else {
            table.addCell(headerCell("Date", font, Element.ALIGN_CENTER, 1, 2, HEADER_ROW_HEIGHT * 2));
            table.addCell(headerCell("Transaction", font, Element.ALIGN_CENTER, 1, 2, HEADER_ROW_HEIGHT * 2));
        }
        table.addCell(headerCell("Contribution", font, Element.ALIGN_CENTER, 3, 1, HEADER_ROW_HEIGHT));
        table.addCell(headerCell("Investment Return", font, Element.ALIGN_CENTER, 4, 1, HEADER_ROW_HEIGHT));
        table.addCell(headerCell("Accumulated Value", font, Element.ALIGN_CENTER, 4, 1, HEADER_ROW_HEIGHT));
    }

    private void addSubHeader(PdfPTable table) {
        Font font = headerFont();
        for (String label : new String[]{
                "EE", "VEE", "ER",
                "EE", "VEE", "ER", "Total",
                "EE", "VEE", "ER", "Total"
        }) {
            table.addCell(headerCell(label, font, Element.ALIGN_CENTER, 1, 1, HEADER_ROW_HEIGHT));
        }
    }

    private void addTotalsRow(PdfPTable table, EmployeeBalanceTotalsDto totals) {
        Font labelFont = font(helveticaBoldOblique, 9);
        Font numFont = font(helveticaBold, 9);
        table.addCell(totalsLabelCell("Totals", labelFont));
        table.addCell(totalsLabelCell("", labelFont));
        addNumericCells(table, numFont, Color.WHITE, true,
                totals.getGrossContributionEe(),
                totals.getGrossContributionVee(),
                totals.getGrossContributionEr(),
                totals.getInvestmentReturnEe(),
                totals.getInvestmentReturnVee(),
                totals.getInvestmentReturnEr(),
                totals.getTotalInvestmentReturn(),
                totals.getInvestmentEe(),
                totals.getInvestmentVee(),
                totals.getInvestmentEr(),
                totals.getInvestmentTotal()
        );
    }

    private void addDataRow(PdfPTable table, EmployeeBalanceRowDto row, int rowIndex) {
        Font textFont = font(helvetica, 10);
        Font numFont = font(helvetica, 10);
        Color bg = zebraBackground(rowIndex);
        table.addCell(dataCell(formatAccessDate(row.getRetroDate()), textFont, Element.ALIGN_LEFT, bg, false));
        table.addCell(dataCell(nullToEmpty(row.getDescription()), textFont, Element.ALIGN_LEFT, bg, false));
        addNumericCells(table, numFont, bg, false,
                row.getGrossContributionEe(),
                row.getGrossContributionVee(),
                row.getGrossContributionEr(),
                row.getInvestmentReturnEe(),
                row.getInvestmentReturnVee(),
                row.getInvestmentReturnEr(),
                row.getTotalInvestmentReturn(),
                row.getInvestmentEe(),
                row.getInvestmentVee(),
                row.getInvestmentEr(),
                row.getInvestmentTotal()
        );
    }

    private void addNumericCells(PdfPTable table, Font font, Color bg, boolean sunken, double... values) {
        for (double value : values) {
            if (sunken) {
                table.addCell(sunkenNumericCell(formatNum(value), font));
            } else {
                table.addCell(dataCell(formatNum(value), font, Element.ALIGN_RIGHT, bg, true));
            }
        }
    }

    private PdfPCell headerCell(String text, Font font, int align, int colspan, int rowspan, float height) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        cell.setFixedHeight(height);
        cell.setBackgroundColor(HEADER_BG);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell totalsLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        cell.setFixedHeight(TOTALS_ROW_HEIGHT);
        cell.setBackgroundColor(Color.WHITE);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell sunkenNumericCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setFixedHeight(TOTALS_ROW_HEIGHT);
        cell.setNoWrap(true);
        cell.setBackgroundColor(SUNKEN_FACE);
        cell.setBorderWidth(1f);
        cell.setBorderColor(SUNKEN_SHADOW);
        cell.setCellEvent((cell1, position, canvases) -> {
            float x1 = position.getLeft();
            float y1 = position.getBottom();
            float x2 = position.getRight();
            float y2 = position.getTop();
            canvases[PdfPTable.LINECANVAS].setLineWidth(0.6f);
            canvases[PdfPTable.LINECANVAS].setColorStroke(SUNKEN_HIGHLIGHT);
            canvases[PdfPTable.LINECANVAS].moveTo(x1, y1);
            canvases[PdfPTable.LINECANVAS].lineTo(x2, y1);
            canvases[PdfPTable.LINECANVAS].lineTo(x2, y2);
            canvases[PdfPTable.LINECANVAS].stroke();
        });
        return cell;
    }

    private PdfPCell dataCell(String text, Font font, int align, Color background, boolean numeric) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setFixedHeight(ROW_HEIGHT);
        cell.setNoWrap(numeric);
        cell.setBackgroundColor(background);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private static Color zebraBackground(int rowIndex) {
        return rowIndex % 2 == 0 ? Color.WHITE : ALT_ROW_BG;
    }

    private Font headerFont() {
        return font(helveticaBoldOblique, 10);
    }

    private PdfPTable buildLegend() {
        PdfPTable footer = lockedTable(new float[]{1f});
        Font legendFont = font(helvetica, 12);
        Phrase legend = new Phrase();
        legend.add(new Phrase("EE   = Employee Fund\n", legendFont));
        legend.add(new Phrase("VEE = Voluntary Employee Fund\n", legendFont));
        legend.add(new Phrase("ER   = Employer Fund", legendFont));
        PdfPCell legendCell = new PdfPCell(legend);
        legendCell.setBorder(Rectangle.NO_BORDER);
        legendCell.setPadding(0);
        footer.addCell(legendCell);
        return footer;
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
        return numberFormat.format(Math.round(value));
    }

    private static String formatAccessDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "";
        }
        String datePart = iso.length() >= 10 ? iso.substring(0, 10) : iso;
        LocalDate date = LocalDate.parse(datePart);
        return date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear();
    }

    private static String formatToday() {
        LocalDateTime now = LocalDateTime.now();
        String weekday = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
        String month = now.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
        return weekday + ", " + month + " " + now.getDayOfMonth() + ", " + now.getYear();
    }

    private static String formatTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US));
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

    /**
     * Access-style "Page X of Y". Total must use the last drawn page; after close()
     * writer.getPageNumber() is already incremented and would show N+1.
     */
    private static final class PageNumbers extends PdfPageEventHelper {
        private final BaseFont font;
        private PdfTemplate totalPages;
        private int lastPage;

        private PageNumbers(BaseFont font) {
            this.font = font;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(40, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            lastPage = writer.getPageNumber();
            PdfContentByte cb = writer.getDirectContent();
            String text = "Page " + lastPage + " of ";
            float size = 10f;
            float textWidth = font.getWidthPoint(text, size);
            float x = document.right();
            float y = document.bottom() - 16f;
            cb.beginText();
            cb.setFontAndSize(font, size);
            cb.setTextMatrix(x - textWidth - 18f, y);
            cb.showText(text);
            cb.endText();
            cb.addTemplate(totalPages, x - 18f, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPages.beginText();
            totalPages.setFontAndSize(font, 10f);
            totalPages.showText(String.valueOf(Math.max(lastPage, 1)));
            totalPages.endText();
        }
    }
}
