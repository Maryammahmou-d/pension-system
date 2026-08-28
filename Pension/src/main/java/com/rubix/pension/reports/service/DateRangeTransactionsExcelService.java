package com.rubix.pension.reports.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DateRangeTransactionsExcelService {

    private static final List<String> COLUMNS = buildColumns();
    private static final String SQL = buildSql();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DateRangeTransactionsExcelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] build(String startDateValue, String endDateValue) throws IOException {
        LocalDate startDate = parseDate(startDateValue, "Start date");
        LocalDate endDate = parseDate(endDateValue, "End date");
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                SQL,
                Map.of("startDate", startDate, "endDateExclusive", endDate.plusDays(1))
        );

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        try (workbook; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempTransactions");
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
            Row headerRow = sheet.createRow(0);
            for (int column = 0; column < COLUMNS.size(); column += 1) {
                headerRow.createCell(column).setCellValue(COLUMNS.get(column));
            }
            sheet.setColumnWidth(COLUMNS.indexOf("Modified_Date"), 14 * 256);
            sheet.setColumnWidth(COLUMNS.indexOf("Payment_Date"), 14 * 256);
            sheet.setColumnWidth(COLUMNS.indexOf("Retro_Date"), 14 * 256);

            int rowIndex = 1;
            while (rowSet.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int column = 0; column < COLUMNS.size(); column += 1) {
                    setCellValue(row.createCell(column), rowSet.getObject(column + 1), dateStyle);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } finally {
            workbook.dispose();
        }
    }

    public static String fileName(String startDateValue, String endDateValue) {
        LocalDate startDate = parseDate(startDateValue, "Start date");
        LocalDate endDate = parseDate(endDateValue, "End date");
        return "Transactions_" + compactDate(startDate) + "_" + compactDate(endDate) + ".xlsx";
    }

    private static LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required.");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must use yyyy-MM-dd format.");
        }
    }

    private static String compactDate(LocalDate date) {
        return date.toString().replace("-", "");
    }

    private void setCellValue(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static List<String> buildColumns() {
        List<String> columns = new ArrayList<>(List.of(
                "ID", "Serial", "Description", "Modified_Date", "Payment_Date", "Retro_Date",
                "Invoice_Number", "Company_Number", "Employee_ID", "Employee_Number", "Currency",
                "UP1", "UP2", "UP3", "UP4", "UP5", "UP6", "UP7", "UP8", "UP9", "UP10",
                "Contribution_EE", "Contribution_VEE", "Contribution_ER", "Contribution_Total",
                "Withdrawal_EE", "Withdrawal_VEE", "Withdrawal_ER", "Withdrawal_Total",
                "TopUp_EE", "TopUp_VEE", "TopUp_ER", "TopUp_Total",
                "IMC_EE", "IMC_VEE", "IMC_ER", "IMC_Total",
                "Admin_Charges_EE", "Admin_Charges_ER", "Admin_Charges_Total",
                "Contribution_Charges_EE", "Contribution_Charges_VEE", "Contribution_Charges_ER", "Contribution_Charges_Total",
                "Surrender_Charges_EE", "Surrender_Charges_VEE", "Surrender_Charges_ER", "Surrender_Charges_Total",
                "Top_Up_Charges_EE", "Top_Up_Charges_VEE", "Top_Up_Charges_ER", "Top_Up_Charges_Total",
                "Withdrawal_Charges_EE", "Withdrawal_Charges_VEE", "Withdrawal_Charges_ER", "Withdrawal_Charges_Total"
        ));
        addFundColumns(columns, "Transactional_EE_Value_F");
        addFundColumns(columns, "Transactional_VEE_Value_F");
        addFundColumns(columns, "Transactional_ER_Value_F");
        addFundColumns(columns, "Terminated_ER_Value_F");
        addFundColumns(columns, "Transactional_Total_Value_F");
        columns.addAll(List.of(
                "Transactional_EE_Value", "Transactional_VEE_Value", "Transactional_ER_Value",
                "Terminated_ER_Value", "Transactional_Total_Value"
        ));
        addFundColumns(columns, "Starting_EE_Units_F");
        addFundColumns(columns, "Starting_VEE_Units_F");
        addFundColumns(columns, "Starting_ER_Units_F");
        addFundColumns(columns, "Starting_Total_Units_F");
        addFundColumns(columns, "Transactional_EE_Units_F");
        addFundColumns(columns, "Transactional_VEE_Units_F");
        addFundColumns(columns, "Transactional_ER_Units_F");
        addFundColumns(columns, "Terminated_ER_Units_F");
        addFundColumns(columns, "Transactional_Total_Units_F");
        addFundColumns(columns, "Total_EE_Units_F");
        addFundColumns(columns, "Total_VEE_Units_F");
        addFundColumns(columns, "Total_ER_Units_F");
        addFundColumns(columns, "Total_Units_F");
        columns.add("UserName");
        return List.copyOf(columns);
    }

    private static void addFundColumns(List<String> columns, String prefix) {
        for (int fund = 1; fund <= 10; fund += 1) {
            columns.add(prefix + fund);
        }
    }

    private static String buildSql() {
        String columns = COLUMNS.stream()
                .map(column -> "t.\"" + column + "\"")
                .collect(java.util.stream.Collectors.joining(", "));
        return "SELECT " + columns + " FROM \"Transactions\" t "
                + "WHERE t.\"Payment_Date\" >= CAST(:startDate AS date) "
                + "AND t.\"Payment_Date\" < CAST(:endDateExclusive AS date) "
                + "ORDER BY t.\"Payment_Date\", t.\"Serial\"";
    }
}
