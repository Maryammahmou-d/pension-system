package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.NetCompanyFundsResult;
import com.rubix.pension.financial_operations.dto.NetEmployeeFundsResult;
import com.rubix.pension.financial_operations.dto.NetFundsResult;
import com.rubix.pension.financial_operations.dto.NetUnitsResult;
import com.rubix.pension.financial_operations.service.NetCompanyFundsService;
import com.rubix.pension.financial_operations.service.NetEmployeeFundsService;
import com.rubix.pension.financial_operations.service.NetFundsService;
import com.rubix.pension.financial_operations.service.NetUnitsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NetReportsController {

    private final NetCompanyFundsService netCompanyFundsService;
    private final NetEmployeeFundsService netEmployeeFundsService;
    private final NetFundsService netFundsService;
    private final NetUnitsService netUnitsService;

    public NetReportsController(
            NetCompanyFundsService netCompanyFundsService,
            NetEmployeeFundsService netEmployeeFundsService,
            NetFundsService netFundsService,
            NetUnitsService netUnitsService
    ) {
        this.netCompanyFundsService = netCompanyFundsService;
        this.netEmployeeFundsService = netEmployeeFundsService;
        this.netFundsService = netFundsService;
        this.netUnitsService = netUnitsService;
    }

    @GetMapping("/net-company-funds/modified")
    public NetCompanyFundsResult getNetCompanyFundsModified(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate
    ) {
        return netCompanyFundsService.calculateModifiedDate(companyNumber, valuationDate);
    }

    @GetMapping("/net-employee-funds")
    public NetEmployeeFundsResult getNetEmployeeFunds(
            @RequestParam String companyNumber,
            @RequestParam String employeeNumber,
            @RequestParam String valuationDate
    ) {
        return netEmployeeFundsService.calculate(companyNumber, employeeNumber, valuationDate);
    }

    @GetMapping("/net-funds")
    public NetFundsResult getNetFunds(@RequestParam String valuationDate) {
        return netFundsService.calculate(valuationDate);
    }

    @GetMapping("/net-units")
    public NetUnitsResult getNetUnits(@RequestParam String valuationDate) {
        return netUnitsService.calculate(valuationDate);
    }
}
