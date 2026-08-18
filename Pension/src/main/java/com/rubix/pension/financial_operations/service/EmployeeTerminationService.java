package com.rubix.pension.financial_operations.service;

import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.financial_operations.dto.EmployeeTerminationEstimateDto;
import com.rubix.pension.financial_operations.dto.TerminationFundRowDto;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.UnitSums;
import com.rubix.pension.financial_operations.support.VestingSupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EmployeeTerminationService {

    private final FundHoldingsSupport fundHoldingsSupport;
    private final VestingSupport vestingSupport;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeTerminationService(
            FundHoldingsSupport fundHoldingsSupport,
            VestingSupport vestingSupport,
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository
    ) {
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.vestingSupport = vestingSupport;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
    }

    public EmployeeTerminationEstimateDto estimate(String companyNumber, String employeeNumber, String terminationDate) {
        fundHoldingsSupport.requireNonBlank(companyNumber, employeeNumber, terminationDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(terminationDate);
        String trimmedCompany = companyNumber.trim();
        String trimmedEmployee = employeeNumber.trim();

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(targetDate);
        Employee employee = employeeRepository.findLatestByCompanyAndEmployeeNumber(trimmedCompany, trimmedEmployee)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE));
        String companyName = companyRepository.findLatestByCompanyNumber(trimmedCompany)
                .map(Company::getCompanyName)
                .orElse("");

        List<Map<String, Object>> rows = fundHoldingsSupport.fetchLatestHoldings(targetDate);
        UnitSums sums = fundHoldingsSupport.aggregateUnits(
                rows,
                row -> Objects.equals(FundHoldingsSupport.asString(row.get("company_number")), trimmedCompany)
                        && Objects.equals(FundHoldingsSupport.asString(row.get("employee_number")), trimmedEmployee)
        );

        double vesting = vestingSupport.vestingFraction(trimmedCompany, employee, targetDate);
        double[] prices = fundHoldingsSupport.pricesOf(unitPrice);

        List<TerminationFundRowDto> fundRows = new ArrayList<>();
        for (int i = 1; i <= 10; i += 1) {
            double eeUnits = sums.hasTransactions() ? sums.eeSums()[i - 1] : 0;
            double veeUnits = sums.hasTransactions() ? sums.veeSums()[i - 1] : 0;
            double erHoldings = sums.hasTransactions() ? sums.erSums()[i - 1] : 0;
            double erUnits = erHoldings * vesting;
            double terminatedErUnits = erHoldings * (1 - vesting);
            double totalUnits = eeUnits + veeUnits + erUnits + terminatedErUnits;

            TerminationFundRowDto row = new TerminationFundRowDto();
            row.setFund(i);
            row.setEeUnits(eeUnits);
            row.setVeeUnits(veeUnits);
            row.setErUnits(erUnits);
            row.setTerminatedErUnits(terminatedErUnits);
            row.setTotalUnits(totalUnits);
            row.setUnitPrice(prices[i - 1]);
            fundRows.add(row);
        }

        EmployeeTerminationEstimateDto result = new EmployeeTerminationEstimateDto();
        result.setCompanyNumber(trimmedCompany);
        result.setCompanyName(companyName);
        result.setEmployeeId(employee.getEmployeeId());
        result.setEmployeeNumber(trimmedEmployee);
        result.setEmployeeName(employee.getFullName() == null ? "" : employee.getFullName());
        result.setCurrency(employee.getSalaryCurrency() == null ? "" : employee.getSalaryCurrency());
        result.setPaymentDate(targetDate.toString());
        result.setTerminationDate(targetDate.toString());
        result.setRows(fundRows);
        return result;
    }
}
