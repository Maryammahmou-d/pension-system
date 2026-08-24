package com.rubix.pension.reports.service;

import com.rubix.pension.financial_operations.repository.UnitPriceRepository;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.reports.dto.AggregatedBalanceReportResponse;
import com.rubix.pension.reports.dto.AggregatedBalanceRowDto;
import com.rubix.pension.reports.dto.AggregatedBalanceTotalsDto;
import com.rubix.pension.reports.sort.AggregatedBalanceRowComparator;
import com.rubix.pension.reports.sql.AggregatedBalanceQueries;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AggregatedBalanceReportService {

    public static final String UNIT_PRICE_MESSAGE =
            "There is no unit price entered for this valuation date. Please enter a unit price first.";
    public static final String NO_DATA_MESSAGE =
            "No data to export for this Employee. This employee might have joined after the Valuation date.";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UnitPriceRepository unitPriceRepository;
    private final FundHoldingsSupport fundHoldingsSupport;

    public AggregatedBalanceReportService(
            NamedParameterJdbcTemplate jdbcTemplate,
            UnitPriceRepository unitPriceRepository,
            FundHoldingsSupport fundHoldingsSupport
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.unitPriceRepository = unitPriceRepository;
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    public AggregatedBalanceReportResponse generate(String companyNumber, String valuationDateRaw) {
        fundHoldingsSupport.requireNonBlank(companyNumber, valuationDateRaw);
        LocalDate valuationDate = fundHoldingsSupport.parseRequiredDate(valuationDateRaw);
        requireUnitPrice(valuationDate);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyNumber", companyNumber.trim())
                .addValue("valuationDate", valuationDate);

        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(
                AggregatedBalanceQueries.AGGREGATED_BALANCE_SQL,
                params
        );

        if (rawRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_DATA_MESSAGE);
        }

        List<AggregatedBalanceRowDto> rows = new ArrayList<>(rawRows.size());
        AggregatedBalanceTotalsDto totals = new AggregatedBalanceTotalsDto();
        String currency = null;

        for (Map<String, Object> raw : rawRows) {
            AggregatedBalanceRowDto row = mapRow(raw);
            rows.add(row);
            if (currency == null && row.getCurrency() != null && !row.getCurrency().isBlank()) {
                currency = row.getCurrency();
            }
            accumulateTotals(totals, row);
        }

        rows.sort(AggregatedBalanceRowComparator.INSTANCE);

        AggregatedBalanceReportResponse response = new AggregatedBalanceReportResponse();
        response.setCompanyNumber(companyNumber.trim());
        response.setValuationDate(valuationDate.toString());
        response.setCurrency(currency == null ? "EGP" : currency);
        response.setRows(rows);
        response.setTotals(totals);
        return response;
    }

    private void requireUnitPrice(LocalDate valuationDate) {
        if (unitPriceRepository.countByCalendarDate(valuationDate) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, UNIT_PRICE_MESSAGE);
        }
    }

    private AggregatedBalanceRowDto mapRow(Map<String, Object> raw) {
        AggregatedBalanceRowDto row = new AggregatedBalanceRowDto();
        row.setFullName(asRawString(raw.get("full_name")));
        row.setEmployeeNumber(asTrimmedString(raw.get("employee_number")));
        row.setCurrency(asTrimmedString(raw.get("currency")));
        row.setGrossContributionEe(number(raw.get("sum_of_gross_ee_contribution")));
        row.setGrossContributionVee(number(raw.get("sum_of_gross_vee_contribution")));
        row.setGrossContributionEr(number(raw.get("sum_of_gross_er_contribution")));
        row.setNetContributionEe(number(raw.get("sum_of_transactional_ee_value")));
        row.setNetContributionVee(number(raw.get("sum_of_transactional_vee_value")));
        row.setNetContributionEr(number(raw.get("sum_of_transactional_er_value")));
        row.setInvestmentReturnEe(number(raw.get("sum_of_investment_return_ee")));
        row.setInvestmentReturnVee(number(raw.get("sum_of_investment_return_vee")));
        row.setInvestmentReturnEr(number(raw.get("sum_of_investment_return_er")));
        row.setTotalInvestmentReturn(number(raw.get("sum_of_total_investment_return")));
        row.setAccumulatedValueEe(number(raw.get("sum_of_investment_ee")));
        row.setAccumulatedValueVee(number(raw.get("sum_of_investment_vee")));
        row.setAccumulatedValueEr(number(raw.get("sum_of_investment_er")));
        row.setAccumulatedValueTotal(number(raw.get("sum_of_investment_total")));
        return row;
    }

    private void accumulateTotals(AggregatedBalanceTotalsDto totals, AggregatedBalanceRowDto row) {
        totals.setGrossContributionEe(totals.getGrossContributionEe() + row.getGrossContributionEe());
        totals.setGrossContributionVee(totals.getGrossContributionVee() + row.getGrossContributionVee());
        totals.setGrossContributionEr(totals.getGrossContributionEr() + row.getGrossContributionEr());
        totals.setInvestmentReturnEe(totals.getInvestmentReturnEe() + row.getInvestmentReturnEe());
        totals.setInvestmentReturnVee(totals.getInvestmentReturnVee() + row.getInvestmentReturnVee());
        totals.setInvestmentReturnEr(totals.getInvestmentReturnEr() + row.getInvestmentReturnEr());
        totals.setTotalInvestmentReturn(totals.getTotalInvestmentReturn() + row.getTotalInvestmentReturn());
        totals.setAccumulatedValueEe(totals.getAccumulatedValueEe() + row.getAccumulatedValueEe());
        totals.setAccumulatedValueVee(totals.getAccumulatedValueVee() + row.getAccumulatedValueVee());
        totals.setAccumulatedValueEr(totals.getAccumulatedValueEr() + row.getAccumulatedValueEr());
        totals.setAccumulatedValueTotal(totals.getAccumulatedValueTotal() + row.getAccumulatedValueTotal());
    }

    private static String asRawString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String asTrimmedString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private static double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }
}
