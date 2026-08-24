package com.rubix.pension.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rubix.pension.reports.dto.AggregatedBalanceReportResponse;
import com.rubix.pension.reports.dto.AggregatedBalanceRowDto;
import com.rubix.pension.reports.dto.AggregatedBalanceTotalsDto;
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
import java.util.List;
import java.util.Locale;

@Service
public class AggregatedBalancePdfService {

    /**
     * Access PDF export is A3 landscape (FitToPage). Design width is 21600 twips (15"),
     * but the saved PDF mediabox is ~1190.56 × 842.02.
     */
    private static final Rectangle PAGE_SIZE = new Rectangle(1190.55f, 841.89f);
    private static final float MARGIN_LEFT = 36f;
    private static final float MARGIN_RIGHT = 36f;
    private static final float MARGIN_TOP = 28f;
    private static final float MARGIN_BOTTOM = 32f;

    /** Access: ~23 data rows page 1, 32 on continuation → 210 pages. */
    private static final int ROWS_PAGE1 = 23;
    private static final int ROWS_OTHER = 32;

    /** Access detail band ≈ 448 twips ≈ 22.4 pt. */
    private static final float ROW_HEIGHT = 22.4f;
    private static final float HEADER_ROW_HEIGHT = 14f;
    private static final float TOTALS_ROW_HEIGHT = 16f;

    /** Name ~19.3% (Access name band ~4151/21528), eleven numeric cols share the rest. */
    private static final float[] COL_WIDTHS = {
            19.3f,
            7.336f, 7.336f, 7.336f,
            7.336f, 7.336f, 7.336f, 7.336f,
            7.336f, 7.336f, 7.336f, 7.336f,
    };

    private static final float CONTENT_WIDTH = PAGE_SIZE.getWidth() - MARGIN_LEFT - MARGIN_RIGHT;

    /** Access AlternateBackColor — light grey band on detail rows. */
    private static final Color ALT_ROW_BG = new Color(231, 231, 231);
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

