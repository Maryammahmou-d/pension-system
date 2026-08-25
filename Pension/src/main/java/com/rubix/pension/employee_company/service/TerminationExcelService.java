package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.TerminationSummaryRowDto;
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
 * Builds the company-termination summary workbook (Termination_{companyNumber}.xlsx), matching
 * the legacy Access "Extract Company Termination Info Per Employee" export (one row per
 * terminated employee, sheet named "Termination"). Stateless: rendered entirely from an
 * already-computed {@link BulkTerminationResultDto}.
 */
@Service
public class TerminationExcelService {

    private static final String[] HEADERS = {
            "Payment_Date", "Company_Number", "Employee_ID", "Employee_Number",
            "National_ID", "Full_Name", "DOB", "Gender", "Currency",
            "Total_EE_Value", "Total_VEE_Value", "Total_ER_Value"
    };

    public byte[] build(BulkTerminationResultDto result) throws IOException {
        List<TerminationSummaryRowDto> rows = result.getSummaryRows() == null ? List.of() : result.getSummaryRows();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Termination");
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle numberStyle = numberStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col += 1) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (TerminationSummaryRowDto summary : rows) {
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(nullToEmpty(summary.getPaymentDate()));
                row.createCell(1).setCellValue(nullToEmpty(summary.getCompanyNumber()));
                row.createCell(2).setCellValue(summary.getEmployeeId() == null ? 0 : summary.getEmployeeId());
                row.createCell(3).setCellValue(nullToEmpty(summary.getEmployeeNumber()));
                row.createCell(4).setCellValue(nullToEmpty(summary.getNationalId()));
                row.createCell(5).setCellValue(nullToEmpty(summary.getFullName()));
                row.createCell(6).setCellValue(nullToEmpty(summary.getDob()));
                row.createCell(7).setCellValue(nullToEmpty(summary.getGender()));
                row.createCell(8).setCellValue(nullToEmpty(summary.getCurrency()));
                setNumericCell(row, 9, summary.getTotalEeValue(), numberStyle);
                setNumericCell(row, 10, summary.getTotalVeeValue(), numberStyle);
                setNumericCell(row, 11, summary.getTotalErValue(), numberStyle);
                rowIndex += 1;
            }

            for (int col = 0; col < HEADERS.length; col += 1) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String fileName(String companyNumber) {
        String trimmed = companyNumber == null ? "" : companyNumber.trim();
        return "Termination_" + trimmed + ".xlsx";
    }

    private void setNumericCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
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
