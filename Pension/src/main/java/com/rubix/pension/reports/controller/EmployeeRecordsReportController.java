package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.service.EmployeeRecordsExcelService;
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
public class EmployeeRecordsReportController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final EmployeeRecordsExcelService employeeRecordsExcelService;

    public EmployeeRecordsReportController(EmployeeRecordsExcelService employeeRecordsExcelService) {
        this.employeeRecordsExcelService = employeeRecordsExcelService;
    }

    @GetMapping(
            value = "/employee-records/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> getEmployeeRecordsExcel(@RequestParam String companyNumber) throws IOException {
        byte[] excel = employeeRecordsExcelService.build(companyNumber);
        String filename = EmployeeRecordsExcelService.fileName(companyNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX_MEDIA_TYPE)
                .body(excel);
    }
}