    public AggregatedBalancePdfService() {
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

    public byte[] build(AggregatedBalanceReportResponse report) throws IOException {
        List<AggregatedBalanceRowDto> rows = report.getRows() == null ? List.of() : report.getRows();
        List<PageChunk> pages = paginate(rows);
        int pageCount = Math.max(pages.size(), 1);
        int globalDataRowIndex = 0;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PAGE_SIZE, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
            PdfWriter.getInstance(document, out);
            document.open();

            for (int i = 0; i < pages.size(); i++) {
                if (i > 0) {
                    document.newPage();
                }
                PageChunk chunk = pages.get(i);
                int pageIndex = i + 1;

                if (chunk.first()) {
                    document.add(buildReportHeader(report));
                    document.add(spacer(8f));
                    document.add(buildSummaryTotalsTable(report.getTotals()));
                    document.add(spacer(8f));
                }

                globalDataRowIndex = addDetailTable(document, chunk.rows(), globalDataRowIndex);
                document.add(spacer(8f));
                document.add(buildFooter(pageIndex, pageCount));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IOException("Failed to build aggregated balance PDF", ex);
        }
    }

    public static String fileName(String valuationDate, String companyNumber) {
        String ymd = valuationDate.replace("-", "");
        return "Balance_Report_" + ymd + "_" + companyNumber + ".pdf";
    }

    private PdfPTable buildReportHeader(AggregatedBalanceReportResponse report) throws IOException {
        PdfPTable header = lockedTable(new float[]{92f, CONTENT_WIDTH - 260f, 168f});

        Image logo = Image.getInstance(logoBytes);
        logo.scaleToFit(84f, 54f);
        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        logoCell.setPadding(0);
        logoCell.setRowspan(2);
        header.addCell(logoCell);

        Font titleFont = font(helveticaBold, 20);
        PdfPCell titleCell = new PdfPCell(new Phrase("Aggregate Employee Balance Report", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        titleCell.setPadding(0);
        titleCell.setPaddingBottom(8f);
        header.addCell(titleCell);

        PdfPCell dateCell = new PdfPCell(buildDateTimeBlock());
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        dateCell.setPadding(0);
        dateCell.setPaddingBottom(8f);
        header.addCell(dateCell);

        PdfPCell metaCell = new PdfPCell(buildMetaBlock(report));
        metaCell.setColspan(2);
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaCell.setVerticalAlignment(Element.ALIGN_TOP);
        metaCell.setPadding(0);
        metaCell.setPaddingTop(6f);
        header.addCell(metaCell);

        return header;
    }

    /** One row per line so Access-like vertical gap is preserved (Phrase +"\\n" packs too tightly). */
    private PdfPTable buildMetaBlock(AggregatedBalanceReportResponse report) {
        PdfPTable meta = new PdfPTable(1);
        meta.setWidthPercentage(100);
        meta.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        meta.getDefaultCell().setPadding(0);

        Font labelFont = font(helveticaBold, 10);
        Font valueFont = font(helvetica, 10);
        meta.addCell(metaLine("Company Number:", nullToEmpty(report.getCompanyNumber()), labelFont, valueFont));
        meta.addCell(metaLine("Valuation Date:", formatAccessDate(report.getValuationDate()), labelFont, valueFont));
        meta.addCell(metaLine("Currency:", nullToEmpty(report.getCurrency()), labelFont, valueFont));
        return meta;
    }

    private PdfPCell metaLine(String label, String value, Font labelFont, Font valueFont) {
        Phrase phrase = new Phrase();
        phrase.add(new Phrase(label + "  ", labelFont));
        phrase.add(new Phrase(value, valueFont));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(0);
        cell.setPaddingRight(0);
        cell.setMinimumHeight(16f);
        return cell;
    }

    private PdfPTable buildDateTimeBlock() {
        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);
        Font dateFont = font(helvetica, 11);

        PdfPCell day = new PdfPCell(new Phrase(formatToday(), dateFont));
        day.setBorder(Rectangle.NO_BORDER);
        day.setHorizontalAlignment(Element.ALIGN_RIGHT);
        day.setPaddingTop(2f);
        day.setPaddingBottom(3f);
        day.setMinimumHeight(15f);
        block.addCell(day);

        PdfPCell time = new PdfPCell(new Phrase(formatTime(), dateFont));
        time.setBorder(Rectangle.NO_BORDER);
        time.setHorizontalAlignment(Element.ALIGN_RIGHT);
        time.setPaddingTop(2f);
        time.setPaddingBottom(2f);
        time.setMinimumHeight(15f);
        block.addCell(time);
        return block;
    }

    /** Page-1 summary band — Access has no "Name" label, only Contribution / Return / Accumulated groups. */
    private PdfPTable buildSummaryTotalsTable(AggregatedBalanceTotalsDto totals) {
        PdfPTable table = lockedTable(COL_WIDTHS);
        table.setHeaderRows(2);
        table.setSplitRows(false);
        table.setSplitLate(false);

        addSummaryGroupHeader(table);
        addSubHeader(table);
        addTotalsRow(table, totals);
        return table;
    }

    private int addDetailTable(Document document, List<AggregatedBalanceRowDto> rows, int globalDataRowIndex)
            throws DocumentException {
        if (rows.isEmpty()) {
            return globalDataRowIndex;
        }
        PdfPTable table = lockedTable(COL_WIDTHS);
        table.setHeaderRows(2);
        table.setSplitRows(false);
        table.setSplitLate(false);

        addDetailGroupHeader(table);
        addSubHeader(table);

        for (AggregatedBalanceRowDto row : rows) {
            addDataRow(table, row, globalDataRowIndex);
            globalDataRowIndex += 1;
        }
        document.add(table);
        return globalDataRowIndex;
    }

    private void addSummaryGroupHeader(PdfPTable table) {
        Font font = headerFont();
        table.addCell(headerCell("", font, Element.ALIGN_CENTER, 1, 2, HEADER_ROW_HEIGHT * 2));
        table.addCell(headerCell("Contribution", font, Element.ALIGN_CENTER, 3, 1, HEADER_ROW_HEIGHT));
        table.addCell(headerCell("Investment Return", font, Element.ALIGN_CENTER, 4, 1, HEADER_ROW_HEIGHT));
        table.addCell(headerCell("Accumulated Value", font, Element.ALIGN_CENTER, 4, 1, HEADER_ROW_HEIGHT));
    }

    private void addDetailGroupHeader(PdfPTable table) {
        Font font = headerFont();
        table.addCell(headerCell("Name", font, Element.ALIGN_CENTER, 1, 2, HEADER_ROW_HEIGHT * 2));
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

    private void addTotalsRow(PdfPTable table, AggregatedBalanceTotalsDto totals) {
        Font labelFont = font(helveticaBoldOblique, 10);
        Font numFont = font(helveticaBold, 10);
        table.addCell(totalsLabelCell("Totals", labelFont));
        addTotalsNumericCells(table, numFont,
                totals.getGrossContributionEe(),
                totals.getGrossContributionVee(),
                totals.getGrossContributionEr(),
                totals.getInvestmentReturnEe(),
                totals.getInvestmentReturnVee(),
                totals.getInvestmentReturnEr(),
                totals.getTotalInvestmentReturn(),
                totals.getAccumulatedValueEe(),
                totals.getAccumulatedValueVee(),
                totals.getAccumulatedValueEr(),
                totals.getAccumulatedValueTotal()
        );
    }

    private void addDataRow(PdfPTable table, AggregatedBalanceRowDto row, int rowIndex) {
        Font nameFont = font(arabicRegular, 9);
        Font numFont = font(helvetica, 9);
        Color bg = zebraBackground(rowIndex);
        table.addCell(dataCell(
                ArabicTextSupport.forPdf(nullToEmpty(row.getFullName())),
                nameFont,
                Element.ALIGN_RIGHT,
                bg,
                false
        ));
        addNumericCells(table, numFont, bg, false,
                row.getGrossContributionEe(),
                row.getGrossContributionVee(),
                row.getGrossContributionEr(),
                row.getInvestmentReturnEe(),
                row.getInvestmentReturnVee(),
                row.getInvestmentReturnEr(),
                row.getTotalInvestmentReturn(),
                row.getAccumulatedValueEe(),
                row.getAccumulatedValueVee(),
                row.getAccumulatedValueEr(),
                row.getAccumulatedValueTotal()
        );
    }

    private void addTotalsNumericCells(PdfPTable table, Font font, double... values) {
        for (double value : values) {
            table.addCell(sunkenNumericCell(formatNum(value), font));
        }
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
        cell.setBorderWidth(0.75f);
        return cell;
    }

    private PdfPCell totalsLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        cell.setFixedHeight(TOTALS_ROW_HEIGHT);
        cell.setBackgroundColor(Color.WHITE);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.75f);
        return cell;
    }

    /** Access-style recessed numeric field on the totals row. */
    private PdfPCell sunkenNumericCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
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

    private PdfPTable buildFooter(int pageIndex, int pageCount) {
        PdfPTable footer = lockedTable(new float[]{3f, 1f});
        Font legendFont = font(helvetica, 9);
        if (pageIndex == pageCount) {
            Phrase legend = new Phrase();
            legend.add(new Phrase("EE  = Employee Fund\n", legendFont));
            legend.add(new Phrase("VEE = Voluntary Employee Fund\n", legendFont));
            legend.add(new Phrase("ER  = Employer Fund", legendFont));
            PdfPCell legendCell = new PdfPCell(legend);
            legendCell.setBorder(Rectangle.NO_BORDER);
            legendCell.setPadding(0);
            footer.addCell(legendCell);
        } else {
            footer.addCell(emptyCell());
        }

        Font pageFont = font(helveticaBold, 10);
        PdfPCell pageCell = new PdfPCell(new Phrase("Page " + pageIndex + " of " + pageCount, pageFont));
        pageCell.setBorder(Rectangle.NO_BORDER);
        pageCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pageCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        pageCell.setPadding(0);
        footer.addCell(pageCell);
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

    private static PdfPCell emptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
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

    private static List<PageChunk> paginate(List<AggregatedBalanceRowDto> rows) {
        List<PageChunk> pages = new ArrayList<>();
        if (rows.isEmpty()) {
            pages.add(new PageChunk(true, List.of()));
            return pages;
        }
        int index = 0;
        int firstEnd = Math.min(ROWS_PAGE1, rows.size());
        pages.add(new PageChunk(true, List.copyOf(rows.subList(index, firstEnd))));
        index = firstEnd;
        while (index < rows.size()) {
            int end = Math.min(index + ROWS_OTHER, rows.size());
            pages.add(new PageChunk(false, List.copyOf(rows.subList(index, end))));
            index = end;
        }
        return pages;
    }

    private String formatNum(double value) {
        return numberFormat.format(Math.round(value));
    }

    private static String formatAccessDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "";
        }
        LocalDate date = LocalDate.parse(iso);
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

    private record PageChunk(boolean first, List<AggregatedBalanceRowDto> rows) {
    }
}
