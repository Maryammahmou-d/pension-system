package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.EmployeeTerminationEstimateDto;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalEstimateDto;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalRequest;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalResultDto;
import com.rubix.pension.financial_operations.service.EmployeeTerminationService;
import com.rubix.pension.financial_operations.service.EmployeeWithdrawalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmployeeFundsController {

    private final EmployeeWithdrawalService employeeWithdrawalService;
    private final EmployeeTerminationService employeeTerminationService;

    public EmployeeFundsController(
            EmployeeWithdrawalService employeeWithdrawalService,
            EmployeeTerminationService employeeTerminationService
    ) {
        this.employeeWithdrawalService = employeeWithdrawalService;
        this.employeeTerminationService = employeeTerminationService;
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

    @GetMapping("/employee-termination/estimate")
    public EmployeeTerminationEstimateDto estimateTermination(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber,
            @RequestParam String terminationDate
    ) {
        return employeeTerminationService.estimate(companyNumber, employeeNumber, terminationDate);
    }
}
