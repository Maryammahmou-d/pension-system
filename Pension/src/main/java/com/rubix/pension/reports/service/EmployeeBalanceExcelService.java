package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.EmployeeBalanceReportResponse;
import com.rubix.pension.reports.dto.EmployeeBalanceRowDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class EmployeeBalanceExcelService {

    /** Access TransferSpreadsheet of table TempBalance. */
    private static final String[] HEADERS = {
            "Payment_Date",
            "Description",
            "Contribution EE",
            "Contribution VEE",
            "Contribution ER",
            "Transactional_EE_Value",
            "Transactional_VEE_Value",
            "Transactional_ER_Value",
            "Investment_Return_EE",
            "Investment_Return_VEE",
            "Investment_Return_ER",
            "Total_Investment_Return",
            "Investment_EE",
            "Investment_VEE",
            "Investment_ER",
            "Investment_Total"
    };

    public byte[] build(EmployeeBalanceReportResponse report) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempBalance");
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("M/d/yyyy"));

            writeHeader(sheet.createRow(0));

            List<EmployeeBalanceRowDto> rows = report.getRows() == null ? List.of() : report.getRows();
            for (int i = 0; i < rows.size(); i++) {
                writeRow(sheet.createRow(i + 1), rows.get(i), dateStyle);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public static String fileName(String valuationDate, String employeeNumber) {
        String ymd = valuationDate.replace("-", "");
        return "Balance_Report_" + ymd + "_" + employeeNumber + ".xlsx";
    }

    private static void writeHeader(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            row.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private static void writeRow(Row excelRow, EmployeeBalanceRowDto row, CellStyle dateStyle) {
        Cell dateCell = excelRow.createCell(0);
        LocalDate paymentDate = parseIsoDate(row.getPaymentDate());
        if (paymentDate != null) {
            Date excelDate = Date.from(paymentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateCell.setCellValue(excelDate);
            dateCell.setCellStyle(dateStyle);
        }
        excelRow.createCell(1).setCellValue(row.getDescription() == null ? "" : row.getDescription());
        setNumber(excelRow.createCell(2), row.getContributionEe());
        setNumber(excelRow.createCell(3), row.getContributionVee());
        setNumber(excelRow.createCell(4), row.getContributionEr());
        setNumber(excelRow.createCell(5), row.getTransactionalEeValue());
        setNumber(excelRow.createCell(6), row.getTransactionalVeeValue());
        setNumber(excelRow.createCell(7), row.getTransactionalErValue());
        setNumber(excelRow.createCell(8), row.getInvestmentReturnEe());
        setNumber(excelRow.createCell(9), row.getInvestmentReturnVee());
        setNumber(excelRow.createCell(10), row.getInvestmentReturnEr());
        setNumber(excelRow.createCell(11), row.getTotalInvestmentReturn());
        setNumber(excelRow.createCell(12), row.getInvestmentEe());
        setNumber(excelRow.createCell(13), row.getInvestmentVee());
        setNumber(excelRow.createCell(14), row.getInvestmentEr());
        setNumber(excelRow.createCell(15), row.getInvestmentTotal());
    }

    private static LocalDate parseIsoDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            return null;
        }
        String datePart = isoDate.length() >= 10 ? isoDate.substring(0, 10) : isoDate;
        try {
            return LocalDate.parse(datePart);
        } catch (Exception ex) {
            return null;
        }
    }

    /** Access Excel keeps full double precision, not rounded integers. */
    private static void setNumber(Cell cell, double value) {
        cell.setCellValue(value);
    }
}
