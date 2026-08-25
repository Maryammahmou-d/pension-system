package com.rubix.pension.employee_company.controller;

import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.CompanyTerminationRequest;
import com.rubix.pension.employee_company.dto.TerminationFundReportDto;
import com.rubix.pension.employee_company.pdf.TerminationFundReportPdfService;
import com.rubix.pension.employee_company.service.CompanyTerminationService;
import com.rubix.pension.employee_company.service.TerminationBundleService;
import com.rubix.pension.employee_company.service.TerminationExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/company-termination")
public class CompanyTerminationController {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final CompanyTerminationService companyTerminationService;
    private final TerminationFundReportPdfService terminationFundReportPdfService;
    private final TerminationExcelService terminationExcelService;
    private final TerminationBundleService terminationBundleService;

    public CompanyTerminationController(
            CompanyTerminationService companyTerminationService,
            TerminationFundReportPdfService terminationFundReportPdfService,
            TerminationExcelService terminationExcelService,
            TerminationBundleService terminationBundleService
    ) {
        this.companyTerminationService = companyTerminationService;
        this.terminationFundReportPdfService = terminationFundReportPdfService;
        this.terminationExcelService = terminationExcelService;
        this.terminationBundleService = terminationBundleService;
    }

    /**
     * Terminates the company and returns the per-employee termination summary workbook directly
     * (generated server-side, ready to download) — no separate frontend rendering step required.
     * If the request includes a (non-blank) {@code savePath}, every artifact for the termination
     * (per-employee PDFs, per-fund PDFs, and both Excel workbooks — this summary workbook
     * included) is also written to that folder on the server; {@code savePath} is optional and
     * may be null.
     */
    @PostMapping
    public ResponseEntity<byte[]> terminateCompany(@RequestBody CompanyTerminationRequest request) throws IOException {
        BulkTerminationResultDto result = companyTerminationService.terminate(request);
        byte[] xlsx = terminationExcelService.build(result);
        String filename = TerminationExcelService.fileName(result.getCompanyNumber());
        terminationBundleService.saveToDirectory(result, request.getSavePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf(XLSX_CONTENT_TYPE))
                .body(xlsx);
    }

    /**
     * Stateless render: turns an already-computed {@link TerminationFundReportDto} (found in a
     * bulk/company termination result's {@code fundReports}) into a downloadable PDF listing every
     * employee terminated for that fund. Does not re-run termination.
     */
    @PostMapping("/report/pdf")
    public ResponseEntity<byte[]> renderFundReportPdf(@RequestBody TerminationFundReportDto fundReport) throws IOException {
        byte[] pdf = terminationFundReportPdfService.build(fundReport);
        String filename = TerminationFundReportPdfService.fileName(fundReport);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
