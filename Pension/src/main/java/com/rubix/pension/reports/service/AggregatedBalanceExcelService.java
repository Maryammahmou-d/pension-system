package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.AggregatedBalanceReportResponse;
import com.rubix.pension.reports.dto.AggregatedBalanceRowDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class AggregatedBalanceExcelService {

    private static final String[] HEADERS = {
            "Full_Name",
            "Gross Contribution EE",
            "Gross Contribution VEE",
            "Gross Contribution ER",
            "Net Contribution EE",
            "Net Contribution VEE",
            "Net Contribution ER",
            "Investment Return EE",
            "Investment Return VEE",
            "Investment Return ER",
            "Total Investment Return",
            "Accumulated Value EE",
            "Accumulated Value VEE",
            "Accumulated Value ER",
            "Accumulated Value Total"
    };

    public byte[] build(AggregatedBalanceReportResponse report) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Balance");
            writeHeader(sheet.createRow(0));

            List<AggregatedBalanceRowDto> rows = report.getRows();
            for (int i = 0; i < rows.size(); i++) {
                writeRow(sheet.createRow(i + 1), rows.get(i));
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public static String fileName(String valuationDate, String companyNumber) {
        String ymd = valuationDate.replace("-", "");
        return "Balance_Report_" + ymd + "_" + companyNumber + ".xlsx";
    }

    private static void writeHeader(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            row.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private static void writeRow(Row excelRow, AggregatedBalanceRowDto row) {
        excelRow.createCell(0).setCellValue(row.getFullName() == null ? "" : row.getFullName());
        setWholeNumber(excelRow.createCell(1), row.getGrossContributionEe());
        setWholeNumber(excelRow.createCell(2), row.getGrossContributionVee());
        setWholeNumber(excelRow.createCell(3), row.getGrossContributionEr());
        setWholeNumber(excelRow.createCell(4), row.getNetContributionEe());
        setWholeNumber(excelRow.createCell(5), row.getNetContributionVee());
        setWholeNumber(excelRow.createCell(6), row.getNetContributionEr());
        setWholeNumber(excelRow.createCell(7), row.getInvestmentReturnEe());
        setWholeNumber(excelRow.createCell(8), row.getInvestmentReturnVee());
        setWholeNumber(excelRow.createCell(9), row.getInvestmentReturnEr());
        setWholeNumber(excelRow.createCell(10), row.getTotalInvestmentReturn());
        setWholeNumber(excelRow.createCell(11), row.getAccumulatedValueEe());
        setWholeNumber(excelRow.createCell(12), row.getAccumulatedValueVee());
        setWholeNumber(excelRow.createCell(13), row.getAccumulatedValueEr());
        setWholeNumber(excelRow.createCell(14), row.getAccumulatedValueTotal());
    }

    private static void setWholeNumber(Cell cell, double value) {
        cell.setCellValue(Math.round(value));
    }
}
