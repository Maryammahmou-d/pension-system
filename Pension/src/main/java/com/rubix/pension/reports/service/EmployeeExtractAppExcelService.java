package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.EmployeeExtractAppRowDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class EmployeeExtractAppExcelService {

    /** Access TransferSpreadsheet of table TempAppData. */
    private static final String[] HEADERS = {
            "Company_Number",
            "Company_Name",
            "Employee_Number",
            "Full_Name",
            "National_ID",
            "DOB",
            "Gender",
            "Occupation",
            "Pension_Start_Date",
            "E-mail",
            "Termination_Date",
            "Available_Withdrawal",
            "Total_Fund_Value",
            "Gross_EE_Contribution",
            "Gross_VEE_Contribution",
            "Gross_ER_Contribution",
            "Net_EE_Contribution",
            "Net_VEE_Contribution",
            "Net_ER_Contribution",
            "Gain_Value",
            "Percentage_Gain_UnitPrice",
            "Percentage_Gain_Paid",
            "Price_Date"
    };

    public byte[] build(List<EmployeeExtractAppRowDto> rows) throws IOException {
        // Streaming workbook — XSSF on ~25k rows is very slow / memory-heavy.
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempAppData");
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("M/d/yyyy"));

            writeHeader(sheet.createRow(0));
            List<EmployeeExtractAppRowDto> data = rows == null ? List.of() : rows;
            for (int i = 0; i < data.size(); i++) {
                writeRow(sheet.createRow(i + 1), data.get(i), dateStyle);
            }

            workbook.write(outputStream);
            workbook.dispose();
            return outputStream.toByteArray();
        }
    }

    /** Access: AppData_yyyy_m_d.xlsx (Month()/Day() — no zero-pad). */
    public static String fileName(String reportDateIso) {
        LocalDate date = LocalDate.parse(reportDateIso.substring(0, 10));
        return "AppData_%d_%d_%d.xlsx".formatted(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    private static void writeHeader(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            row.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private static void writeRow(Row excelRow, EmployeeExtractAppRowDto row, CellStyle dateStyle) {
        excelRow.createCell(0).setCellValue(nz(row.getCompanyNumber()));
        excelRow.createCell(1).setCellValue(nz(row.getCompanyName()));
        excelRow.createCell(2).setCellValue(nz(row.getEmployeeNumber()));
        excelRow.createCell(3).setCellValue(nz(row.getFullName()));
        excelRow.createCell(4).setCellValue(nz(row.getNationalId()));
        setDate(excelRow.createCell(5), row.getDob(), dateStyle);
        excelRow.createCell(6).setCellValue(nz(row.getGender()));
        excelRow.createCell(7).setCellValue(nz(row.getOccupation()));
        setDate(excelRow.createCell(8), row.getPensionStartDate(), dateStyle);
        excelRow.createCell(9).setCellValue(nz(row.getEmail()));
        setDate(excelRow.createCell(10), row.getTerminationDate(), dateStyle);
        if (row.getAvailableWithdrawal() == null) {
            excelRow.createCell(11).setBlank();
        } else {
            excelRow.createCell(11).setCellValue(row.getAvailableWithdrawal());
        }
        excelRow.createCell(12).setCellValue(row.getTotalFundValue());
        excelRow.createCell(13).setCellValue(row.getGrossEeContribution());
        excelRow.createCell(14).setCellValue(row.getGrossVeeContribution());
        excelRow.createCell(15).setCellValue(row.getGrossErContribution());
        excelRow.createCell(16).setCellValue(row.getNetEeContribution());
        excelRow.createCell(17).setCellValue(row.getNetVeeContribution());
        excelRow.createCell(18).setCellValue(row.getNetErContribution());
        excelRow.createCell(19).setCellValue(row.getGainValue());
        excelRow.createCell(20).setCellValue(row.getPercentageGainUnitPrice());
        if (row.getPercentageGainPaid() == null) {
            excelRow.createCell(21).setBlank();
        } else {
            excelRow.createCell(21).setCellValue(row.getPercentageGainPaid());
        }
        setDate(excelRow.createCell(22), row.getPriceDate(), dateStyle);
    }

    private static void setDate(Cell cell, LocalDate value, CellStyle dateStyle) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        cell.setCellValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cell.setCellStyle(dateStyle);
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
