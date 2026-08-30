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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeRecordsExcelService {

    private static final List<String> COLUMNS = buildColumns();
    private static final String SQL = buildSql();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeRecordsExcelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] build(String companyNumber) throws IOException {
        String company = requireValue(companyNumber, "Company number is required.");
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                SQL,
                Map.of("companyNumber", company)
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempEmployees");

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

    public static String fileName(String companyNumber) {
        String company = companyNumber == null ? "" : companyNumber.trim();
        return "Employee_" + company + ".xlsx";
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
        List<String> columns = new ArrayList<>(List.of(
                "ID", "Serial", "Modified_Date", "Company_Number", "Employee_ID", "Employee_Number",
                "National_ID", "Full_Name", "DOB", "Gender", "Occupation", "Hire_Date",
                "Age_At_Hire", "Pension_Start_Date", "Kaf_Joining_Date", "Category",
                "Gross_Salary", "Salary_Currency", "Contribution_EE", "Contribution_ER",
                "E-mail", "Starting_EE_Value", "Starting_ER_Value", "Starting_Fund_Value",
                "Termination_Date", "Resignation_Date", "VEE"
        ));
        for (int i = 1; i <= 10; i++) {
            columns.add("Weight_F" + i + "_EE");
        }
        for (int i = 1; i <= 10; i++) {
            columns.add("Weight_F" + i + "_ER");
        }
        columns.add("Username");
        return columns;
    }

    private static String buildSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("\"ID\", ");
        sb.append("\"Serial\", ");
        sb.append("\"Modified_Date\", ");
        sb.append("\"Company_Number\", ");
        sb.append("\"Employee_ID\", ");
        sb.append("\"Employee_Number\", ");
        sb.append("\"National_ID\", ");
        sb.append("\"Full_Name\", ");
        sb.append("\"DOB\", ");
        sb.append("\"Gender\", ");
        sb.append("\"Occupation\", ");
        sb.append("\"Hire_Date\", ");
        sb.append("(CASE WHEN \"Hire_Date\" IS NOT NULL AND \"DOB\" IS NOT NULL ");
        sb.append("      THEN (CAST(CAST(\"Hire_Date\" AS date) - CAST(\"DOB\" AS date) AS double precision) / 365.25) ");
        sb.append("      ELSE NULL END) AS \"Age_At_Hire\", ");
        sb.append("\"Pension_Start_Date\", ");
        sb.append("\"Kaf_Joining_Date\", ");
        sb.append("\"Category\", ");
        sb.append("\"Gross_Salary\", ");
        sb.append("\"Salary_Currency\", ");
        sb.append("\"Contribution_EE\", ");
        sb.append("\"Contribution_ER\", ");
        sb.append("\"E-mail\", ");
        sb.append("\"Starting_EE_Value\", ");
        sb.append("\"Starting_ER_Value\", ");
        sb.append("COALESCE(\"Starting_Fund_Value\", 0) AS \"Starting_Fund_Value\", ");
        sb.append("\"Termination_Date\", ");
        sb.append("\"Resignation_Date\", ");
        sb.append("COALESCE(\"VEE\", 0) AS \"VEE\", ");
        for (int i = 1; i <= 10; i++) {
            sb.append("\"Weight_F").append(i).append("_EE\", ");
        }
        for (int i = 1; i <= 10; i++) {
            sb.append("\"Weight_F").append(i).append("_ER\", ");
        }
        sb.append("NULL::text AS \"Username\" ");
        sb.append("FROM \"LatestEmployeeRecords\" ");
        sb.append("WHERE \"Company_Number\" = :companyNumber ");
        sb.append("ORDER BY \"ID\" ASC");
        return sb.toString();
    }
}
