package com.rubix.pension.reports.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Service
public class CompanyTransactionsExcelService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CompanyTransactionsExcelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] build(String companyNumber) throws IOException {
        if (companyNumber == null || companyNumber.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company number is required.");
        }
        String trimmed = companyNumber.trim();

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT * FROM \"Transactions\" WHERE \"Company_Number\" = :companyNumber ORDER BY \"Serial\"",
                Map.of("companyNumber", trimmed)
        );

        SqlRowSetMetaData metaData = rowSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");

            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                Cell cell = headerRow.createCell(i - 1);
                cell.setCellValue(metaData.getColumnName(i));
            }

            int rowIndex = 1;
            while (rowSet.next()) {
                Row dataRow = sheet.createRow(rowIndex++);
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rowSet.getObject(i);
                    setCellValue(dataRow.createCell(i - 1), value);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String fileName(String companyNumber) {
        String trimmed = companyNumber == null ? "" : companyNumber.trim();
        return "Transactions_" + trimmed + ".xlsx";
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
