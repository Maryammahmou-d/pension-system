package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.dto.AggregatedBalanceReportResponse;
import com.rubix.pension.reports.service.AggregatedBalancePdfService;
import com.rubix.pension.reports.service.AggregatedBalanceReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
public class AggregatedBalanceReportController {

    private final AggregatedBalanceReportService aggregatedBalanceReportService;
    private final AggregatedBalancePdfService aggregatedBalancePdfService;

    public AggregatedBalanceReportController(
            AggregatedBalanceReportService aggregatedBalanceReportService,
            AggregatedBalancePdfService aggregatedBalancePdfService
    ) {
        this.aggregatedBalanceReportService = aggregatedBalanceReportService;
        this.aggregatedBalancePdfService = aggregatedBalancePdfService;
    }

    @GetMapping("/aggregated-employee-balance")
    public AggregatedBalanceReportResponse getAggregatedEmployeeBalance(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate
    ) {
        return aggregatedBalanceReportService.generate(companyNumber, valuationDate);
    }

    @GetMapping(value = "/aggregated-employee-balance/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getAggregatedEmployeeBalancePdf(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate
    ) throws IOException {
        AggregatedBalanceReportResponse report = aggregatedBalanceReportService.generate(companyNumber, valuationDate);
        byte[] pdf = aggregatedBalancePdfService.build(report);
        String filename = AggregatedBalancePdfService.fileName(report.getValuationDate(), report.getCompanyNumber());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
