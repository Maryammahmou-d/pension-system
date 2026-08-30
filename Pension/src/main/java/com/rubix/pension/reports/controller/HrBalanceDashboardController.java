package com.rubix.pension.reports.controller;

import com.rubix.pension.reports.dto.HrBalanceDashboardRunResponse;
import com.rubix.pension.reports.service.HrBalanceDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports/hr-balance-dashboard")
public class HrBalanceDashboardController {

    private final HrBalanceDashboardService hrBalanceDashboardService;

    public HrBalanceDashboardController(HrBalanceDashboardService hrBalanceDashboardService) {
        this.hrBalanceDashboardService = hrBalanceDashboardService;
    }

    @GetMapping("/previous-runs")
    public ResponseEntity<List<String>> listPreviousRuns() {
        return ResponseEntity.ok(hrBalanceDashboardService.listPreviousRuns());
    }

    @PostMapping("/run")
    public ResponseEntity<HrBalanceDashboardRunResponse> run(
            @RequestParam String valuationDate
    ) {
        return ResponseEntity.ok(hrBalanceDashboardService.run(valuationDate));
    }
}
