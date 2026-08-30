package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.service.InvoiceDetailsExcelService;
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
public class InvoiceDetailsReportController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final InvoiceDetailsExcelService invoiceDetailsExcelService;

    public InvoiceDetailsReportController(InvoiceDetailsExcelService invoiceDetailsExcelService) {
        this.invoiceDetailsExcelService = invoiceDetailsExcelService;
    }

    @GetMapping(
            value = "/invoice-details/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> getInvoiceDetailsExcel(@RequestParam String invoiceNumber) throws IOException {
        byte[] excel = invoiceDetailsExcelService.build(invoiceNumber);
        String filename = InvoiceDetailsExcelService.fileName(invoiceNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX_MEDIA_TYPE)
                .body(excel);
    }
}
