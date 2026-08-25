package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalEstimateDto;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalRequest;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalResultDto;
import com.rubix.pension.financial_operations.service.EmployeeWithdrawalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmployeeFundsController {

    private final EmployeeWithdrawalService employeeWithdrawalService;

    public EmployeeFundsController(EmployeeWithdrawalService employeeWithdrawalService) {
        this.employeeWithdrawalService = employeeWithdrawalService;
    }

    @GetMapping("/employee-withdrawal/estimate")
    public EmployeeWithdrawalEstimateDto estimateWithdrawal(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber,
            @RequestParam String withdrawalDate
    ) {
        return employeeWithdrawalService.estimate(companyNumber, employeeNumber, withdrawalDate);
    }

    @PostMapping("/employee-withdrawal")
    public EmployeeWithdrawalResultDto withdraw(@RequestBody EmployeeWithdrawalRequest request) {
        return employeeWithdrawalService.withdraw(request);
    }
}
