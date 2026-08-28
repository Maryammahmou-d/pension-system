package com.rubix.pension.reports.service;

import com.rubix.pension.financial_operations.repository.UnitPriceRepository;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.reports.dto.EmployeeExtractAppRowDto;
import com.rubix.pension.reports.sql.EmployeeExtractAppQueries;
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
public class EmployeeExtractAppReportService {

    public static final String UNIT_PRICE_MESSAGE = AggregatedBalanceReportService.UNIT_PRICE_MESSAGE;
    public static final String LAST_MONTH_UNIT_PRICE_MESSAGE =
            " There is no unit price for Last Month from that date.";
    public static final String NO_DATA_MESSAGE = "No records found for the selected criteria.";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UnitPriceRepository unitPriceRepository;
    private final FundHoldingsSupport fundHoldingsSupport;

    public EmployeeExtractAppReportService(
            NamedParameterJdbcTemplate jdbcTemplate,
            UnitPriceRepository unitPriceRepository,
            FundHoldingsSupport fundHoldingsSupport
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.unitPriceRepository = unitPriceRepository;
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    public List<EmployeeExtractAppRowDto> generate(String reportDateRaw) {
        fundHoldingsSupport.requireNonBlank(reportDateRaw);
        LocalDate reportDate = fundHoldingsSupport.parseRequiredDate(reportDateRaw);
        LocalDate lastMonthDate = reportDate.minusMonths(1);

        requireUnitPrice(reportDate, UNIT_PRICE_MESSAGE);
        requireUnitPrice(lastMonthDate, LAST_MONTH_UNIT_PRICE_MESSAGE);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reportDate", reportDate)
                .addValue("lastMonthDate", lastMonthDate);

        List<Map<String, Object>> rawRows =
                jdbcTemplate.queryForList(EmployeeExtractAppQueries.TEMP_APP_DATA_SQL, params);
        if (rawRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_DATA_MESSAGE);
        }

        List<EmployeeExtractAppRowDto> rows = new ArrayList<>(rawRows.size());
        for (Map<String, Object> raw : rawRows) {
            rows.add(mapRow(raw));
        }
        return rows;
    }

    private void requireUnitPrice(LocalDate date, String message) {
        if (unitPriceRepository.countByCalendarDate(date) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static EmployeeExtractAppRowDto mapRow(Map<String, Object> raw) {
        EmployeeExtractAppRowDto row = new EmployeeExtractAppRowDto();
        row.setCompanyNumber(asString(raw.get("company_number")));
        row.setCompanyName(asString(raw.get("company_name")));
        row.setEmployeeNumber(asString(raw.get("employee_number")));
        row.setFullName(asString(raw.get("full_name")));
        row.setNationalId(asString(raw.get("national_id")));
        row.setDob(asLocalDate(raw.get("dob")));
        row.setGender(asString(raw.get("gender")));
        row.setOccupation(asString(raw.get("occupation")));
        row.setPensionStartDate(asLocalDate(raw.get("pension_start_date")));
        row.setEmail(asString(raw.get("email")));
        row.setTerminationDate(asLocalDate(raw.get("termination_date")));
        row.setAvailableWithdrawal(asNullableDouble(raw.get("available_withdrawal")));
        row.setTotalFundValue(asDouble(raw.get("total_fund_value")));
        row.setGrossEeContribution(asDouble(raw.get("gross_ee_contribution")));
        row.setGrossVeeContribution(asDouble(raw.get("gross_vee_contribution")));
        row.setGrossErContribution(asDouble(raw.get("gross_er_contribution")));
        row.setNetEeContribution(asDouble(raw.get("net_ee_contribution")));
        row.setNetVeeContribution(asDouble(raw.get("net_vee_contribution")));
        row.setNetErContribution(asDouble(raw.get("net_er_contribution")));
        row.setGainValue(asDouble(raw.get("gain_value")));
        row.setPercentageGainUnitPrice(asDouble(raw.get("percentage_gain_unit_price")));
        row.setPercentageGainPaid(asNullableDouble(raw.get("percentage_gain_paid")));
        row.setPriceDate(asLocalDate(raw.get("price_date")));
        return row;
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static double asDouble(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static Double asNullableDouble(Object value) {
        if (value == null) {
            return null;
        }
        return asDouble(value);
    }

    private static LocalDate asLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        String text = String.valueOf(value).trim();
        if (text.length() >= 10) {
            try {
                return LocalDate.parse(text.substring(0, 10));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
