package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.dto.CompanyFundsExtractResponse;
import com.rubix.pension.reports.dto.CompanyFundsRequest;
import com.rubix.pension.reports.service.CompanyFundsExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint that replaces the legacy Access "Extract_Funds_Excel_Click" form handler.
 * Given a valuation date (and optionally an output directory), it produces the
 * Funds_Report_yyyymmdd.xlsx file that matches the TempFunds staging table layout.
 */
@RestController
@RequestMapping("/api/funds")
public class CompanyFundsController {

    private final CompanyFundsExcelService excelService;

    public CompanyFundsController(CompanyFundsExcelService excelService) {
        this.excelService = excelService;
    }

    /**
     * Generates the report and returns it as a downloadable Excel attachment.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFundsGet(@RequestParam Map<String, String> parameters) throws IOException {
        return downloadResponse(parseDate(findParameter(parameters,
                "valuationDate", "valuation_date", "Valuation_Date", "ValuationDate", "date", "valDate", "ValDate")));
    }

    /**
     * Generates the report and returns it as a downloadable Excel attachment.
     */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportFundsPost(@RequestBody CompanyFundsRequest request) throws IOException {
        return downloadResponse(parseDate(request.getValuationDate()));
    }

    /**
     * Generates the report and writes it to disk.
     * Example: /api/funds/extract?valuationDate=2026-04-30&outputPath=C:\Users\...\Downloads
     */
    @GetMapping("/extract")
    public ResponseEntity<?> extractFundsGet(@RequestParam Map<String, String> parameters) throws IOException {
        String valuationDate = findParameter(parameters,
                "valuationDate", "valuation_date", "Valuation_Date", "ValuationDate", "date", "valDate", "ValDate");
        String outputPath = findParameter(parameters,
                "outputPath", "output_path", "OutputPath", "path", "url", "URL");
        return extractFunds(parseDate(valuationDate), outputPath);
    }

    /**
     * Generates the report and writes it to disk.
     */
    @PostMapping("/extract")
    public ResponseEntity<?> extractFundsPost(@RequestBody CompanyFundsRequest request) throws IOException {
        return extractFunds(parseDate(request.getValuationDate()), request.getOutputPath());
    }

    private ResponseEntity<?> extractFunds(LocalDate date, String outputPath) throws IOException {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            return downloadResponse(date);
        }
        Path target = resolveOutputPath(date, outputPath);
        CompanyFundsExcelService.ExtractResult result = excelService.extract(date, target);
        return ResponseEntity.ok(new CompanyFundsExtractResponse(result.getFilePath(), result.getRecordCount()));
    }

    private ResponseEntity<byte[]> downloadResponse(LocalDate date) throws IOException {
        byte[] xlsx = excelService.build(date);
        String filename = CompanyFundsExcelService.fileName(date);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    );

    private String findParameter(Map<String, String> parameters, String... names) {
        for (String name : names) {
            String value = parameters.get(name);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private LocalDate parseDate(String valuationDate) {
        if (valuationDate == null || valuationDate.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valuation date is required.");
        }
        String trimmed = valuationDate.trim();
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_ZONED_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(trimmed).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Valuation date '" + trimmed + "' is not supported. Use yyyy-MM-dd (e.g. 2026-04-30)."
        );
    }

    private Path resolveOutputPath(LocalDate date, String outputPath) {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Output path is required.");
        }
        String normalized = outputPath.trim();
        if (normalized.toLowerCase().endsWith(".xlsx")) {
            return Path.of(normalized);
        }
        return Path.of(normalized).resolve(CompanyFundsExcelService.fileName(date));
    }
}
