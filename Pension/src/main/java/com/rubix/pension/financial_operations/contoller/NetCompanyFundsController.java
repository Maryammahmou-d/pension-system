package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.NetCompanyFundsResult;
import com.rubix.pension.financial_operations.service.NetCompanyFundsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/net-company-funds")
public class NetCompanyFundsController {

    private final NetCompanyFundsService netCompanyFundsService;

    public NetCompanyFundsController(NetCompanyFundsService netCompanyFundsService) {
        this.netCompanyFundsService = netCompanyFundsService;
    }

    @GetMapping("/modified")
    public NetCompanyFundsResult getModifiedDateFunds(
            @RequestParam String companyNumber,
            @RequestParam String valuationDate
    ) {
        return netCompanyFundsService.calculateModifiedDate(companyNumber, valuationDate);
    }
}
