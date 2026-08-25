package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.TerminationReportDto;
import com.rubix.pension.employee_company.dto.TerminationReportFundRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Builds the raw per-transaction termination export (Terminations_{companyNumber}.xlsx),
 * mirroring the legacy Access "TerminationDataExport" query output (a dump of the
 * TempTerminationTransactions staging table: one row per terminated employee's Transactions
 * insert, with per-fund unit prices and values). Stateless: rendered entirely from an
 * already-computed {@link BulkTerminationResultDto}.
 */
@Service
public class TerminationTransactionsExcelService {

    private static final int FUND_COUNT = 10;

    public byte[] build(BulkTerminationResultDto result) throws IOException {
        List<TerminationReportDto> reports = result.getReports() == null ? List.of() : result.getReports();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("TempTerminationTransactions");
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle numberStyle = numberStyle(workbook);

            List<String> headers = buildHeaders();
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.size(); col += 1) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (TerminationReportDto report : reports) {
                Row row = sheet.createRow(rowIndex);
                int col = 0;
                row.createCell(col++).setCellValue(report.getId() == null ? 0 : report.getId());
                row.createCell(col++).setCellValue(report.getEmployeeSerial() == null ? 0 : report.getEmployeeSerial());
                row.createCell(col++).setCellValue(nullToEmpty(report.getModifiedDate()));
                row.createCell(col++).setCellValue(nullToEmpty(report.getPaymentDate()));
                row.createCell(col++).setCellValue(nullToEmpty(report.getCompanyNumber()));
                row.createCell(col++).setCellValue(report.getEmployeeId() == null ? 0 : report.getEmployeeId());
                row.createCell(col++).setCellValue(nullToEmpty(report.getEmployeeNumber()));

                List<TerminationReportFundRow> fundRows = report.getRows() == null ? List.of() : report.getRows();

                for (int i = 0; i < FUND_COUNT; i += 1) {
                    col = setNumericCell(row, col, unitPriceOf(fundRows, i), numberStyle);
                }
                for (int i = 0; i < FUND_COUNT; i += 1) {
                    col = setNumericCell(row, col, eeValueOf(fundRows, i), numberStyle);
                }
                for (int i = 0; i < FUND_COUNT; i += 1) {
                    col = setNumericCell(row, col, veeValueOf(fundRows, i), numberStyle);
                }
                for (int i = 0; i < FUND_COUNT; i += 1) {
                    col = setNumericCell(row, col, erValueOf(fundRows, i), numberStyle);
                }
                for (int i = 0; i < FUND_COUNT; i += 1) {
                    col = setNumericCell(row, col, terminatedErValueOf(fundRows, i), numberStyle);
                }

                col = setNumericCell(row, col, report.getTotalTransactionalEeValue(), numberStyle);
                col = setNumericCell(row, col, report.getTotalTransactionalVeeValue(), numberStyle);
                col = setNumericCell(row, col, report.getTotalTransactionalErValue(), numberStyle);
                setNumericCell(row, col, report.getTotalTransactionalValue(), numberStyle);

                rowIndex += 1;
            }

            for (int col = 0; col < headers.size(); col += 1) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String fileName(String companyNumber) {
        String trimmed = companyNumber == null ? "" : companyNumber.trim();
        return "Terminations_" + trimmed + ".xlsx";
    }

    private List<String> buildHeaders() {
        List<String> headers = new java.util.ArrayList<>(List.of(
                "ID", "Serial", "Modified_Date", "Payment_Date", "Company_Number", "Employee_ID", "Employee_Number"
        ));
        addFundHeaders(headers, "UP");
        addFundHeaders(headers, "Transactional_EE_Value_F");
        addFundHeaders(headers, "Transactional_VEE_Value_F");
        addFundHeaders(headers, "Transactional_ER_Value_F");
        addFundHeaders(headers, "Terminated_ER_Value_F");
        headers.add("Transactional_EE_Value");
        headers.add("Transactional_VEE_Value");
        headers.add("Transactional_ER_Value");
        headers.add("Transactional_Total_Value");
        return headers;
    }

    private void addFundHeaders(List<String> headers, String prefix) {
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            headers.add(prefix + i);
        }
    }

    private double unitPriceOf(List<TerminationReportFundRow> rows, int index) {
        return index < rows.size() ? rows.get(index).getUnitPrice() : 0;
    }

    private double eeValueOf(List<TerminationReportFundRow> rows, int index) {
        return index < rows.size() ? rows.get(index).getTransactionalEeValue() : 0;
    }

    private double veeValueOf(List<TerminationReportFundRow> rows, int index) {
        return index < rows.size() ? rows.get(index).getTransactionalVeeValue() : 0;
    }

    private double erValueOf(List<TerminationReportFundRow> rows, int index) {
        return index < rows.size() ? rows.get(index).getTransactionalErValue() : 0;
    }

    private double terminatedErValueOf(List<TerminationReportFundRow> rows, int index) {
        return index < rows.size() ? rows.get(index).getTerminatedErValue() : 0;
    }

    private int setNumericCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return col + 1;
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle numberStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
