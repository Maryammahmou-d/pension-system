package com.rubix.pension.financial_operations.support;

import com.rubix.pension.financial_operations.dto.FundNetSummary;
import com.rubix.pension.financial_operations.dto.FundUnitRow;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.repository.UnitPriceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
public class FundHoldingsSupport {

    public static final String REQUIRED_FIELDS_MESSAGE = "Please fill all required fields.";
    public static final String UNIT_PRICE_MESSAGE =
            " There is no unit price for that date. Please try again after the unit price is entered or choose a different date.";

    private static final String MODIFIED_ROWS_SQL = """
            SELECT
                t."Company_Number" AS company_number,
                t."Employee_Number" AS employee_number,
                t."Total_EE_Units_F1" AS ee1,
                t."Total_EE_Units_F2" AS ee2,
                t."Total_EE_Units_F3" AS ee3,
                t."Total_EE_Units_F4" AS ee4,
                t."Total_EE_Units_F5" AS ee5,
                t."Total_EE_Units_F6" AS ee6,
                t."Total_EE_Units_F7" AS ee7,
                t."Total_EE_Units_F8" AS ee8,
                t."Total_EE_Units_F9" AS ee9,
                t."Total_EE_Units_F10" AS ee10,
                t."Total_VEE_Units_F1" AS vee1,
                t."Total_VEE_Units_F2" AS vee2,
                t."Total_VEE_Units_F3" AS vee3,
                t."Total_VEE_Units_F4" AS vee4,
                t."Total_VEE_Units_F5" AS vee5,
                t."Total_VEE_Units_F6" AS vee6,
                t."Total_VEE_Units_F7" AS vee7,
                t."Total_VEE_Units_F8" AS vee8,
                t."Total_VEE_Units_F9" AS vee9,
                t."Total_VEE_Units_F10" AS vee10,
                t."Total_ER_Units_F1" AS er1,
                t."Total_ER_Units_F2" AS er2,
                t."Total_ER_Units_F3" AS er3,
                t."Total_ER_Units_F4" AS er4,
                t."Total_ER_Units_F5" AS er5,
                t."Total_ER_Units_F6" AS er6,
                t."Total_ER_Units_F7" AS er7,
                t."Total_ER_Units_F8" AS er8,
                t."Total_ER_Units_F9" AS er9,
                t."Total_ER_Units_F10" AS er10
            FROM "Transactions" t
            INNER JOIN (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Transactions"
                WHERE "Modified_Date" <= CAST(:asOf AS date)
                GROUP BY "Employee_Number"
            ) latest
              ON t."Employee_Number" = latest."Employee_Number"
             AND t."Serial" = latest.max_serial
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UnitPriceRepository unitPriceRepository;

    public FundHoldingsSupport(
            NamedParameterJdbcTemplate jdbcTemplate,
            UnitPriceRepository unitPriceRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.unitPriceRepository = unitPriceRepository;
    }

    public LocalDate parseRequiredDate(String dateValue) {
        if (isBlank(dateValue)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REQUIRED_FIELDS_MESSAGE);
        }
        try {
            return LocalDate.parse(dateValue.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REQUIRED_FIELDS_MESSAGE);
        }
    }

    public void requireNonBlank(String... values) {
        for (String value : values) {
            if (isBlank(value)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REQUIRED_FIELDS_MESSAGE);
            }
        }
    }

    public boolean hasUnitPrice(LocalDate targetDate) {
        return unitPriceRepository.countByCalendarDate(targetDate) > 0;
    }

    public UnitPrice requireUnitPrice(LocalDate targetDate) {
        if (unitPriceRepository.countByCalendarDate(targetDate) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, UNIT_PRICE_MESSAGE);
        }
        return unitPriceRepository.findLatestByCalendarDate(targetDate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, UNIT_PRICE_MESSAGE));
    }

    public List<Map<String, Object>> fetchLatestHoldings(LocalDate asOf) {
        return jdbcTemplate.queryForList(
                MODIFIED_ROWS_SQL,
                new MapSqlParameterSource("asOf", asOf)
        );
    }

    public UnitSums aggregateUnits(List<Map<String, Object>> rows, Predicate<Map<String, Object>> filter) {
        double[] eeSums = new double[10];
        double[] veeSums = new double[10];
        double[] erSums = new double[10];
        boolean hasTransactions = !rows.isEmpty();

        if (hasTransactions) {
            for (Map<String, Object> row : rows) {
                if (!filter.test(row)) {
                    continue;
                }
                for (int i = 1; i <= 10; i += 1) {
                    eeSums[i - 1] += nz(row.get("ee" + i));
                    veeSums[i - 1] += nz(row.get("vee" + i));
                    erSums[i - 1] += nz(row.get("er" + i));
                }
            }
        }

        return new UnitSums(eeSums, veeSums, erSums, hasTransactions);
    }

    public FundNetBuildResult buildFundNetSummaryRows(UnitSums sums, UnitPrice unitPrice) {
        double[] prices = {
                unitPrice.getFund1(), unitPrice.getFund2(), unitPrice.getFund3(), unitPrice.getFund4(), unitPrice.getFund5(),
                unitPrice.getFund6(), unitPrice.getFund7(), unitPrice.getFund8(), unitPrice.getFund9(), unitPrice.getFund10()
        };

        List<FundNetSummary> rows = new ArrayList<>();
        double totalEEFunds = 0;
        double totalVEEFunds = 0;
        double totalERFunds = 0;

        for (int i = 1; i <= 10; i += 1) {
            Double eeUnits = sums.hasTransactions() ? sums.eeSums()[i - 1] : null;
            Double veeUnits = sums.hasTransactions() ? sums.veeSums()[i - 1] : null;
            Double erUnits = sums.hasTransactions() ? sums.erSums()[i - 1] : null;
            Double price = prices[i - 1];

            Double eeFunds = multiply(eeUnits, price);
            Double veeFunds = multiply(veeUnits, price);
            Double erFunds = multiply(erUnits, price);
            double totalUnits = nz(eeUnits) + nz(veeUnits) + nz(erUnits);
            double totalFunds = nz(eeFunds) + nz(veeFunds) + nz(erFunds);

            FundNetSummary row = new FundNetSummary();
            row.setFund(i);
            row.setEeUnits(eeUnits);
            row.setVeeUnits(veeUnits);
            row.setErUnits(erUnits);
            row.setTotalUnits(totalUnits);
            row.setUnitPrice(price);
            row.setEeFunds(eeFunds);
            row.setVeeFunds(veeFunds);
            row.setErFunds(erFunds);
            row.setTotalFunds(totalFunds);
            rows.add(row);

            totalEEFunds += nz(eeFunds);
            totalVEEFunds += nz(veeFunds);
            totalERFunds += nz(erFunds);
        }

        return new FundNetBuildResult(rows, totalEEFunds, totalVEEFunds, totalERFunds);
    }

    public FundUnitsBuildResult buildFundUnitRows(UnitSums sums) {
        List<FundUnitRow> rows = new ArrayList<>();
        double totalEEUnits = 0;
        double totalVEEUnits = 0;
        double totalERUnits = 0;

        for (int i = 1; i <= 10; i += 1) {
            Double eeUnits = sums.hasTransactions() ? sums.eeSums()[i - 1] : null;
            Double veeUnits = sums.hasTransactions() ? sums.veeSums()[i - 1] : null;
            Double erUnits = sums.hasTransactions() ? sums.erSums()[i - 1] : null;
            double totalUnits = nz(eeUnits) + nz(veeUnits) + nz(erUnits);

            FundUnitRow row = new FundUnitRow();
            row.setFund(i);
            row.setEeUnits(eeUnits);
            row.setVeeUnits(veeUnits);
            row.setErUnits(erUnits);
            row.setTotalUnits(totalUnits);
            rows.add(row);

            totalEEUnits += nz(eeUnits);
            totalVEEUnits += nz(veeUnits);
            totalERUnits += nz(erUnits);
        }

        return new FundUnitsBuildResult(rows, totalEEUnits, totalVEEUnits, totalERUnits);
    }

    public static String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    public double[] pricesOf(UnitPrice unitPrice) {
        return new double[]{
                nz(unitPrice.getFund1()), nz(unitPrice.getFund2()), nz(unitPrice.getFund3()), nz(unitPrice.getFund4()), nz(unitPrice.getFund5()),
                nz(unitPrice.getFund6()), nz(unitPrice.getFund7()), nz(unitPrice.getFund8()), nz(unitPrice.getFund9()), nz(unitPrice.getFund10())
        };
    }

    public double numberOrZero(Object value) {
        return nz(value);
    }

    private Double multiply(Double units, Double price) {
        if (units == null || price == null) {
            return null;
        }
        return units * price;
    }

    private double nz(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }

    private double nz(Double value) {
        return value == null ? 0 : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record UnitSums(double[] eeSums, double[] veeSums, double[] erSums, boolean hasTransactions) {}

    public record FundNetBuildResult(
            List<FundNetSummary> rows,
            double totalEEFunds,
            double totalVEEFunds,
            double totalERFunds
    ) {
        public double totalFunds() {
            return totalEEFunds + totalVEEFunds + totalERFunds;
        }
    }

    public record FundUnitsBuildResult(
            List<FundUnitRow> rows,
            double totalEEUnits,
            double totalVEEUnits,
            double totalERUnits
    ) {
        public double totalUnits() {
            return totalEEUnits + totalVEEUnits + totalERUnits;
        }
    }
}
