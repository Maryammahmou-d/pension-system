package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.HrBalanceDashboardRunResponse;
import com.rubix.pension.reports.sql.HrBalanceDashboardQueries;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class HrBalanceDashboardService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public HrBalanceDashboardService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> listPreviousRuns() {
        return jdbcTemplate.query(
                HrBalanceDashboardQueries.PREVIOUS_RUNS_SQL,
                Map.of(),
                (rs, rowNum) -> {
                    java.sql.Timestamp ts = rs.getTimestamp("valuation_date");
                    if (ts != null) {
                        return ts.toLocalDateTime().toLocalDate().toString();
                    }
                    java.sql.Date d = rs.getDate("valuation_date");
                    return d != null ? d.toLocalDate().toString() : "";
                }
        ).stream().filter(s -> !s.isEmpty()).toList();
    }

    @Transactional
    public HrBalanceDashboardRunResponse run(String valuationDateStr) {
        if (valuationDateStr == null || valuationDateStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All fields are required");
        }

        LocalDate valuationDate;
        try {
            valuationDate = LocalDate.parse(valuationDateStr.trim());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid valuation date format");
        }

        // 1. Check unit price availability
        Integer upCount = jdbcTemplate.queryForObject(
                HrBalanceDashboardQueries.CHECK_UNIT_PRICE_SQL,
                new MapSqlParameterSource("valuationDate", valuationDate),
                Integer.class
        );
        if (upCount == null || upCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is no unit price available for this valuation date. Please enter a unit price first."
            );
        }

        // 2. Check if already calculated
        Integer existingCount = jdbcTemplate.queryForObject(
                HrBalanceDashboardQueries.CHECK_DUPLICATE_DASHBOARD_SQL,
                new MapSqlParameterSource("valuationDate", valuationDate),
                Integer.class
        );
        if (existingCount != null && existingCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Balance was already calculated for that day."
            );
        }

        LocalDate lm = valuationDate.withDayOfMonth(1).minusDays(1);

        MapSqlParameterSource hrDashParams = new MapSqlParameterSource()
                .addValue("valDate", valuationDate);

        MapSqlParameterSource monthlyParams = new MapSqlParameterSource()
                .addValue("tm", valuationDate)
                .addValue("lm", lm);

        MapSqlParameterSource annualizedGainParams = new MapSqlParameterSource()
                .addValue("valDate", valuationDate)
                .addValue("tm", valuationDate)
                .addValue("lm", lm);

        int hrDashboardInserted = jdbcTemplate.update(
                HrBalanceDashboardQueries.INSERT_HR_DASHBOARD_SQL,
                hrDashParams
        );

        int countsInserted = jdbcTemplate.update(
                HrBalanceDashboardQueries.INSERT_HR_DASHBOARD_COUNTS_SQL,
                hrDashParams
        );

        int monthlyInserted = jdbcTemplate.update(
                HrBalanceDashboardQueries.INSERT_MONTHLY_HR_DASHBOARD_SQL,
                monthlyParams
        );

        int annualizedInserted = jdbcTemplate.update(
                HrBalanceDashboardQueries.INSERT_UNIT_PRICE_ANNUALIZED_GAIN_SQL,
                annualizedGainParams
        );

        HrBalanceDashboardRunResponse response = new HrBalanceDashboardRunResponse();
        response.setValuationDate(valuationDate.toString());
        response.setHrDashboardRows(hrDashboardInserted);
        response.setHrDashboardCountsRows(countsInserted);
        response.setHrDashboardMonthlyRows(monthlyInserted);
        response.setUnitPriceAnnualizedGainRows(annualizedInserted);
        response.setPreviousRuns(listPreviousRuns());

        return response;
    }
}
