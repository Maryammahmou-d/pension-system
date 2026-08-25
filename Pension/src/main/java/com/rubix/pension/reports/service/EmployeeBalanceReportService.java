package com.rubix.pension.reports.service;

import com.rubix.pension.financial_operations.repository.UnitPriceRepository;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.reports.dto.EmployeeBalanceReportResponse;
import com.rubix.pension.reports.dto.EmployeeBalanceRowDto;
import com.rubix.pension.reports.dto.EmployeeBalanceTotalsDto;
import com.rubix.pension.reports.sql.EmployeeBalanceQueries;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeBalanceReportService {

    public static final String UNIT_PRICE_MESSAGE = AggregatedBalanceReportService.UNIT_PRICE_MESSAGE;
    public static final String NO_DATA_MESSAGE =
            "No data to export for this Employee. This employee might have joined after the Valuation date.";
    public static final String NO_COMPANY_DATA_MESSAGE = "No records found for the selected criteria.";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UnitPriceRepository unitPriceRepository;
    private final FundHoldingsSupport fundHoldingsSupport;

    public EmployeeBalanceReportService(
            NamedParameterJdbcTemplate jdbcTemplate,
            UnitPriceRepository unitPriceRepository,
            FundHoldingsSupport fundHoldingsSupport
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.unitPriceRepository = unitPriceRepository;
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    public EmployeeBalanceReportResponse generate(
            String companyNumber,
            String employeeNumber,
            String valuationDateRaw
    ) {
        fundHoldingsSupport.requireNonBlank(companyNumber, employeeNumber, valuationDateRaw);
        LocalDate valuationDate = fundHoldingsSupport.parseRequiredDate(valuationDateRaw);
        requireUnitPrice(valuationDate);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyNumber", companyNumber.trim())
                .addValue("employeeNumber", employeeNumber.trim())
                .addValue("valuationDate", valuationDate);

        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(EmployeeBalanceQueries.EMPLOYEE_BALANCE_SQL, params);
        if (rawRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_DATA_MESSAGE);
        }

        List<EmployeeBalanceRowDto> rows = new ArrayList<>(rawRows.size());
        EmployeeBalanceTotalsDto totals = new EmployeeBalanceTotalsDto();
        String fullName = null;
        String currency = null;
        Integer employeeId = null;

        for (Map<String, Object> raw : rawRows) {
            EmployeeBalanceRowDto row = mapRow(raw);
            rows.add(row);
            accumulateTotals(totals, row);

            if (fullName == null) {
                fullName = asRawString(raw.get("full_name"));
            }
            if (currency == null && asTrimmedString(raw.get("currency")) != null) {
                currency = asTrimmedString(raw.get("currency"));
            }
            if (employeeId == null) {
                employeeId = integer(raw.get("employee_id"));
            }
        }

        EmployeeBalanceReportResponse response = new EmployeeBalanceReportResponse();
        response.setCompanyNumber(companyNumber.trim());
        response.setEmployeeNumber(employeeNumber.trim());
        response.setEmployeeId(employeeId);
        response.setFullName(fullName);
        response.setCurrency(currency == null ? "EGP" : currency);
        response.setValuationDate(valuationDate.toString());
        response.setRows(rows);
        response.setTotals(totals);
        return response;
    }

    public List<String> listCompanyEmployeeNumbers(
            String companyNumber,
            String valuationDateRaw,
            boolean activeOnly
    ) {
        fundHoldingsSupport.requireNonBlank(companyNumber, valuationDateRaw);
        LocalDate valuationDate = fundHoldingsSupport.parseRequiredDate(valuationDateRaw);
        requireUnitPrice(valuationDate);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyNumber", companyNumber.trim())
                .addValue("valuationDate", valuationDate)
                .addValue("activeOnly", activeOnly);

        List<String> employeeNumbers = jdbcTemplate.query(
                EmployeeBalanceQueries.COMPANY_EMPLOYEE_NUMBERS_SQL,
                params,
                (rs, rowNum) -> rs.getString(1)
        );

        if (employeeNumbers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_COMPANY_DATA_MESSAGE);
        }
        return employeeNumbers;
    }

    private void requireUnitPrice(LocalDate valuationDate) {
        if (unitPriceRepository.countByCalendarDate(valuationDate) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, UNIT_PRICE_MESSAGE);
        }
    }

    private static EmployeeBalanceRowDto mapRow(Map<String, Object> raw) {
        EmployeeBalanceRowDto row = new EmployeeBalanceRowDto();
        row.setSerial(integer(raw.get("serial")));
        row.setPaymentDate(dateString(raw.get("payment_date")));
        row.setRetroDate(dateString(raw.get("retro_date")));
        row.setDescription(asTrimmedString(raw.get("new_description")));
        row.setGrossContributionEe(number(raw.get("gross_ee_contribution")));
        row.setGrossContributionVee(number(raw.get("gross_vee_contribution")));
        row.setGrossContributionEr(number(raw.get("gross_er_contribution")));
        row.setContributionEe(number(raw.get("contribution_ee")));
        row.setContributionVee(number(raw.get("contribution_vee")));
        row.setContributionEr(number(raw.get("contribution_er")));
        row.setTransactionalEeValue(number(raw.get("transactional_ee_value")));
        row.setTransactionalVeeValue(number(raw.get("transactional_vee_value")));
        row.setTransactionalErValue(number(raw.get("transactional_er_value")));
        row.setInvestmentReturnEe(number(raw.get("investment_return_ee")));
        row.setInvestmentReturnVee(number(raw.get("investment_return_vee")));
        row.setInvestmentReturnEr(number(raw.get("investment_return_er")));
        row.setTotalInvestmentReturn(number(raw.get("total_investment_return")));
        row.setInvestmentEe(number(raw.get("investment_ee")));
        row.setInvestmentVee(number(raw.get("investment_vee")));
        row.setInvestmentEr(number(raw.get("investment_er")));
        row.setInvestmentTotal(number(raw.get("investment_total")));
        return row;
    }

    private static void accumulateTotals(EmployeeBalanceTotalsDto totals, EmployeeBalanceRowDto row) {
        totals.setGrossContributionEe(totals.getGrossContributionEe() + row.getGrossContributionEe());
        totals.setGrossContributionVee(totals.getGrossContributionVee() + row.getGrossContributionVee());
        totals.setGrossContributionEr(totals.getGrossContributionEr() + row.getGrossContributionEr());
        totals.setContributionEe(totals.getContributionEe() + row.getContributionEe());
        totals.setContributionVee(totals.getContributionVee() + row.getContributionVee());
        totals.setContributionEr(totals.getContributionEr() + row.getContributionEr());
        totals.setTransactionalEeValue(totals.getTransactionalEeValue() + row.getTransactionalEeValue());
        totals.setTransactionalVeeValue(totals.getTransactionalVeeValue() + row.getTransactionalVeeValue());
        totals.setTransactionalErValue(totals.getTransactionalErValue() + row.getTransactionalErValue());
        totals.setInvestmentReturnEe(totals.getInvestmentReturnEe() + row.getInvestmentReturnEe());
        totals.setInvestmentReturnVee(totals.getInvestmentReturnVee() + row.getInvestmentReturnVee());
        totals.setInvestmentReturnEr(totals.getInvestmentReturnEr() + row.getInvestmentReturnEr());
        totals.setTotalInvestmentReturn(totals.getTotalInvestmentReturn() + row.getTotalInvestmentReturn());
        totals.setInvestmentEe(totals.getInvestmentEe() + row.getInvestmentEe());
        totals.setInvestmentVee(totals.getInvestmentVee() + row.getInvestmentVee());
        totals.setInvestmentEr(totals.getInvestmentEr() + row.getInvestmentEr());
        totals.setInvestmentTotal(totals.getInvestmentTotal() + row.getInvestmentTotal());
    }

    private static String asRawString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String asTrimmedString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private static Integer integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private static double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }

    private static String dateString(Object value) {
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate().toString();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate().toString();
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toLocalDate().toString();
        }
        return value == null ? null : value.toString();
    }
}
