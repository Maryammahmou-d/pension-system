package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.MonthlyChargeRunDto;
import com.rubix.pension.financial_operations.dto.MonthlyChargesResultDto;
import com.rubix.pension.financial_operations.dto.MonthlyChargesRunRequest;
import com.rubix.pension.financial_operations.service.MonthlyChargesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monthly-charges")
public class MonthlyChargesController {

    private final MonthlyChargesService monthlyChargesService;

    public MonthlyChargesController(MonthlyChargesService monthlyChargesService) {
        this.monthlyChargesService = monthlyChargesService;
    }

    @GetMapping
    public List<MonthlyChargeRunDto> list() {
        return monthlyChargesService.listPrevious();
    }

    @PostMapping("/run")
    public MonthlyChargesResultDto run(@RequestBody MonthlyChargesRunRequest request) {
        return monthlyChargesService.run(request.getRunDate(), request.getMonth(), request.getYear());
    }
}
