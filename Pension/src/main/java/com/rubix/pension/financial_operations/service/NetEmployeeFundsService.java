package com.rubix.pension.financial_operations.service;

import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.financial_operations.dto.NetEmployeeFundsResult;
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
public class NetEmployeeFundsService {

    private final FundHoldingsSupport fundHoldingsSupport;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public NetEmployeeFundsService(
            FundHoldingsSupport fundHoldingsSupport,
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository
    ) {
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
    }

    public NetEmployeeFundsResult calculate(String companyNumber, String employeeNumber, String valuationDate) {
        fundHoldingsSupport.requireNonBlank(companyNumber, employeeNumber, valuationDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(valuationDate);
        String trimmedCompanyNumber = companyNumber.trim();
        String trimmedEmployeeNumber = employeeNumber.trim();

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(targetDate);
        List<Map<String, Object>> transactionRows = fundHoldingsSupport.fetchLatestHoldings(targetDate);

        String companyName = companyRepository.findLatestByCompanyNumber(trimmedCompanyNumber)
                .map(Company::getCompanyName)
                .orElse("");

        String employeeName = employeeRepository.findLatestByCompanyAndEmployeeNumber(
                        trimmedCompanyNumber,
                        trimmedEmployeeNumber
                )
                .map(Employee::getFullName)
                .orElse("");

        UnitSums sums = fundHoldingsSupport.aggregateUnits(
                transactionRows,
                row -> Objects.equals(FundHoldingsSupport.asString(row.get("company_number")), trimmedCompanyNumber)
                        && Objects.equals(FundHoldingsSupport.asString(row.get("employee_number")), trimmedEmployeeNumber)
        );
        FundNetBuildResult build = fundHoldingsSupport.buildFundNetSummaryRows(sums, unitPrice);

        NetEmployeeFundsResult result = new NetEmployeeFundsResult();
        result.setCompanyNumber(trimmedCompanyNumber);
        result.setCompanyName(companyName);
        result.setEmployeeNumber(trimmedEmployeeNumber);
        result.setEmployeeName(employeeName);
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
