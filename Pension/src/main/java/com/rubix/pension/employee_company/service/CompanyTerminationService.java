package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.BulkTerminationRequest;
import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.BulkTerminationRow;
import com.rubix.pension.employee_company.dto.CompanyTerminationRequest;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
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
import java.util.List;

@Service
public class CompanyTerminationService {

    private static final String PENDING_INVOICE_SQL = """
            SELECT COUNT(*) AS cnt
            FROM "LatestInvoiceRecords"
            WHERE "Company_Number" = :companyNumber
              AND "Status" = 'Pending'
            """;

    private final CompanyRepository companyRepository;
    private final EmployeeService employeeService;
    private final EmployeeTerminationService employeeTerminationService;
    private final FundHoldingsSupport fundHoldingsSupport;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CompanyTerminationService(
            CompanyRepository companyRepository,
            EmployeeService employeeService,
            EmployeeTerminationService employeeTerminationService,
            FundHoldingsSupport fundHoldingsSupport,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.companyRepository = companyRepository;
        this.employeeService = employeeService;
        this.employeeTerminationService = employeeTerminationService;
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public BulkTerminationResultDto terminate(CompanyTerminationRequest request) {
        if (request == null
                || isBlank(request.getCompanyNumber())
                || isBlank(request.getTerminationDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }

        LocalDate terminationDate = fundHoldingsSupport.parseRequiredDate(request.getTerminationDate());
        String companyNumber = request.getCompanyNumber().trim();

        Company company = companyRepository.findLatestByCompanyNumber(companyNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found."));

        if (company.getTerminationDate() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company " + companyNumber + " is already terminated.");
        }

        fundHoldingsSupport.requireUnitPrice(terminationDate);

        OffsetDateTime terminationTs = OffsetDateTime.of(terminationDate, LocalTime.MIN, ZoneOffset.ofHours(3));

        List<Employee> activeEmployees = employeeService.getActiveEmployeesByCompanyNumber(companyNumber);
        if (activeEmployees.isEmpty()) {
            saveCompanyTermination(company, terminationTs);
            BulkTerminationResultDto empty = new BulkTerminationResultDto();
            empty.setCompanyNumber(companyNumber);
            empty.setCompanyName(blank(company.getCompanyName()));
            empty.setSummaryFileName("Termination_" + companyNumber + ".xlsx");
            empty.setMessage("This company does not have active employee rows.");
            empty.setTotalRows(0);
            empty.setTerminatedCount(0);
            return empty;
        }

        if (hasPendingInvoices(companyNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There are pending invoices for this company. Please resolve them before terminating the company."
            );
        }

        saveCompanyTermination(company, terminationTs);

        BulkTerminationRequest bulk = new BulkTerminationRequest();
        bulk.setCompanyNumber(companyNumber);
        bulk.setUserName(request.getUserName());
        for (Employee employee : activeEmployees) {
            BulkTerminationRow row = new BulkTerminationRow();
            row.setEmployeeNumber(employee.getEmployeeNumber());
            row.setTerminationDate(request.getTerminationDate());
            row.setResignationDate(request.getResignationDate());
            bulk.getRows().add(row);
        }

        return employeeTerminationService.terminateBulk(bulk);
    }

    private void saveCompanyTermination(Company company, OffsetDateTime terminationDate) {
        Company newCompany = new Company();
        BeanUtils.copyProperties(company, newCompany, "id");
        newCompany.setSerial((company.getSerial() == null ? 0 : company.getSerial()) + 1);
        newCompany.setModifiedDate(OffsetDateTime.now());
        newCompany.setTerminationDate(terminationDate);
        companyRepository.save(newCompany);
    }

    private boolean hasPendingInvoices(String companyNumber) {
        if (!hasView("LatestInvoiceRecords")) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                PENDING_INVOICE_SQL,
                new MapSqlParameterSource("companyNumber", companyNumber),
                Integer.class
        );
        return count != null && count > 0;
    }

    private final java.util.concurrent.ConcurrentHashMap<String, Boolean> viewExistsCache = new java.util.concurrent.ConcurrentHashMap<>();

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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }
}
