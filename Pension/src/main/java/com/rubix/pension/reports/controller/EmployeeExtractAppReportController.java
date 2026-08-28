package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.dto.EmployeeExtractAppRowDto;
import com.rubix.pension.reports.service.EmployeeExtractAppExcelService;
import com.rubix.pension.reports.service.EmployeeExtractAppReportService;
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
public class EmployeeExtractAppReportController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final EmployeeExtractAppReportService employeeExtractAppReportService;
    private final EmployeeExtractAppExcelService employeeExtractAppExcelService;

    public EmployeeExtractAppReportController(
            EmployeeExtractAppReportService employeeExtractAppReportService,
            EmployeeExtractAppExcelService employeeExtractAppExcelService
    ) {
        this.employeeExtractAppReportService = employeeExtractAppReportService;
        this.employeeExtractAppExcelService = employeeExtractAppExcelService;
    }

    @GetMapping(
            value = "/employee-extract-app/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> getEmployeeExtractAppExcel(@RequestParam String reportDate) throws IOException {
        List<EmployeeExtractAppRowDto> rows = employeeExtractAppReportService.generate(reportDate);
        byte[] excel = employeeExtractAppExcelService.build(rows);
        String filename = EmployeeExtractAppExcelService.fileName(reportDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX_MEDIA_TYPE)
                .body(excel);
    }
}
