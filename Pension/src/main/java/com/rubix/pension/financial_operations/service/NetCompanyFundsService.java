package com.rubix.pension.financial_operations.service;

import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.financial_operations.dto.NetCompanyFundsResult;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.FundNetBuildResult;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.UnitSums;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class NetCompanyFundsService {

    private final FundHoldingsSupport fundHoldingsSupport;
    private final CompanyRepository companyRepository;

    public NetCompanyFundsService(
            FundHoldingsSupport fundHoldingsSupport,
            CompanyRepository companyRepository
    ) {
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.companyRepository = companyRepository;
    }

    public NetCompanyFundsResult calculateModifiedDate(String companyNumber, String valuationDate) {
        fundHoldingsSupport.requireNonBlank(companyNumber, valuationDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(valuationDate);
        String trimmedCompanyNumber = companyNumber.trim();

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(targetDate);
        List<Map<String, Object>> transactionRows = fundHoldingsSupport.fetchLatestHoldings(targetDate);

        String companyName = companyRepository.findLatestByCompanyNumber(trimmedCompanyNumber)
                .map(Company::getCompanyName)
                .orElse("");

        UnitSums sums = fundHoldingsSupport.aggregateUnits(
                transactionRows,
                row -> Objects.equals(FundHoldingsSupport.asString(row.get("company_number")), trimmedCompanyNumber)
        );
        FundNetBuildResult build = fundHoldingsSupport.buildFundNetSummaryRows(sums, unitPrice);

        NetCompanyFundsResult result = new NetCompanyFundsResult();
        result.setCompanyNumber(trimmedCompanyNumber);
        result.setCompanyName(companyName);
        result.setValuationDate(targetDate.toString());
        result.setDateFinal(sums.hasTransactions() ? targetDate.toString() : null);
        result.setRows(build.rows());
        result.setTotalEEFunds(build.totalEEFunds());
        result.setTotalVEEFunds(build.totalVEEFunds());
        result.setTotalERFunds(build.totalERFunds());
        result.setTotalFunds(build.totalFunds());
        return result;
    }
}
