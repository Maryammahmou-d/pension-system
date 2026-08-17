package com.rubix.pension.financial_operations.service;

import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalEstimateDto;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalRequest;
import com.rubix.pension.financial_operations.dto.EmployeeWithdrawalResultDto;
import com.rubix.pension.financial_operations.dto.WithdrawalAmountDto;
import com.rubix.pension.financial_operations.dto.WithdrawalRowDto;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.UnitSums;
import com.rubix.pension.financial_operations.support.VestingSupport;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EmployeeWithdrawalService {

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
            FROM "Transactions"
            WHERE "Employee_Number" = :employeeNumber
              AND "Description" = 'Withdrawal'
              AND "Payment_Date" >= CAST(:fromDate AS date)
            """;

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

    private final FundHoldingsSupport fundHoldingsSupport;
    private final VestingSupport vestingSupport;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeWithdrawalService(
            FundHoldingsSupport fundHoldingsSupport,
            VestingSupport vestingSupport,
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.vestingSupport = vestingSupport;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public EmployeeWithdrawalEstimateDto estimate(String companyNumber, String employeeNumber, String withdrawalDate) {
        fundHoldingsSupport.requireNonBlank(companyNumber, employeeNumber, withdrawalDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(withdrawalDate);
        String trimmedCompany = companyNumber.trim();
        String trimmedEmployee = employeeNumber.trim();

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(targetDate);
        Company company = companyRepository.findLatestByCompanyNumber(trimmedCompany)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE));
        Employee employee = employeeRepository.findLatestByCompanyAndEmployeeNumber(trimmedCompany, trimmedEmployee)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE));

        List<Map<String, Object>> holdings = fundHoldingsSupport.fetchLatestHoldings(targetDate);
        UnitSums sums = fundHoldingsSupport.aggregateUnits(
                holdings,
                row -> Objects.equals(FundHoldingsSupport.asString(row.get("company_number")), trimmedCompany)
                        && Objects.equals(FundHoldingsSupport.asString(row.get("employee_number")), trimmedEmployee)
        );

        double vestingPct = vestingSupport.vestingPercent(trimmedCompany, employee, targetDate);
        double chargeEePct = nz(company.getWithdrawalChargesEe()) / 100.0;
        double chargeVeePct = nz(company.getWithdrawalChargesVee()) / 100.0;
        double chargeErPct = nz(company.getWithdrawalChargesEr()) / 100.0;
        double maxPct = nz(company.getMaxWithdrawalPercentage());
        if (maxPct > 1) {
            maxPct = maxPct / 100.0;
        }
        if (maxPct <= 0) {
            maxPct = 1;
        }
        double[] prices = fundHoldingsSupport.pricesOf(unitPrice);

        List<WithdrawalRowDto> rows = new ArrayList<>();
        double totalEe = 0;
        double totalVee = 0;
        double totalEr = 0;
        double availableEe = 0;
        double availableVee = 0;
        double availableEr = 0;

        for (int i = 1; i <= 10; i += 1) {
            double eeUnits = sums.hasTransactions() ? sums.eeSums()[i - 1] : 0;
            double veeUnits = sums.hasTransactions() ? sums.veeSums()[i - 1] : 0;
            double erUnits = sums.hasTransactions() ? sums.erSums()[i - 1] : 0;
            double price = prices[i - 1];
            double eeTotal = eeUnits * price;
            double veeTotal = veeUnits * price;
            double erTotal = erUnits * price;
            double availEe = eeTotal * (1 - chargeEePct) * maxPct;
            double availVee = veeTotal * (1 - chargeVeePct) * maxPct;
            double availEr = erTotal * (1 - chargeErPct) * maxPct;

            WithdrawalRowDto row = new WithdrawalRowDto();
            row.setFund(i);
            row.setEmployeeFundUnits(eeUnits);
            row.setVoluntaryEmployeeFundUnits(veeUnits);
            row.setEmployerFundUnits(erUnits);
            row.setUnitPrice(price);
            row.setEmployeeFundTotal(eeTotal);
            row.setVoluntaryEmployeeFundTotal(veeTotal);
            row.setEmployerFundTotal(erTotal);
            row.setAvailableEmployeeFund(availEe);
            row.setAvailableVoluntaryEmployeeFund(availVee);
            row.setAvailableEmployerFund(availEr);
            rows.add(row);

            totalEe += eeTotal;
            totalVee += veeTotal;
            totalEr += erTotal;
            availableEe += availEe;
            availableVee += availVee;
            availableEr += availEr;
        }

        int priorCount = countWithdrawals(trimmedEmployee, targetDate.minusDays(365));

        EmployeeWithdrawalEstimateDto result = new EmployeeWithdrawalEstimateDto();
        result.setCompanyNumber(trimmedCompany);
        result.setCompanyName(company.getCompanyName() == null ? "" : company.getCompanyName());
        result.setEmployeeNumber(trimmedEmployee);
        result.setEmployeeName(employee.getFullName() == null ? "" : employee.getFullName());
        result.setEmployeeId(employee.getEmployeeId());
        result.setCurrency(employee.getSalaryCurrency() == null ? "" : employee.getSalaryCurrency());
        result.setWithdrawalDate(targetDate.toString());
        result.setPensionStartDate(isoDate(employee.getPensionStartDate()));
        result.setTerminationDate(isoDate(employee.getTerminationDate()));
        result.setNationalId(employee.getNationalId() == null ? "" : employee.getNationalId());
        result.setCategory(employee.getCategory() == null ? "" : employee.getCategory());
        result.setCompanyAddress(blankTo(company.getAddress(), "Address"));
        result.setCompanyPhone(blankTo(company.getMobileNumber(), "Phone Number"));
        result.setRows(rows);
        result.getCharges().setEe(nz(company.getWithdrawalChargesEe()));
        result.getCharges().setVoluntaryEE(nz(company.getWithdrawalChargesVee()));
        result.getCharges().setEr(nz(company.getWithdrawalChargesEr()));
        result.setMaximumWithdrawalPercentage(maxPct);
        result.setMaximumWithdrawalCount(nz(company.getMaxWithdrawalCount()));
        result.setWithdrawalCountInPast365Days(priorCount);
        result.setVestingRulePercentage(vestingPct);
        result.setTotalEmployeeFund(totalEe);
        result.setTotalVoluntaryEmployeeFund(totalVee);
        result.setTotalEmployerFund(totalEr);
        result.setAvailableEmployeeFund(availableEe);
        result.setAvailableVoluntaryEmployeeFund(availableVee);
        result.setAvailableEmployerFund(availableEr);
        return result;
    }

    @Transactional
    public EmployeeWithdrawalResultDto withdraw(EmployeeWithdrawalRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        EmployeeWithdrawalEstimateDto estimate = estimate(
                request.getCompanyNumber(),
                request.getEmployeeNumber(),
                request.getWithdrawalDate()
        );
        if (estimate.getMaximumWithdrawalCount() > 0
                && estimate.getWithdrawalCountInPast365Days() >= estimate.getMaximumWithdrawalCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum withdrawal count has been reached.");
        }

        double[] takeEe = new double[10];
        double[] takeVee = new double[10];
        double[] takeEr = new double[10];
        double totalTake = 0;
        List<WithdrawalAmountDto> amounts = request.getAmounts() == null ? List.of() : request.getAmounts();
        for (WithdrawalAmountDto amount : amounts) {
            if (amount == null || amount.getFund() < 1 || amount.getFund() > 10) {
                continue;
            }
            int idx = amount.getFund() - 1;
            takeEe[idx] = Math.max(0, amount.getEmployeeFund());
            takeVee[idx] = Math.max(0, amount.getVoluntaryEmployeeFund());
            takeEr[idx] = Math.max(0, amount.getEmployerFund());
        }
        for (int i = 0; i < 10; i += 1) {
            WithdrawalRowDto row = estimate.getRows().get(i);
            if (takeEe[i] > row.getAvailableEmployeeFund() + 0.01
                    || takeVee[i] > row.getAvailableVoluntaryEmployeeFund() + 0.01
                    || takeEr[i] > row.getAvailableEmployerFund() + 0.01) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdrawal cannot exceed available funds.");
            }
            totalTake += takeEe[i] + takeVee[i] + takeEr[i];
        }
        if (totalTake <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a withdrawal amount.");
        }

        LocalDate targetDate = LocalDate.parse(estimate.getWithdrawalDate());
        Map<String, Object> snapshot = latestSnapshot(estimate.getEmployeeNumber());
        int nextSerial = ((Number) snapshot.getOrDefault("serial", 0)).intValue() + 1;
        insertWithdrawal(estimate, snapshot, nextSerial, targetDate, takeEe, takeVee, takeEr);

        return new EmployeeWithdrawalResultDto(
                "WD-" + estimate.getEmployeeNumber() + "-" + nextSerial,
                "Withdrawal recorded for " + estimate.getEmployeeNumber() + " on " + estimate.getWithdrawalDate() + "."
        );
    }

    private int countWithdrawals(String employeeNumber, LocalDate fromDate) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_SQL,
                new MapSqlParameterSource()
                        .addValue("employeeNumber", employeeNumber)
                        .addValue("fromDate", fromDate),
                Integer.class
        );
        return count == null ? 0 : count;
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

    private void insertWithdrawal(
            EmployeeWithdrawalEstimateDto estimate,
            Map<String, Object> snapshot,
            int serial,
            LocalDate paymentDate,
            double[] takeEe,
            double[] takeVee,
            double[] takeEr
    ) {
        StringBuilder columns = new StringBuilder(
                "\"Serial\", \"Description\", \"Modified_Date\", \"Payment_Date\", \"Company_Number\", \"Employee_ID\", \"Employee_Number\", \"Currency\""
        );
        StringBuilder values = new StringBuilder(
                ":serial, 'Withdrawal', CAST(:asOf AS date), CAST(:asOf AS date), :companyNumber, :employeeId, :employeeNumber, :currency"
        );
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("serial", serial)
                .addValue("asOf", paymentDate)
                .addValue("companyNumber", estimate.getCompanyNumber())
                .addValue("employeeId", snapshot.get("employee_id"))
                .addValue("employeeNumber", estimate.getEmployeeNumber())
                .addValue("currency", snapshot.get("currency"));

        double withdrawalEe = 0;
        double withdrawalVee = 0;
        double withdrawalEr = 0;

        for (int i = 1; i <= 10; i += 1) {
            WithdrawalRowDto row = estimate.getRows().get(i - 1);
            double price = row.getUnitPrice();
            double startEe = fundHoldingsSupport.numberOrZero(snapshot.get("ee" + i));
            double startVee = fundHoldingsSupport.numberOrZero(snapshot.get("vee" + i));
            double startEr = fundHoldingsSupport.numberOrZero(snapshot.get("er" + i));
            double txEeVal = takeEe[i - 1];
            double txVeeVal = takeVee[i - 1];
            double txErVal = takeEr[i - 1];
            double txEeUnits = price == 0 ? 0 : -txEeVal / price;
            double txVeeUnits = price == 0 ? 0 : -txVeeVal / price;
            double txErUnits = price == 0 ? 0 : -txErVal / price;
            double newEe = startEe + txEeUnits;
            double newVee = startVee + txVeeUnits;
            double newEr = startEr + txErUnits;

            addCol(columns, values, "UP" + i, "up" + i);
            params.addValue("up" + i, price);
            addCol(columns, values, "Starting_EE_Units_F" + i, "startEe" + i);
            params.addValue("startEe" + i, startEe);
            addCol(columns, values, "Starting_VEE_Units_F" + i, "startVee" + i);
            params.addValue("startVee" + i, startVee);
            addCol(columns, values, "Starting_ER_Units_F" + i, "startEr" + i);
            params.addValue("startEr" + i, startEr);
            addCol(columns, values, "Starting_Total_Units_F" + i, "startTot" + i);
            params.addValue("startTot" + i, startEe + startVee + startEr);
            addCol(columns, values, "Transactional_EE_Units_F" + i, "txEe" + i);
            params.addValue("txEe" + i, txEeUnits);
            addCol(columns, values, "Transactional_VEE_Units_F" + i, "txVee" + i);
            params.addValue("txVee" + i, txVeeUnits);
            addCol(columns, values, "Transactional_ER_Units_F" + i, "txEr" + i);
            params.addValue("txEr" + i, txErUnits);
            addCol(columns, values, "Transactional_Total_Units_F" + i, "txTot" + i);
            params.addValue("txTot" + i, txEeUnits + txVeeUnits + txErUnits);
            addCol(columns, values, "Total_EE_Units_F" + i, "totEe" + i);
            params.addValue("totEe" + i, newEe);
            addCol(columns, values, "Total_VEE_Units_F" + i, "totVee" + i);
            params.addValue("totVee" + i, newVee);
            addCol(columns, values, "Total_ER_Units_F" + i, "totEr" + i);
            params.addValue("totEr" + i, newEr);
            addCol(columns, values, "Total_Units_F" + i, "tot" + i);
            params.addValue("tot" + i, newEe + newVee + newEr);
            addCol(columns, values, "Transactional_EE_Value_F" + i, "txEeVal" + i);
            params.addValue("txEeVal" + i, -txEeVal);
            addCol(columns, values, "Transactional_VEE_Value_F" + i, "txVeeVal" + i);
            params.addValue("txVeeVal" + i, -txVeeVal);
            addCol(columns, values, "Transactional_ER_Value_F" + i, "txErVal" + i);
            params.addValue("txErVal" + i, -txErVal);

            withdrawalEe += txEeVal;
            withdrawalVee += txVeeVal;
            withdrawalEr += txErVal;
        }

        addCol(columns, values, "Withdrawal_EE", "wEe");
        params.addValue("wEe", withdrawalEe);
        addCol(columns, values, "Withdrawal_VEE", "wVee");
        params.addValue("wVee", withdrawalVee);
        addCol(columns, values, "Withdrawal_ER", "wEr");
        params.addValue("wEr", withdrawalEr);
        addCol(columns, values, "Withdrawal_Total", "wTot");
        params.addValue("wTot", withdrawalEe + withdrawalVee + withdrawalEr);
        addCol(columns, values, "Withdrawal_Charges_EE", "cEe");
        params.addValue("cEe", estimate.getCharges().getEe());
        addCol(columns, values, "Withdrawal_Charges_VEE", "cVee");
        params.addValue("cVee", estimate.getCharges().getVoluntaryEE());
        addCol(columns, values, "Withdrawal_Charges_ER", "cEr");
        params.addValue("cEr", estimate.getCharges().getEr());
        addCol(columns, values, "Transactional_EE_Value", "txEeVal");
        params.addValue("txEeVal", -withdrawalEe);
        addCol(columns, values, "Transactional_VEE_Value", "txVeeVal");
        params.addValue("txVeeVal", -withdrawalVee);
        addCol(columns, values, "Transactional_ER_Value", "txErVal");
        params.addValue("txErVal", -withdrawalEr);
        addCol(columns, values, "Transactional_Total_Value", "txTotVal");
        params.addValue("txTotVal", -(withdrawalEe + withdrawalVee + withdrawalEr));

        String sql = "INSERT INTO \"Transactions\" (" + columns + ") VALUES (" + values + ")";
        jdbcTemplate.update(sql, params);
    }

    private void addCol(StringBuilder columns, StringBuilder values, String column, String param) {
        columns.append(", \"").append(column).append('"');
        values.append(", :").append(param);
    }

    private double nz(Double value) {
        return value == null ? 0 : value;
    }

    private String isoDate(java.time.OffsetDateTime value) {
        return value == null ? null : value.toLocalDate().toString();
    }

    private String blankTo(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
