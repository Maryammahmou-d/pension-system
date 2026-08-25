package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.service.CompanyTransactionsExcelService;
import com.rubix.pension.reports.service.DateRangeTransactionsExcelService;
import com.rubix.pension.reports.service.EmployeeTransactionsExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/transactions")
public class CompanyTransactionsController {

    private final CompanyTransactionsExcelService excelService;
    private final EmployeeTransactionsExcelService employeeExcelService;
    private final DateRangeTransactionsExcelService dateRangeExcelService;

    public CompanyTransactionsController(
            CompanyTransactionsExcelService excelService,
            EmployeeTransactionsExcelService employeeExcelService,
            DateRangeTransactionsExcelService dateRangeExcelService
    ) {
        this.excelService = excelService;
        this.employeeExcelService = employeeExcelService;
        this.dateRangeExcelService = dateRangeExcelService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCompanyTransactions(@RequestParam String companyNumber) throws IOException {
        byte[] xlsx = excelService.build(companyNumber);
        String filename = CompanyTransactionsExcelService.fileName(companyNumber);
        return excelResponse(xlsx, filename);
    }

    @GetMapping("/employee/export")
    public ResponseEntity<byte[]> exportEmployeeTransactions(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber
    ) throws IOException {
        byte[] xlsx = employeeExcelService.build(companyNumber, employeeNumber);
        String filename = EmployeeTransactionsExcelService.fileName(employeeNumber);
        return excelResponse(xlsx, filename);
    }

    @GetMapping("/dates/export")
    public ResponseEntity<byte[]> exportTransactionsBetweenDates(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) throws IOException {
        byte[] xlsx = dateRangeExcelService.build(startDate, endDate);
        String filename = DateRangeTransactionsExcelService.fileName(startDate, endDate);
        return excelResponse(xlsx, filename);
    }

    private ResponseEntity<byte[]> excelResponse(byte[] xlsx, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
