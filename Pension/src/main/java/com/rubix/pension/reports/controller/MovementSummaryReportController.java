package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.dto.DailyMovementRowDto;
import com.rubix.pension.reports.dto.MovementBetweenDatesBalanceResponse;
import com.rubix.pension.reports.service.DailyMovementExcelService;
import com.rubix.pension.reports.service.DailyMovementPdfService;
import com.rubix.pension.reports.service.MovementBetweenDatesReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/reports/movement-summary")
public class MovementSummaryReportController {

    private final DailyMovementExcelService dailyExcelService;
    private final DailyMovementPdfService dailyPdfService;
    private final MovementBetweenDatesReportService betweenDatesService;

    public MovementSummaryReportController(
            DailyMovementExcelService dailyExcelService,
            DailyMovementPdfService dailyPdfService,
            MovementBetweenDatesReportService betweenDatesService
    ) {
        this.dailyExcelService = dailyExcelService;
        this.dailyPdfService = dailyPdfService;
        this.betweenDatesService = betweenDatesService;
    }

    @GetMapping("/day/data")
    public ResponseEntity<List<DailyMovementRowDto>> getDailyData(@RequestParam("date") String dateStr) {
        LocalDate date = parseDate(dateStr);
        return ResponseEntity.ok(dailyExcelService.fetchRows(date));
    }

    @GetMapping("/day/excel")
    public ResponseEntity<byte[]> exportDailyExcel(@RequestParam("date") String dateStr) throws IOException {
        LocalDate date = parseDate(dateStr);
        byte[] bytes = dailyExcelService.buildExcel(date);
        String filename = DailyMovementExcelService.excelFileName(date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/day/pdf")
    public ResponseEntity<byte[]> exportDailyPdf(@RequestParam("date") String dateStr) throws IOException {
        LocalDate date = parseDate(dateStr);
        byte[] bytes = dailyPdfService.buildPdf(date);
        String filename = DailyMovementExcelService.pdfFileName(date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/between-dates/balance")
    public ResponseEntity<MovementBetweenDatesBalanceResponse> getBalanceBetweenDates(
            @RequestParam("startDate") String startStr,
            @RequestParam("endDate") String endStr
    ) {
        LocalDate start = parseDate(startStr);
        LocalDate end = parseDate(endStr);
        return ResponseEntity.ok(betweenDatesService.showBalance(start, end));
    }

    @GetMapping("/between-dates/excel")
    public ResponseEntity<byte[]> exportBetweenDatesExcel(
            @RequestParam("startDate") String startStr,
            @RequestParam("endDate") String endStr
    ) throws IOException {
        LocalDate start = parseDate(startStr);
        LocalDate end = parseDate(endStr);
        byte[] bytes = betweenDatesService.buildExcel(start, end);
        String filename = MovementBetweenDatesReportService.excelFileName(start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required.");
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format: " + raw);
        }
    }
}
