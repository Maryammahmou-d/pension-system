package com.rubix.pension.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rubix.pension.reports.dto.DailyMovementRowDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DailyMovementPdfService {

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat UNITS_FMT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat VALUE_FMT = new DecimalFormat("#,##0");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font SECTION_TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font SUBSECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font ROW_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
    private static final Font ROW_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
    private static final Font META_LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
    private static final Font META_VAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);

    private final DailyMovementExcelService excelService;
    private final byte[] logoBytes;

    public DailyMovementPdfService(DailyMovementExcelService excelService) {
        this.excelService = excelService;
        byte[] bytes = null;
        try {
            ClassPathResource res = new ClassPathResource("reports/kaf-logo.jpg");
            if (res.exists()) {
                try (InputStream in = res.getInputStream()) {
                    bytes = in.readAllBytes();
                }
            }
        } catch (Exception ignored) {
        }
        this.logoBytes = bytes;
    }

    public byte[] buildPdf(LocalDate reportDate) throws IOException {
        List<DailyMovementRowDto> rows = excelService.fetchRows(reportDate);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Letter Portrait ~612 x 792 pt, matching Access report exact page size
            Document document = new Document(PageSize.LETTER, 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            if (rows.isEmpty()) {
                document.add(new Paragraph("No movement transactions found for " + reportDate, TITLE_FONT));
                document.close();
                return out.toByteArray();
            }

            for (int rIdx = 0; rIdx < rows.size(); rIdx++) {
                DailyMovementRowDto dto = rows.get(rIdx);

                if (rIdx > 0) {
                    document.newPage();
                }

                // ================= PAGE 1: FUNDS MOVEMENT SUMMARY & CHARGES =================
                renderPage1(document, dto, reportDate);

                // ================= PAGE 2: FUND MANAGER MOVEMENT SUMMARY - UNITS =================
                document.newPage();
                renderPage2(document, dto);

                // ================= PAGE 3: FUND MANAGER MOVEMENT SUMMARY - FUNDS =================
                document.newPage();
                renderPage3(document, dto);
            }

            document.close();
            return out.toByteArray();
        }
    }

    private void renderPage1(Document document, DailyMovementRowDto dto, LocalDate reportDate) throws DocumentException {
        // Top Header: Logo + Address & Phone on Left, Extraction Date on Right
        PdfPTable topMeta = new PdfPTable(2);
        topMeta.setWidthPercentage(100);
        topMeta.setWidths(new float[]{60, 40});

        PdfPCell leftMeta = new PdfPCell();
        leftMeta.setBorder(Rectangle.NO_BORDER);
        if (logoBytes != null && logoBytes.length > 0) {
            try {
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(56f, 36f);
                leftMeta.addElement(logo);
            } catch (Exception ignored) {
            }
        }
        leftMeta.addElement(new Phrase("Address", META_LABEL_FONT));
        leftMeta.addElement(new Phrase("Phone Number", META_LABEL_FONT));
        topMeta.addCell(leftMeta);

        PdfPCell rightMeta = new PdfPCell();
        rightMeta.setBorder(Rectangle.NO_BORDER);
        rightMeta.setHorizontalAlignment(Element.ALIGN_RIGHT);

        DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("M/d/yyyy");
        String todayStr = LocalDate.now().format(dFmt);
        String moveDateStr = dto.getModifiedDate() != null ? formatDateStr(dto.getModifiedDate()) : reportDate.format(dFmt);

        rightMeta.addElement(new Phrase("Extraction Date: " + todayStr, META_VAL_FONT));
        rightMeta.addElement(new Phrase("Movement on Date: " + moveDateStr, META_VAL_FONT));
        topMeta.addCell(rightMeta);

        document.add(topMeta);
        document.add(new Paragraph(" "));

        // Report Title
        Paragraph title = new Paragraph("Funds Movement Summary", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Sub Meta: Payment Date, Description
        PdfPTable subMeta = new PdfPTable(2);
        subMeta.setWidthPercentage(100);
        subMeta.setWidths(new float[]{50, 50});

        String payDateStr = dto.getPaymentDate() != null ? formatDateStr(dto.getPaymentDate()) : "-";
        PdfPCell payCell = new PdfPCell(new Phrase("Unit Price Date: " + payDateStr, META_LABEL_FONT));
        payCell.setBorder(Rectangle.NO_BORDER);
        subMeta.addCell(payCell);

        PdfPCell descCell = new PdfPCell(new Phrase("Transaction: " + (dto.getDescription() != null ? dto.getDescription() : "-"), META_LABEL_FONT));
        descCell.setBorder(Rectangle.NO_BORDER);
        descCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subMeta.addCell(descCell);

        document.add(subMeta);
        document.add(new Paragraph(" "));

        // Table 1: Funds Movement Summary (Contribution, Withdrawal, Termination, Top Up)
        PdfPTable t1 = new PdfPTable(5);
        t1.setWidthPercentage(100);
        t1.setWidths(new float[]{20, 20, 20, 20, 20});
        t1.setSpacingBefore(8);
        t1.setSpacingAfter(18);

        addCellBordered(t1, "", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t1, "Contribution", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t1, "Withdrawal", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t1, "Termination\n(charges not deducted)", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t1, "Top Up", HEADER_FONT, Element.ALIGN_CENTER, false);

        addMovementRow(t1, "Employee", dto.getContributionEe(), dto.getWithdrawalEe(), dto.getTerminationEe(), dto.getTopUpEe(), false);
        addMovementRow(t1, "VEE", dto.getContributionVee(), dto.getWithdrawalVee(), dto.getTerminationVee(), dto.getTopUpVee(), false);
        addMovementRow(t1, "Employer", dto.getContributionEr(), dto.getWithdrawalEr(), dto.getTerminationEr(), dto.getTopUpEr(), false);
        addMovementRow(t1, "Total", dto.getContributionTotal(), dto.getWithdrawalTotal(), dto.getTerminationTotal(), dto.getTopUpTotal(), true);

        document.add(t1);

        // Section Title: Charges:
        Paragraph chargesTitle = new Paragraph("Charges:", SECTION_TITLE_FONT);
        document.add(chargesTitle);

        // Table 2: Charges Summary
        PdfPTable t2 = new PdfPTable(7);
        t2.setWidthPercentage(100);
        t2.setWidths(new float[]{16, 14, 14, 14, 14, 14, 14});
        t2.setSpacingBefore(8);

        addCellBordered(t2, "", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t2, "Contribution\nCharges", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t2, "Withdrawal\nCharges", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t2, "Surrender\nCharges", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t2, "Top Up\nCharges", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t2, "Admin\nCharges", HEADER_FONT, Element.ALIGN_CENTER, false);
        addCellBordered(t2, "IMC", HEADER_FONT, Element.ALIGN_CENTER, false);

        addChargesRow(t2, "Employee", dto.getContributionChargesEe(), dto.getWithdrawalChargesEe(), dto.getSurrenderChargesEe(), dto.getTopUpChargesEe(), dto.getAdminChargesEe(), dto.getImcEe(), false);
        addChargesRow(t2, "VEE", dto.getContributionChargesVee(), dto.getWithdrawalChargesVee(), dto.getSurrenderChargesVee(), dto.getTopUpChargesVee(), 0.0, dto.getImcVee(), false);
        addChargesRow(t2, "Employer", dto.getContributionChargesEr(), dto.getWithdrawalChargesEr(), dto.getSurrenderChargesEr(), dto.getTopUpChargesEr(), dto.getAdminChargesEr(), dto.getImcEr(), false);
        addChargesRow(t2, "Total", dto.getContributionChargesTotal(), dto.getWithdrawalChargesTotal(), dto.getSurrenderChargesTotal(), dto.getTopUpChargesTotal(), dto.getAdminChargesTotal(), dto.getImcTotal(), true);

        document.add(t2);
    }

    private void renderPage2(Document document, DailyMovementRowDto dto) throws DocumentException {
        Paragraph title = new Paragraph("Fund Manager Movement Summary:", SECTION_TITLE_FONT);
        document.add(title);
        document.add(new Paragraph(" "));

        Paragraph sub = new Paragraph("Units:", SUBSECTION_FONT);
        document.add(sub);
        document.add(new Paragraph(" "));

        // Funds 1 to 5 Table
        PdfPTable t1_5 = new PdfPTable(6);
        t1_5.setWidthPercentage(100);
        t1_5.setWidths(new float[]{25, 15, 15, 15, 15, 15});
        t1_5.setSpacingBefore(4);
        t1_5.setSpacingAfter(20);

        addCellBordered(t1_5, "", HEADER_FONT, Element.ALIGN_CENTER, false);
        for (int i = 1; i <= 5; i++) {
            addCellBordered(t1_5, "Fund " + i, HEADER_FONT, Element.ALIGN_CENTER, false);
        }

        addUnitsRow(t1_5, "EE Units", dto.getTxEeUnits(), 0, 5, false);
        addUnitsRow(t1_5, "VEE Units", dto.getTxVeeUnits(), 0, 5, false);
        addUnitsRow(t1_5, "ER Units", dto.getTxErUnits(), 0, 5, false);
        addUnitsRow(t1_5, "ER Units (non-vested)", dto.getTermErUnits(), 0, 5, false);
        addUnitsRow(t1_5, "Total Units", dto.getTxTotUnits(), 0, 5, true);
        addPriceRow(t1_5, "Unit Price", dto.getUp(), 0, 5);

        document.add(t1_5);

        // Funds 6 to 10 Table
        PdfPTable t6_10 = new PdfPTable(6);
        t6_10.setWidthPercentage(100);
        t6_10.setWidths(new float[]{25, 15, 15, 15, 15, 15});
        t6_10.setSpacingBefore(4);

        addCellBordered(t6_10, "", HEADER_FONT, Element.ALIGN_CENTER, false);
        for (int i = 6; i <= 10; i++) {
            addCellBordered(t6_10, "Fund " + i, HEADER_FONT, Element.ALIGN_CENTER, false);
        }

        addUnitsRow(t6_10, "EE Units", dto.getTxEeUnits(), 5, 10, false);
        addUnitsRow(t6_10, "VEE Units", dto.getTxVeeUnits(), 5, 10, false);
        addUnitsRow(t6_10, "ER Units", dto.getTxErUnits(), 5, 10, false);
        addUnitsRow(t6_10, "ER Units (non-vested)", dto.getTermErUnits(), 5, 10, false);
        addUnitsRow(t6_10, "Total Units", dto.getTxTotUnits(), 5, 10, true);
        addPriceRow(t6_10, "Unit Price", dto.getUp(), 5, 10);

        document.add(t6_10);
    }

    private void renderPage3(Document document, DailyMovementRowDto dto) throws DocumentException {
        Paragraph sub = new Paragraph("Funds:", SUBSECTION_FONT);
        document.add(sub);
        document.add(new Paragraph(" "));

        // Funds 1 to 5 Table
        PdfPTable t1_5 = new PdfPTable(6);
        t1_5.setWidthPercentage(100);
        t1_5.setWidths(new float[]{25, 15, 15, 15, 15, 15});
        t1_5.setSpacingBefore(4);
        t1_5.setSpacingAfter(20);

        addCellBordered(t1_5, "", HEADER_FONT, Element.ALIGN_CENTER, false);
        for (int i = 1; i <= 5; i++) {
            addCellBordered(t1_5, "Fund " + i, HEADER_FONT, Element.ALIGN_CENTER, false);
        }

        addValueRow(t1_5, "EE Value", dto.getTxEeValue(), 0, 5, false);
        addValueRow(t1_5, "VEE Value", dto.getTxVeeValue(), 0, 5, false);
        addValueRow(t1_5, "ER Value", dto.getTxErValue(), 0, 5, false);
        addValueRow(t1_5, "ER Value (Non-vested)", dto.getTermErValue(), 0, 5, false);
        addValueRow(t1_5, "Total Value", dto.getTxTotValue(), 0, 5, true);

        document.add(t1_5);

        // Funds 6 to 10 Table
        PdfPTable t6_10 = new PdfPTable(6);
        t6_10.setWidthPercentage(100);
        t6_10.setWidths(new float[]{25, 15, 15, 15, 15, 15});
        t6_10.setSpacingBefore(4);

        addCellBordered(t6_10, "", HEADER_FONT, Element.ALIGN_CENTER, false);
        for (int i = 6; i <= 10; i++) {
            addCellBordered(t6_10, "Fund " + i, HEADER_FONT, Element.ALIGN_CENTER, false);
        }

        addValueRow(t6_10, "EE Value", dto.getTxEeValue(), 5, 10, false);
        addValueRow(t6_10, "VEE Value", dto.getTxVeeValue(), 5, 10, false);
        addValueRow(t6_10, "ER Value", dto.getTxErValue(), 5, 10, false);
        addValueRow(t6_10, "ER Value (Non-vested)", dto.getTermErValue(), 5, 10, false);
        addValueRow(t6_10, "Total Value", dto.getTxTotValue(), 5, 10, true);

        document.add(t6_10);
    }

    private void addMovementRow(PdfPTable table, String label, double cont, double with, double term, double topup, boolean bold) {
        Font f = bold ? ROW_BOLD_FONT : ROW_FONT;
        addCellBordered(table, label, f, Element.ALIGN_LEFT, false);
        addCellBordered(table, CURRENCY_FMT.format(cont), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(with), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(term), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(topup), f, Element.ALIGN_RIGHT, false);
    }

    private void addChargesRow(PdfPTable table, String label, double cont, double with, double surr, double topup, double admin, double imc, boolean bold) {
        Font f = bold ? ROW_BOLD_FONT : ROW_FONT;
        addCellBordered(table, label, f, Element.ALIGN_LEFT, false);
        addCellBordered(table, CURRENCY_FMT.format(cont), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(with), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(surr), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(topup), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(admin), f, Element.ALIGN_RIGHT, false);
        addCellBordered(table, CURRENCY_FMT.format(imc), f, Element.ALIGN_RIGHT, false);
    }

    private void addUnitsRow(PdfPTable table, String label, double[] arr, int start, int end, boolean bold) {
        Font f = bold ? ROW_BOLD_FONT : ROW_FONT;
        addCellBordered(table, label, f, Element.ALIGN_LEFT, false);
        for (int i = start; i < end; i++) {
            double val = (arr != null && i < arr.length) ? arr[i] : 0.0;
            addCellBordered(table, UNITS_FMT.format(val), f, Element.ALIGN_RIGHT, false);
        }
    }

    private void addPriceRow(PdfPTable table, String label, double[] arr, int start, int end) {
        addCellBordered(table, label, ROW_BOLD_FONT, Element.ALIGN_LEFT, false);
        for (int i = start; i < end; i++) {
            double val = (arr != null && i < arr.length) ? arr[i] : 0.0;
            String text = formatPriceAccessStyle(val);
            addCellBordered(table, text, ROW_FONT, Element.ALIGN_RIGHT, false);
        }
    }

    private void addValueRow(PdfPTable table, String label, double[] arr, int start, int end, boolean bold) {
        Font f = bold ? ROW_BOLD_FONT : ROW_FONT;
        addCellBordered(table, label, f, Element.ALIGN_LEFT, false);
        for (int i = start; i < end; i++) {
            double val = (arr != null && i < arr.length) ? arr[i] : 0.0;
            addCellBordered(table, VALUE_FMT.format(val), f, Element.ALIGN_RIGHT, false);
        }
    }

    private void addCellBordered(PdfPTable table, String text, Font font, int align, boolean grayBg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        if (grayBg) {
            cell.setBackgroundColor(new Color(240, 240, 240));
        }
        table.addCell(cell);
    }

    private String formatPriceAccessStyle(double val) {
        if (val == 0.0) return "0";
        if (val == Math.floor(val)) return String.valueOf((int) val);
        String s = String.valueOf(val);
        // If it has more than 6 decimal places, round gracefully
        if (s.contains(".") && s.length() - s.indexOf(".") > 7) {
            return new DecimalFormat("#,##0.00####").format(val);
        }
        return s;
    }

    private String formatDateStr(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            LocalDate ld = LocalDate.parse(iso.substring(0, 10));
            return ld.format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        } catch (Exception e) {
            return iso;
        }
    }
}
