package com.rubix.pension.reports.service;

import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.reports.sql.CompanyFundsQueries;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds the company funds aggregate export (Funds_Report_yyyymmdd.xlsx).
 * Mirrors the legacy Access form's "Extract Companies Funds" make-table query,
 * which populates TempFunds with one row per company containing summed fund
 * unit/value columns, then exports it to Excel.
 */
@Service
public class CompanyFundsExcelService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FundHoldingsSupport fundHoldingsSupport;

    public CompanyFundsExcelService(
            NamedParameterJdbcTemplate jdbcTemplate,
            FundHoldingsSupport fundHoldingsSupport
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    /**
     * Generates the Excel in memory and returns it as a byte array.
     */
    public byte[] build(LocalDate valuationDate) throws IOException {
        List<Map<String, Object>> rows = fetchRows(valuationDate);
        return createWorkbook(rows);
    }

    /**
     * Generates the Excel and writes it to the supplied path.
     */
    public ExtractResult extract(LocalDate valuationDate, Path outputPath) throws IOException {
        if (outputPath == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Output path is required.");
        }
        List<Map<String, Object>> rows = fetchRows(valuationDate);
        byte[] bytes = createWorkbook(rows);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, bytes);
        return new ExtractResult(outputPath.toString(), rows.size());
    }

    private List<Map<String, Object>> fetchRows(LocalDate valuationDate) {
        fundHoldingsSupport.requireUnitPrice(valuationDate);
        return jdbcTemplate.queryForList(
                CompanyFundsQueries.COMPANY_FUNDS_SQL,
                Map.of("valuationDate", valuationDate)
        );
    }

    private byte[] createWorkbook(List<Map<String, Object>> rows) throws IOException {
        List<String> headers = buildHeaders();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempFunds");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i += 1) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }

            for (int i = 0; i < rows.size(); i += 1) {
                Map<String, Object> row = rows.get(i);
                Row dataRow = sheet.createRow(i + 1);
                for (int j = 0; j < headers.size(); j += 1) {
                    Object value = row.get(headers.get(j));
                    setCellValue(dataRow.createCell(j), value);
                }
            }

            for (int i = 0; i < headers.size(); i += 1) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private List<String> buildHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("Company_Number");
        addFundHeaders(headers, "Total_EE_Units_F");
        addFundHeaders(headers, "Total_VEE_Units_F");
        addFundHeaders(headers, "Total_ER_Units_F");
        addFundHeaders(headers, "Total_EE_Value_F");
        addFundHeaders(headers, "Total_VEE_Value_F");
        addFundHeaders(headers, "Total_ER_Value_F");
        addFundHeaders(headers, "Total_Value_F");
        return headers;
    }

    private void addFundHeaders(List<String> headers, String prefix) {
        for (int i = 1; i <= 10; i += 1) {
            headers.add(prefix + i);
        }
    }

    public static String fileName(LocalDate valuationDate) {
        return "Funds_Report_" + valuationDate.toString().replace("-", "") + ".xlsx";
    }

    public static class ExtractResult {
        private final String filePath;
        private final int recordCount;

        public ExtractResult(String filePath, int recordCount) {
            this.filePath = filePath;
            this.recordCount = recordCount;
        }

        public String getFilePath() {
            return filePath;
        }

        public int getRecordCount() {
            return recordCount;
        }
    }
}
