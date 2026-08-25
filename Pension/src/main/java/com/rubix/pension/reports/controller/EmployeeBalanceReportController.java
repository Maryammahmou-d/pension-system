package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.dto.EmployeeBalanceReportResponse;
import com.rubix.pension.reports.service.CompanyBalanceZipService;
import com.rubix.pension.reports.service.EmployeeBalanceExcelService;
import com.rubix.pension.reports.service.EmployeeBalancePdfService;
import com.rubix.pension.reports.service.EmployeeBalanceReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class EmployeeBalanceReportController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    private static final MediaType ZIP_MEDIA_TYPE = MediaType.parseMediaType("application/zip");

    private final EmployeeBalanceReportService employeeBalanceReportService;
    private final EmployeeBalancePdfService employeeBalancePdfService;
    private final EmployeeBalanceExcelService employeeBalanceExcelService;
    private final CompanyBalanceZipService companyBalanceZipService;

    public EmployeeBalanceReportController(
            EmployeeBalanceReportService employeeBalanceReportService,
            EmployeeBalancePdfService employeeBalancePdfService,
            EmployeeBalanceExcelService employeeBalanceExcelService,
            CompanyBalanceZipService companyBalanceZipService
    ) {
        this.employeeBalanceReportService = employeeBalanceReportService;
        this.employeeBalancePdfService = employeeBalancePdfService;
        this.employeeBalanceExcelService = employeeBalanceExcelService;
        this.companyBalanceZipService = companyBalanceZipService;
    }

    @GetMapping(value = "/employee-balance/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getEmployeeBalancePdf(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber,
            @RequestParam String valuationDate
    ) throws IOException {
        EmployeeBalanceReportResponse report =
                employeeBalanceReportService.generate(companyNumber, employeeNumber, valuationDate);
        byte[] pdf = employeeBalancePdfService.build(report);
        String filename = EmployeeBalancePdfService.fileName(report.getValuationDate(), report.getEmployeeNumber());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(
            value = "/employee-balance/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> getEmployeeBalanceExcel(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber,
            @RequestParam String valuationDate
    ) throws IOException {
        EmployeeBalanceReportResponse report =
                employeeBalanceReportService.generate(companyNumber, employeeNumber, valuationDate);
        byte[] excel = employeeBalanceExcelService.build(report);
        String filename = EmployeeBalanceExcelService.fileName(report.getValuationDate(), report.getEmployeeNumber());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX_MEDIA_TYPE)
                .body(excel);
    }

    @GetMapping(value = "/company-balance/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> listCompanyBalanceEmployees(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return employeeBalanceReportService.listCompanyEmployeeNumbers(companyNumber, valuationDate, activeOnly);
    }

    @GetMapping(value = "/company-balance/pdf", produces = "application/zip")
    public ResponseEntity<byte[]> getCompanyBalancePdfZip(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) throws IOException {
        byte[] zip = companyBalanceZipService.buildPdfZip(companyNumber, valuationDate, activeOnly);
        String filename = CompanyBalanceZipService.fileName(valuationDate, companyNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(ZIP_MEDIA_TYPE)
                .body(zip);
    }

    @GetMapping(value = "/company-balance/excel", produces = "application/zip")
    public ResponseEntity<byte[]> getCompanyBalanceExcelZip(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) throws IOException {
        byte[] zip = companyBalanceZipService.buildExcelZip(companyNumber, valuationDate, activeOnly);
        String filename = CompanyBalanceZipService.fileName(valuationDate, companyNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(ZIP_MEDIA_TYPE)
                .body(zip);
    }
}
