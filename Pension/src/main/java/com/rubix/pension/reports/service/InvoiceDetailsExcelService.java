package com.rubix.pension.reports.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceDetailsExcelService {

    private static final List<String> COLUMNS = buildColumns();
    private static final String SQL = buildSql();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InvoiceDetailsExcelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] build(String invoiceNumber) throws IOException {
        String invoice = requireValue(invoiceNumber, "Invoice number is required.");
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                SQL,
                Map.of("invoiceNumber", invoice)
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempInvoicesEmployee");

            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("m/d/yyyy"));

            Row headerRow = sheet.createRow(0);
            for (int column = 0; column < COLUMNS.size(); column += 1) {
                headerRow.createCell(column).setCellValue(COLUMNS.get(column));
            }

            int rowIndex = 1;
            while (rowSet.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int column = 0; column < COLUMNS.size(); column += 1) {
                    setCellValue(row.createCell(column), rowSet.getObject(column + 1), dateCellStyle);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String fileName(String invoiceNumber) {
        String invoice = invoiceNumber == null ? "" : invoiceNumber.trim();
        return "Invoice_" + invoice + ".xlsx";
    }

    private String requireValue(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private void setCellValue(Cell cell, Object value, CellStyle dateCellStyle) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof LocalDate localDate) {
            cell.setCellValue(localDate);
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof LocalDateTime localDateTime) {
            cell.setCellValue(localDateTime);
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof OffsetDateTime offsetDateTime) {
            cell.setCellValue(offsetDateTime.toLocalDateTime());
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof ZonedDateTime zonedDateTime) {
            cell.setCellValue(zonedDateTime.toLocalDateTime());
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static List<String> buildColumns() {
        return List.of(
                "ID", "Serial", "Modified_Date", "Invoice_Number", "Invoice_Date", "Status",
                "Payment_Date", "Date_From", "Date_To", "Company_Number", "Kafs_Company_Number",
                "Company_Name", "Employee_ID", "Employee_Number", "Gross_Salary", "Currency",
                "Category", "EE", "ER", "VEE", "Employee_Contribution", "VEE_Contribution",
                "Employer_Contribution", "Total_Contribution", "Proportional_Stamp_Duty",
                "Supervisory_Fees", "FRA_Approval_Fees", "FRA_Provision_Fees", "Grand_Total",
                "UserName"
        );
    }

    private static String buildSql() {
        return """
                SELECT "ID", "Serial", "Modified_Date", "Invoice_Number", "Invoice_Date", "Status",
                       "Payment_Date", "Date_From", "Date_To", "Company_Number", "Kafs_Company_Number",
                       "Company_Name", "Employee_ID", "Employee_Number", "Gross_Salary", "Currency",
                       "Category", "EE", "ER", "VEE", "Employee_Contribution", "VEE_Contribution",
                       "Employer_Contribution", "Total_Contribution", "Proportional_Stamp_Duty",
                       "Supervisory_Fees", "FRA_Approval_Fees", "FRA_Provision_Fees", "Grand_Total",
                       "UserName"
                FROM "InvoicesEmployee"
                WHERE "Invoice_Number" = :invoiceNumber
                ORDER BY "ID" ASC
                """;
    }
}
