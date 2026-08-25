package com.rubix.pension.reports.service;

import org.apache.poi.ss.usermodel.Cell;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeTransactionsExcelService {

    private static final List<String> COLUMNS = buildColumns();
    private static final String SQL = buildSql();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeTransactionsExcelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] build(String companyNumber, String employeeNumber) throws IOException {
        String company = requireValue(companyNumber, "Company number is required.");
        String employee = requireValue(employeeNumber, "Employee number is required.");
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                SQL,
                Map.of("companyNumber", company, "employeeNumber", employee)
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempTransactions");
            Row headerRow = sheet.createRow(0);
            for (int column = 0; column < COLUMNS.size(); column += 1) {
                headerRow.createCell(column).setCellValue(COLUMNS.get(column));
            }

            int rowIndex = 1;
            while (rowSet.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int column = 0; column < COLUMNS.size(); column += 1) {
                    setCellValue(row.createCell(column), rowSet.getObject(column + 1));
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String fileName(String employeeNumber) {
        String employee = employeeNumber == null ? "" : employeeNumber.trim();
        return "Transactions_" + employee + ".xlsx";
    }

    private String requireValue(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static List<String> buildColumns() {
        List<String> columns = new ArrayList<>(List.of(
                "Serial", "Description", "Modified_Date", "Payment_Date", "Invoice_Number",
                "Company_Number", "Employee_ID", "Employee_Number",
                "UP1", "UP2", "UP3", "UP4", "UP5", "UP6", "UP7", "UP8", "UP9", "UP10",
                "Contribution_EE", "Contribution_VEE", "Contribution_ER",
                "Withdrawal_EE", "Withdrawal_VEE", "Withdrawal_ER",
                "TopUp_EE", "TopUp_ER", "IMC_EE", "IMC_ER",
                "Admin_Charges_EE", "Admin_Charges_ER",
                "Contribution_Charges_EE", "Contribution_Charges_VEE", "Contribution_Charges_ER",
                "Surrender_Charges_EE", "Surrender_Charges_ER",
                "Top_Up_Charges_EE", "Top_Up_Charges_ER",
                "Withdrawal_Charges_EE", "Withdrawal_Charges_ER"
        ));
        addFundColumns(columns, "Transactional_EE_Value_F");
        addFundColumns(columns, "Transactional_VEE_Value_F");
        addFundColumns(columns, "Transactional_ER_Value_F");
        addFundColumns(columns, "Terminated_ER_Value_F");
        addFundColumns(columns, "Starting_EE_Units_F");
        addFundColumns(columns, "Starting_VEE_Units_F");
        addFundColumns(columns, "Starting_ER_Units_F");
        addFundColumns(columns, "Transactional_EE_Units_F");
        addFundColumns(columns, "Transactional_VEE_Units_F");
        addFundColumns(columns, "Transactional_ER_Units_F");
        addFundColumns(columns, "Terminated_ER_Units_F");
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
                + "WHERE t.\"Company_Number\" = :companyNumber "
                + "AND t.\"Employee_Number\" = :employeeNumber "
                + "ORDER BY t.\"Serial\"";
    }
}
