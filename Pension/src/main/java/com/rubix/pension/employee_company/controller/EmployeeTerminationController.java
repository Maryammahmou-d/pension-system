package com.rubix.pension.employee_company.controller;

import com.rubix.pension.employee_company.dto.BulkTerminationRequest;
import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.EmployeeTerminationEstimateDto;
import com.rubix.pension.employee_company.dto.EmployeeTerminationRequest;
import com.rubix.pension.employee_company.dto.TerminationReportDto;
import com.rubix.pension.employee_company.pdf.TerminationReportPdfService;
import com.rubix.pension.employee_company.service.EmployeeTerminationService;
import com.rubix.pension.employee_company.service.TerminationBundleService;
import com.rubix.pension.employee_company.service.TerminationFileSaveSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/employee-termination")
public class EmployeeTerminationController {

    private final EmployeeTerminationService employeeTerminationService;
    private final TerminationReportPdfService terminationReportPdfService;
    private final TerminationBundleService terminationBundleService;
    private final TerminationFileSaveSupport terminationFileSaveSupport;

    public EmployeeTerminationController(
            EmployeeTerminationService employeeTerminationService,
            TerminationReportPdfService terminationReportPdfService,
            TerminationBundleService terminationBundleService,
            TerminationFileSaveSupport terminationFileSaveSupport
    ) {
        this.employeeTerminationService = employeeTerminationService;
        this.terminationReportPdfService = terminationReportPdfService;
        this.terminationBundleService = terminationBundleService;
        this.terminationFileSaveSupport = terminationFileSaveSupport;
    }

    @GetMapping("/estimate")
    public EmployeeTerminationEstimateDto estimateTermination(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber,
            @RequestParam String terminationDate
    ) {
        return employeeTerminationService.estimate(companyNumber, employeeNumber, terminationDate);
    }

    /**
     * Terminates the employee and returns the termination statement PDF directly (generated
     * server-side, ready to download) — no separate frontend rendering step required. If the
     * request includes a (non-blank) {@code savePath}, the PDF is also written to that folder on
     * the server; {@code savePath} is optional and may be null.
     */
    @PostMapping
    public ResponseEntity<byte[]> terminate(@RequestBody EmployeeTerminationRequest request) throws IOException {
        TerminationReportDto report = employeeTerminationService.terminate(request);
        byte[] pdf = terminationReportPdfService.build(report);
        String filename = TerminationReportPdfService.fileName(report);
        terminationFileSaveSupport.save(request.getSavePath(), filename, pdf);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Terminates the employees and returns the JSON result as before. If the request includes a
     * (non-blank) {@code savePath}, also generates and writes every artifact (one PDF per
     * terminated employee, one PDF per affected fund, and both Excel workbooks) to that folder on
     * the server; {@code savePath} is optional and may be null.
     */
    @PostMapping("/bulk")
    public BulkTerminationResultDto terminateBulk(@RequestBody BulkTerminationRequest request) throws IOException {
        BulkTerminationResultDto result = employeeTerminationService.terminateBulk(request);
        terminationBundleService.saveToDirectory(result, request.getSavePath());
        return result;
    }

    /**
     * Stateless render: turns an already-computed {@link TerminationReportDto} (returned by
     * {@code terminate} or found in a bulk result's {@code reports}) into a downloadable PDF.
     * Does not re-run termination.
     */
    @PostMapping("/report/pdf")
    public ResponseEntity<byte[]> renderReportPdf(@RequestBody TerminationReportDto report) throws IOException {
        byte[] pdf = terminationReportPdfService.build(report);
        String filename = TerminationReportPdfService.fileName(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Stateless render: turns an already-computed {@link BulkTerminationResultDto} (returned by
     * {@code /bulk}, or by {@code /api/company-termination}) into a ZIP containing one PDF per
     * terminated employee, one PDF per affected fund, and the summary Excel workbook.
     * Does not re-run termination.
     */
    @PostMapping("/bulk/files")
    public ResponseEntity<byte[]> renderBulkFiles(@RequestBody BulkTerminationResultDto result) throws IOException {
        byte[] zip = terminationBundleService.buildZip(result);
        String filename = TerminationBundleService.zipFileName(result.getCompanyNumber());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf("application/zip"))
                .body(zip);
    }
}
