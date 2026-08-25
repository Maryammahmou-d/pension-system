package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.BulkTerminationRequest;
import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.BulkTerminationRow;
import com.rubix.pension.employee_company.dto.EmployeeTerminationEstimateDto;
import com.rubix.pension.employee_company.dto.EmployeeTerminationRequest;
import com.rubix.pension.employee_company.dto.TerminationFundEmployeeRow;
import com.rubix.pension.employee_company.dto.TerminationFundReportDto;
import com.rubix.pension.employee_company.dto.TerminationFundRowDto;
import com.rubix.pension.employee_company.dto.TerminationReportDto;
import com.rubix.pension.employee_company.dto.TerminationReportFundRow;
import com.rubix.pension.employee_company.dto.TerminationSummaryRowDto;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.employee_company.repository.TempEmpTerminationsRepository;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.UnitSums;
import com.rubix.pension.financial_operations.support.VestingSupport;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class EmployeeTerminationService {

    private static final String ALREADY_TERMINATED_MESSAGE = " is already terminated";

    private static final String SNAPSHOT_SQL = """
            SELECT
                t."Serial" AS serial,
                t."Employee_ID" AS employee_id,
                t."Currency" AS currency,
                t."Total_EE_Units_F1" AS ee1, t."Total_EE_Units_F2" AS ee2, t."Total_EE_Units_F3" AS ee3,
                t."Total_EE_Units_F4" AS ee4, t."Total_EE_Units_F5" AS ee5, t."Total_EE_Units_F6" AS ee6,
                t."Total_EE_Units_F7" AS ee7, t."Total_EE_Units_F8" AS ee8, t."Total_EE_Units_F9" AS ee9,
                t."Total_EE_Units_F10" AS ee10,
                t."Total_VEE_Units_F1" AS vee1, t."Total_VEE_Units_F2" AS vee2, t."Total_VEE_Units_F3" AS vee3,
                t."Total_VEE_Units_F4" AS vee4, t."Total_VEE_Units_F5" AS vee5, t."Total_VEE_Units_F6" AS vee6,
                t."Total_VEE_Units_F7" AS vee7, t."Total_VEE_Units_F8" AS vee8, t."Total_VEE_Units_F9" AS vee9,
                t."Total_VEE_Units_F10" AS vee10,
                t."Total_ER_Units_F1" AS er1, t."Total_ER_Units_F2" AS er2, t."Total_ER_Units_F3" AS er3,
                t."Total_ER_Units_F4" AS er4, t."Total_ER_Units_F5" AS er5, t."Total_ER_Units_F6" AS er6,
                t."Total_ER_Units_F7" AS er7, t."Total_ER_Units_F8" AS er8, t."Total_ER_Units_F9" AS er9,
                t."Total_ER_Units_F10" AS er10
            FROM "Transactions" t
            WHERE t."Employee_Number" = :employeeNumber
            ORDER BY t."Serial" DESC NULLS LAST, t."ID" DESC
            LIMIT 1
            """;

    private static final String PENDING_INVOICE_SQL = """
            SELECT COUNT(*) AS cnt
            FROM "LatestInvoiceEmployeeRecords"
            WHERE "Employee_Number" = :employeeNumber
              AND "Status" = 'Pending'
            """;

    private final FundHoldingsSupport fundHoldingsSupport;
    private final VestingSupport vestingSupport;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final TempEmpTerminationsRepository tempEmpTerminationsRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private volatile Boolean useFullInsert;
    private final Object useFullInsertLock = new Object();
    private final java.util.concurrent.ConcurrentHashMap<String, Boolean> viewExistsCache = new java.util.concurrent.ConcurrentHashMap<>();

    public EmployeeTerminationService(
            FundHoldingsSupport fundHoldingsSupport,
            VestingSupport vestingSupport,
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            TempEmpTerminationsRepository tempEmpTerminationsRepository,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.vestingSupport = vestingSupport;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.tempEmpTerminationsRepository = tempEmpTerminationsRepository;
        this.jdbcTemplate = jdbcTemplate;
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

    @Transactional
    public TerminationReportDto terminate(EmployeeTerminationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        fundHoldingsSupport.requireNonBlank(request.getCompanyNumber(), request.getEmployeeNumber(), request.getTerminationDate());
        LocalDate terminationDate = fundHoldingsSupport.parseRequiredDate(request.getTerminationDate());
        LocalDate resignationDate;
        try {
            resignationDate = parseOptionalDate(request.getResignationDate());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid resignation date.");
        }

        String trimmedCompany = request.getCompanyNumber().trim();
        String trimmedEmployee = request.getEmployeeNumber().trim();

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(terminationDate);
        Employee employee = employeeRepository.findLatestByCompanyAndEmployeeNumber(trimmedCompany, trimmedEmployee)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE));
        Company company = companyRepository.findLatestByCompanyNumber(trimmedCompany)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE));
        if (employee.getTerminationDate() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, trimmedEmployee + ALREADY_TERMINATED_MESSAGE);
        }

        return process(company, employee, terminationDate, resignationDate, unitPrice,
                vestingSupport.findRule(trimmedCompany), blank(request.getUserName()));
    }

    @Transactional
    public BulkTerminationResultDto terminateBulk(BulkTerminationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        fundHoldingsSupport.requireNonBlank(request.getCompanyNumber());
        if (request.getRows().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file has no employees to terminate.");
        }
        String trimmedCompany = request.getCompanyNumber().trim();
        Company company = companyRepository.findLatestByCompanyNumber(trimmedCompany)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found."));

        List<PendingTermination> pending = validateBulk(trimmedCompany, request.getRows());

        com.rubix.pension.financial_operations.entity.VestingRule vestingRule = vestingSupport.findRule(trimmedCompany);
        String userName = blank(request.getUserName());
        List<TerminationReportDto> reports = new ArrayList<>();
        for (PendingTermination row : pending) {
            reports.add(process(company, row.employee(), row.terminationDate(), row.resignationDate(), row.unitPrice(), vestingRule, userName));
        }

        BulkTerminationResultDto result = new BulkTerminationResultDto();
        result.setCompanyNumber(trimmedCompany);
        result.setCompanyName(blank(company.getCompanyName()));
        result.setSummaryFileName("Termination_" + trimmedCompany + ".xlsx");
        result.setTotalRows(request.getRows().size());
        result.setTerminatedCount(reports.size());
        result.setReports(reports);
        result.setSummaryRows(buildSummaryRows(reports));
        result.setFundReports(buildFundReports(company, reports));
        for (TerminationReportDto report : reports) {
            result.setGrandTotalTransactionalEeValue(result.getGrandTotalTransactionalEeValue() + report.getTotalTransactionalEeValue());
            result.setGrandTotalTransactionalVeeValue(result.getGrandTotalTransactionalVeeValue() + report.getTotalTransactionalVeeValue());
            result.setGrandTotalTransactionalErValue(result.getGrandTotalTransactionalErValue() + report.getTotalTransactionalErValue());
            result.setGrandTotalTerminatedErValue(result.getGrandTotalTerminatedErValue() + report.getTotalTerminatedErValue());
            result.setGrandTotalTransactionalValue(result.getGrandTotalTransactionalValue() + report.getTotalTransactionalValue());
        }
        return result;
    }

    private List<PendingTermination> validateBulk(String companyNumber, List<BulkTerminationRow> rows) {
        List<PendingTermination> pending = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (int index = 0; index < rows.size(); index += 1) {
            BulkTerminationRow row = rows.get(index);
            String label = "Row " + (index + 2);
            if (row == null || isBlank(row.getEmployeeNumber())) {
                problems.add(label + ": Employee_Number is required.");
                continue;
            }
            String employeeNumber = row.getEmployeeNumber().trim();
            label = employeeNumber;
            if (!seen.add(employeeNumber)) {
                problems.add(label + ": duplicated in the uploaded file.");
                continue;
            }
            if (isBlank(row.getTerminationDate())) {
                problems.add(label + ": Termination_Date is required.");
                continue;
            }
            LocalDate terminationDate;
            LocalDate resignationDate;
            try {
                terminationDate = LocalDate.parse(row.getTerminationDate().trim());
                resignationDate = parseOptionalDate(row.getResignationDate());
            } catch (Exception ex) {
                problems.add(label + ": invalid date format. Use yyyy-MM-dd.");
                continue;
            }
            Employee employee = resolveEmployee(companyNumber, employeeNumber);
            if (employee == null) {
                problems.add(label + ": employee not found for company " + companyNumber + ".");
                continue;
            }
            if (employee.getTerminationDate() != null) {
                problems.add(label + ALREADY_TERMINATED_MESSAGE + ".");
                continue;
            }
            if (!fundHoldingsSupport.hasUnitPrice(terminationDate)) {
                problems.add(label + ": there is no unit price for " + terminationDate + ".");
                continue;
            }
            if (!hasTransactionHistory(employeeNumber)) {
                problems.add(label + ": no transaction history found.");
                continue;
            }
            if (hasPendingInvoices(employeeNumber)) {
                problems.add(label + ": there are pending invoices. Please resolve them before terminating the employee.");
                continue;
            }
            pending.add(new PendingTermination(
                    employeeNumber,
                    employee,
                    terminationDate,
                    resignationDate,
                    fundHoldingsSupport.requireUnitPrice(terminationDate)
            ));
        }

        if (!problems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No employee was terminated. Please fix the following and upload again: " + String.join(" | ", problems)
            );
        }
        return pending;
    }

    private Employee resolveEmployee(String companyNumber, String employeeNumber) {
        // Prefer the active latest record (same logic as /employee/company/{companyNumber}/active)
        Employee active = employeeRepository.findLatestEmployeesByCompanyNumber(companyNumber).stream()
                .filter(e -> Objects.equals(blank(e.getEmployeeNumber()), employeeNumber)
                        && e.getTerminationDate() == null)
                .findFirst()
                .orElse(null);
        if (active != null) {
            return active;
        }
        // Fallback to the absolute latest for a clearer "already terminated" or "not found" error
        return employeeRepository.findLatestByCompanyAndEmployeeNumber(companyNumber, employeeNumber)
                .orElseGet(() -> employeeRepository.findLatestByEmployeeNumber(employeeNumber)
                        .filter(found -> Objects.equals(blank(found.getCompanyNumber()), companyNumber))
                        .orElse(null));
    }

    private List<TerminationSummaryRowDto> buildSummaryRows(List<TerminationReportDto> reports) {
        List<TerminationSummaryRowDto> rows = new ArrayList<>();
        for (TerminationReportDto report : reports) {
            TerminationSummaryRowDto row = new TerminationSummaryRowDto();
            row.setDescription("Termination");
            row.setPaymentDate(report.getPaymentDate());
            row.setCompanyNumber(report.getCompanyNumber());
            row.setEmployeeId(report.getEmployeeId());
            row.setEmployeeNumber(report.getEmployeeNumber());
            row.setNationalId(report.getNationalId());
            row.setFullName(report.getEmployeeName());
            row.setDob(report.getDob());
            row.setGender(report.getGender());
            row.setCurrency(report.getCurrency());
            row.setTotalEeValue(report.getTotalTransactionalEeValue());
            row.setTotalVeeValue(report.getTotalTransactionalVeeValue());
            row.setTotalErValue(report.getTotalTransactionalErValue());
            row.setTotalTerminatedErValue(report.getTotalTerminatedErValue());
            row.setTotalValue(report.getTotalTransactionalValue());
            rows.add(row);
        }
        return rows;
    }

    private List<TerminationFundReportDto> buildFundReports(Company company, List<TerminationReportDto> reports) {
        List<TerminationFundReportDto> fundReports = new ArrayList<>();
        for (int i = 1; i <= 10; i += 1) {
            TerminationFundReportDto fundReport = new TerminationFundReportDto();
            fundReport.setFund(i);
            fundReport.setCompanyNumber(blank(company.getCompanyNumber()));
            fundReport.setCompanyName(blank(company.getCompanyName()));
            boolean affected = false;

            for (TerminationReportDto report : reports) {
                TerminationReportFundRow row = report.getRows().get(i - 1);
                if (row.getStartingTotalUnits() == 0 && row.getTransactionalTotalUnits() == 0) {
                    continue;
                }
                affected = true;
                fundReport.setUnitPrice(row.getUnitPrice());
                fundReport.setTerminationDate(report.getTerminationDate());

                TerminationFundEmployeeRow employeeRow = new TerminationFundEmployeeRow();
                employeeRow.setEmployeeNumber(report.getEmployeeNumber());
                employeeRow.setEmployeeName(report.getEmployeeName());
                employeeRow.setDob(report.getDob());
                employeeRow.setCurrency(report.getCurrency());
                employeeRow.setTerminationDate(report.getTerminationDate());
                employeeRow.setStartingEeUnits(row.getStartingEeUnits());
                employeeRow.setStartingVeeUnits(row.getStartingVeeUnits());
                employeeRow.setStartingErUnits(row.getStartingErUnits());
                employeeRow.setTransactionalEeUnits(row.getTransactionalEeUnits());
                employeeRow.setTransactionalVeeUnits(row.getTransactionalVeeUnits());
                employeeRow.setTransactionalErUnits(row.getTransactionalErUnits());
                employeeRow.setTerminatedErUnits(row.getTerminatedErUnits());
                employeeRow.setTransactionalTotalUnits(row.getTransactionalTotalUnits());
                employeeRow.setTransactionalEeValue(row.getTransactionalEeValue());
                employeeRow.setTransactionalVeeValue(row.getTransactionalVeeValue());
                employeeRow.setTransactionalErValue(row.getTransactionalErValue());
                employeeRow.setTerminatedErValue(row.getTerminatedErValue());
                employeeRow.setTransactionalTotalValue(row.getTransactionalTotalValue());
                fundReport.getEmployees().add(employeeRow);

                fundReport.setTotalStartingEeUnits(fundReport.getTotalStartingEeUnits() + row.getStartingEeUnits());
                fundReport.setTotalStartingVeeUnits(fundReport.getTotalStartingVeeUnits() + row.getStartingVeeUnits());
                fundReport.setTotalStartingErUnits(fundReport.getTotalStartingErUnits() + row.getStartingErUnits());
                fundReport.setTotalTransactionalEeUnits(fundReport.getTotalTransactionalEeUnits() + row.getTransactionalEeUnits());
                fundReport.setTotalTransactionalVeeUnits(fundReport.getTotalTransactionalVeeUnits() + row.getTransactionalVeeUnits());
                fundReport.setTotalTransactionalErUnits(fundReport.getTotalTransactionalErUnits() + row.getTransactionalErUnits());
                fundReport.setTotalTerminatedErUnits(fundReport.getTotalTerminatedErUnits() + row.getTerminatedErUnits());
                fundReport.setTotalTransactionalUnits(fundReport.getTotalTransactionalUnits() + row.getTransactionalTotalUnits());
                fundReport.setTotalTransactionalEeValue(fundReport.getTotalTransactionalEeValue() + row.getTransactionalEeValue());
                fundReport.setTotalTransactionalVeeValue(fundReport.getTotalTransactionalVeeValue() + row.getTransactionalVeeValue());
                fundReport.setTotalTransactionalErValue(fundReport.getTotalTransactionalErValue() + row.getTransactionalErValue());
                fundReport.setTotalTerminatedErValue(fundReport.getTotalTerminatedErValue() + row.getTerminatedErValue());
                fundReport.setTotalTransactionalValue(fundReport.getTotalTransactionalValue() + row.getTransactionalTotalValue());
            }

            if (affected) {
                fundReport.setEmployeeCount(fundReport.getEmployees().size());
                fundReports.add(fundReport);
            }
        }
        return fundReports;
    }

    private TerminationReportDto process(
            Company company,
            Employee employee,
            LocalDate terminationDate,
            LocalDate resignationDate,
            UnitPrice unitPrice,
            com.rubix.pension.financial_operations.entity.VestingRule vestingRule,
            String userName
    ) {
        String companyNumber = blank(company.getCompanyNumber());
        String employeeNumber = blank(employee.getEmployeeNumber());

        if (hasPendingInvoices(employeeNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There are pending invoices for employee " + employeeNumber + ". Please resolve them before terminating the employee."
            );
        }

        OffsetDateTime terminationTs = OffsetDateTime.of(terminationDate, LocalTime.MIN, ZoneOffset.ofHours(3));
        OffsetDateTime resignationTs = resignationDate == null
                ? null
                : OffsetDateTime.of(resignationDate, LocalTime.MIN, ZoneOffset.ofHours(3));
        tempEmpTerminationsRepository.save(employeeNumber, terminationTs, resignationTs);

        double vestingFraction = vestingSupport.vestingFraction(vestingRule, employee, terminationDate);
        double vestingPercent = vestingSupport.vestingPercent(vestingRule, employee, terminationDate);

        Map<String, Object> snapshot = latestSnapshot(employeeNumber);
        int nextSerial = ((Number) snapshot.getOrDefault("serial", 0)).intValue() + 1;

        double[] prices = fundHoldingsSupport.pricesOf(unitPrice);
        List<TerminationReportFundRow> rows = new ArrayList<>();

        double totalEeValue = 0;
        double totalVeeValue = 0;
        double totalErValue = 0;
        double totalTerminatedErValue = 0;
        double totalTransactionalValue = 0;

        OffsetDateTime modifiedDate = OffsetDateTime.now();
        StringBuilder coreColumns = new StringBuilder(
                "\"Serial\", \"Description\", \"Modified_Date\", \"Payment_Date\", \"Retro_Date\", \"Company_Number\", \"Employee_ID\", \"Employee_Number\", \"Currency\", \"UserName\"");
        StringBuilder coreValues = new StringBuilder(
                ":serial, 'Termination', :modifiedDate, CAST(:asOf AS date), CAST(:asOf AS date), :companyNumber, :employeeId, :employeeNumber, :currency, :userName");
        StringBuilder fullColumns = new StringBuilder(coreColumns);
        StringBuilder fullValues = new StringBuilder(coreValues);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("serial", nextSerial)
                .addValue("modifiedDate", modifiedDate)
                .addValue("asOf", terminationDate)
                .addValue("companyNumber", companyNumber)
                .addValue("employeeId", snapshot.get("employee_id"))
                .addValue("employeeNumber", employeeNumber)
                .addValue("currency", snapshot.get("currency"))
                .addValue("userName", blankToDefault(userName, employee.getUsername()));

        for (int i = 1; i <= 10; i += 1) {
            double startEe = fundHoldingsSupport.numberOrZero(snapshot.get("ee" + i));
            double startVee = fundHoldingsSupport.numberOrZero(snapshot.get("vee" + i));
            double startEr = fundHoldingsSupport.numberOrZero(snapshot.get("er" + i));
            double price = prices[i - 1];

            double txEeUnits = -startEe;
            double txVeeUnits = -startVee;
            double txErUnits = -startEr * vestingFraction;
            double terminatedErUnits = -startEr * (1 - vestingFraction);
            double transactionalTotalUnits = txEeUnits + txVeeUnits + txErUnits + terminatedErUnits;

            double txEeValue = txEeUnits * price;
            double txVeeValue = txVeeUnits * price;
            double txErValue = txErUnits * price;
            double terminatedErValue = terminatedErUnits * price;
            double transactionalTotalValue = txEeValue + txVeeValue + txErValue + terminatedErValue;

            TerminationReportFundRow reportRow = new TerminationReportFundRow();
            reportRow.setFund(i);
            reportRow.setStartingEeUnits(startEe);
            reportRow.setStartingVeeUnits(startVee);
            reportRow.setStartingErUnits(startEr);
            reportRow.setStartingTotalUnits(startEe + startVee + startEr);
            reportRow.setTransactionalEeUnits(txEeUnits);
            reportRow.setTransactionalVeeUnits(txVeeUnits);
            reportRow.setTransactionalErUnits(txErUnits);
            reportRow.setTerminatedErUnits(terminatedErUnits);
            reportRow.setTransactionalTotalUnits(transactionalTotalUnits);
            reportRow.setUnitPrice(price);
            reportRow.setTransactionalEeValue(txEeValue);
            reportRow.setTransactionalVeeValue(txVeeValue);
            reportRow.setTransactionalErValue(txErValue);
            reportRow.setTerminatedErValue(terminatedErValue);
            reportRow.setTransactionalTotalValue(transactionalTotalValue);
            rows.add(reportRow);

            totalEeValue += txEeValue;
            totalVeeValue += txVeeValue;
            totalErValue += txErValue;
            totalTerminatedErValue += terminatedErValue;
            totalTransactionalValue += transactionalTotalValue;

            addCol(coreColumns, coreValues, "UP" + i, "up" + i);
            params.addValue("up" + i, price);
            addCol(coreColumns, coreValues, "Starting_EE_Units_F" + i, "startEe" + i);
            params.addValue("startEe" + i, startEe);
            addCol(coreColumns, coreValues, "Starting_VEE_Units_F" + i, "startVee" + i);
            params.addValue("startVee" + i, startVee);
            addCol(coreColumns, coreValues, "Starting_ER_Units_F" + i, "startEr" + i);
            params.addValue("startEr" + i, startEr);
            addCol(coreColumns, coreValues, "Starting_Total_Units_F" + i, "startTot" + i);
            params.addValue("startTot" + i, startEe + startVee + startEr);
            addCol(coreColumns, coreValues, "Transactional_EE_Units_F" + i, "txEe" + i);
            params.addValue("txEe" + i, txEeUnits);
            addCol(coreColumns, coreValues, "Transactional_VEE_Units_F" + i, "txVee" + i);
            params.addValue("txVee" + i, txVeeUnits);
            addCol(coreColumns, coreValues, "Transactional_ER_Units_F" + i, "txEr" + i);
            params.addValue("txEr" + i, txErUnits);
            addCol(coreColumns, coreValues, "Transactional_Total_Units_F" + i, "txTot" + i);
            params.addValue("txTot" + i, transactionalTotalUnits);
            addCol(coreColumns, coreValues, "Total_EE_Units_F" + i, "totEe" + i);
            params.addValue("totEe" + i, 0.0);
            addCol(coreColumns, coreValues, "Total_VEE_Units_F" + i, "totVee" + i);
            params.addValue("totVee" + i, 0.0);
            addCol(coreColumns, coreValues, "Total_ER_Units_F" + i, "totEr" + i);
            params.addValue("totEr" + i, 0.0);
            addCol(coreColumns, coreValues, "Total_Units_F" + i, "tot" + i);
            params.addValue("tot" + i, 0.0);
            addCol(coreColumns, coreValues, "Transactional_EE_Value_F" + i, "txEeVal" + i);
            params.addValue("txEeVal" + i, txEeValue);
            addCol(coreColumns, coreValues, "Transactional_VEE_Value_F" + i, "txVeeVal" + i);
            params.addValue("txVeeVal" + i, txVeeValue);
            addCol(coreColumns, coreValues, "Transactional_ER_Value_F" + i, "txErVal" + i);
            params.addValue("txErVal" + i, txErValue);
            addCol(coreColumns, coreValues, "Transactional_Total_Value_F" + i, "txTotVal" + i);
            params.addValue("txTotVal" + i, transactionalTotalValue);

            addCol(fullColumns, fullValues, "Terminated_ER_Units_F" + i, "termEr" + i);
            params.addValue("termEr" + i, terminatedErUnits);
            addCol(fullColumns, fullValues, "Terminated_ER_Value_F" + i, "termErVal" + i);
            params.addValue("termErVal" + i, terminatedErValue);
        }

        addCol(coreColumns, coreValues, "Transactional_EE_Value", "txEeVal");
        params.addValue("txEeVal", totalEeValue);
        addCol(coreColumns, coreValues, "Transactional_VEE_Value", "txVeeVal");
        params.addValue("txVeeVal", totalVeeValue);
        addCol(coreColumns, coreValues, "Transactional_ER_Value", "txErVal");
        params.addValue("txErVal", totalErValue);
        addCol(coreColumns, coreValues, "Transactional_Total_Value", "txTotVal");
        params.addValue("txTotVal", totalTransactionalValue);

        addCol(fullColumns, fullValues, "Surrender_Charges_EE", "surrenderEe");
        params.addValue("surrenderEe", nz(company.getEmployeeSurrenderCharge()));
        addCol(fullColumns, fullValues, "Surrender_Charges_VEE", "surrenderVee");
        params.addValue("surrenderVee", 0.0);
        addCol(fullColumns, fullValues, "Surrender_Charges_ER", "surrenderEr");
        params.addValue("surrenderEr", 0.0);
        addCol(fullColumns, fullValues, "Terminated_ER_Value", "termErTot");
        params.addValue("termErTot", totalTerminatedErValue);

        Integer transactionId = insertTransaction(fullColumns, fullValues, coreColumns, coreValues, params);
        Employee newEmployee = saveEmployeeDates(employee, terminationTs, resignationTs);

        TerminationReportDto report = new TerminationReportDto();
        report.setId(transactionId);
        report.setModifiedDate(modifiedDate.toString());
        report.setSerial(nextSerial);
        report.setEmployeeSerial(newEmployee.getSerial());
        report.setReference("TR-" + employeeNumber + "-" + nextSerial);
        report.setCompanyNumber(companyNumber);
        report.setCompanyName(blank(company.getCompanyName()));
        report.setCompanyAddress(blank(company.getAddress()));
        report.setCompanyPhone(blank(company.getMobileNumber()));
        report.setEmployeeId(employee.getEmployeeId());
        report.setEmployeeNumber(employeeNumber);
        report.setEmployeeName(blank(employee.getFullName()));
        report.setNationalId(blank(employee.getNationalId()));
        report.setDob(isoDate(employee.getDob()));
        report.setGender(blank(employee.getGender()));
        report.setCategory(blank(employee.getCategory()));
        report.setCurrency(blank(employee.getSalaryCurrency()));
        report.setPensionStartDate(isoDate(employee.getPensionStartDate()));
        report.setTerminationDate(terminationDate.toString());
        report.setResignationDate(resignationDate == null ? "" : resignationDate.toString());
        report.setPaymentDate(terminationDate.toString());
        report.setVestingPercentage(vestingPercent);
        report.setSurrenderChargesEe(nz(company.getEmployeeSurrenderCharge()));
        report.setSurrenderChargesVee(0);
        report.setSurrenderChargesEr(0);
        report.setRows(rows);
        report.setTotalTransactionalEeValue(totalEeValue);
        report.setTotalTransactionalVeeValue(totalVeeValue);
        report.setTotalTransactionalErValue(totalErValue);
        report.setTotalTerminatedErValue(totalTerminatedErValue);
        report.setTotalTransactionalValue(totalTransactionalValue);
        return report;
    }

    private Integer insertTransaction(
            StringBuilder fullColumns,
            StringBuilder fullValues,
            StringBuilder coreColumns,
            StringBuilder coreValues,
            MapSqlParameterSource params
    ) {
        String sql = (useFullInsert()
                ? "INSERT INTO \"Transactions\" (" + fullColumns + ") VALUES (" + fullValues + ")"
                : "INSERT INTO \"Transactions\" (" + coreColumns + ") VALUES (" + coreValues + ")")
                + " RETURNING \"ID\"";
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    private boolean hasTransactionHistory(String employeeNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"Transactions\" WHERE \"Employee_Number\" = :employeeNumber",
                new MapSqlParameterSource("employeeNumber", employeeNumber),
                Integer.class
        );
        return count != null && count > 0;
    }

    private boolean hasPendingInvoices(String employeeNumber) {
        if (!hasView("LatestInvoiceEmployeeRecords")) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                PENDING_INVOICE_SQL,
                new MapSqlParameterSource("employeeNumber", employeeNumber),
                Integer.class
        );
        return count != null && count > 0;
    }

    private boolean hasView(String name) {
        return viewExistsCache.computeIfAbsent(name, key -> {
            String existing = jdbcTemplate.queryForObject(
                    "SELECT to_regclass(:name)::text",
                    new MapSqlParameterSource("name", "public.\"" + key + "\""),
                    String.class
            );
            return !isBlank(existing);
        });
    }

    private boolean useFullInsert() {
        Boolean value = useFullInsert;
        if (value == null) {
            synchronized (useFullInsertLock) {
                value = useFullInsert;
                if (value == null) {
                    value = hasColumn("Transactions", "Terminated_ER_Units_F1");
                    useFullInsert = value;
                }
            }
        }
        return value;
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_name = :tableName AND column_name = :columnName
                """,
                new MapSqlParameterSource()
                        .addValue("tableName", tableName)
                        .addValue("columnName", columnName),
                Integer.class
        );
        return count != null && count > 0;
    }

    private Map<String, Object> latestSnapshot(String employeeNumber) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                SNAPSHOT_SQL,
                new MapSqlParameterSource("employeeNumber", employeeNumber)
        );
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No transaction history found for this employee.");
        }
        return rows.get(0);
    }

    private Employee saveEmployeeDates(Employee employee, OffsetDateTime terminationDate, OffsetDateTime resignationDate) {
        Employee newEmployee = new Employee();
        BeanUtils.copyProperties(employee, newEmployee, "id");
        newEmployee.setSerial((employee.getSerial() == null ? 0 : employee.getSerial()) + 1);
        newEmployee.setModifiedDate(OffsetDateTime.now());
        newEmployee.setTerminationDate(OffsetDateTime.now());
        if (resignationDate != null) {
            newEmployee.setResignationDate(resignationDate);
        }
        return employeeRepository.save(newEmployee);
    }

    private LocalDate parseOptionalDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }

    private void addCol(StringBuilder columns, StringBuilder values, String column, String param) {
        columns.append(", \"").append(column).append('"');
        values.append(", :").append(param);
    }

    private String isoDate(OffsetDateTime value) {
        return value == null ? "" : value.toLocalDate().toString();
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToDefault(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return isBlank(fallback) ? "system" : fallback.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private double nz(Double value) {
        return value == null ? 0 : value;
    }

    private record PendingTermination(
            String employeeNumber,
            Employee employee,
            LocalDate terminationDate,
            LocalDate resignationDate,
            UnitPrice unitPrice
    ) {}
}
