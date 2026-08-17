package com.rubix.pension.financial_operations.service;

import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.financial_operations.dto.MonthlyChargeRunDto;
import com.rubix.pension.financial_operations.dto.MonthlyChargesResultDto;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonthlyChargesService {

    private static final String PREVIOUS_SQL = """
            SELECT CAST("Payment_Date" AS date) AS payment_date, COUNT(*) AS processed_count
            FROM "Transactions"
            WHERE "Description" = 'Monthly Charges'
            GROUP BY CAST("Payment_Date" AS date)
            ORDER BY CAST("Payment_Date" AS date) DESC
            """;

    private static final String DUPLICATE_SQL = """
            SELECT COUNT(*)
            FROM "Transactions"
            WHERE "Description" = 'Monthly Charges'
              AND CAST("Payment_Date" AS date) = CAST(:asOf AS date)
            """;

    private static final String EMPLOYEE_STATUS_SQL = """
            SELECT DISTINCT ON ("Employee_Number")
                "Employee_Number" AS employee_number,
                "Termination_Date" AS termination_date,
                "Employee_ID" AS employee_id
            FROM "Employees"
            ORDER BY "Employee_Number", "Serial" DESC NULLS LAST, "ID" DESC
            """;

    private static final String SNAPSHOT_SQL = """
            SELECT
                t."Serial" AS serial,
                t."Employee_ID" AS employee_id,
                t."Employee_Number" AS employee_number,
                t."Company_Number" AS company_number,
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
            INNER JOIN (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Transactions"
                WHERE "Modified_Date" <= CAST(:asOf AS date)
                GROUP BY "Employee_Number"
            ) latest
              ON t."Employee_Number" = latest."Employee_Number"
             AND t."Serial" = latest.max_serial
            """;

    private final FundHoldingsSupport fundHoldingsSupport;
    private final CompanyRepository companyRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MonthlyChargesService(
            FundHoldingsSupport fundHoldingsSupport,
            CompanyRepository companyRepository,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.companyRepository = companyRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MonthlyChargeRunDto> listPrevious() {
        List<MonthlyChargeRunDto> runs = new ArrayList<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(PREVIOUS_SQL, Map.of())) {
            LocalDate date = toLocalDate(row.get("payment_date"));
            MonthlyChargeRunDto run = new MonthlyChargeRunDto();
            run.setPaymentDate(date.toString());
            run.setRunDate(date.toString());
            run.setMonth(date.getMonthValue());
            run.setYear(date.getYear());
            run.setProcessedCount(((Number) row.get("processed_count")).intValue());
            runs.add(run);
        }
        return runs;
    }

    @Transactional
    public MonthlyChargesResultDto run(String runDate, Integer month, Integer year) {
        fundHoldingsSupport.requireNonBlank(runDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(runDate);
        if (month == null || month < 1 || month > 12 || year == null || year < 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }

        Integer existing = jdbcTemplate.queryForObject(
                DUPLICATE_SQL,
                new MapSqlParameterSource("asOf", targetDate),
                Integer.class
        );
        if (existing != null && existing > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monthly charges have already been run for that date.");
        }

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(targetDate);
        double[] prices = fundHoldingsSupport.pricesOf(unitPrice);

        Map<String, Company> companies = new HashMap<>();
        for (Company company : companyRepository.findLatestCompanies()) {
            companies.put(company.getCompanyNumber(), company);
        }

        Map<String, Map<String, Object>> employees = new HashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(EMPLOYEE_STATUS_SQL, Map.of())) {
            employees.put(FundHoldingsSupport.asString(row.get("employee_number")), row);
        }

        int processed = 0;
        List<Map<String, Object>> snapshots = jdbcTemplate.queryForList(
                SNAPSHOT_SQL,
                new MapSqlParameterSource("asOf", targetDate)
        );
        for (Map<String, Object> snapshot : snapshots) {
            String employeeNumber = FundHoldingsSupport.asString(snapshot.get("employee_number"));
            String companyNumber = FundHoldingsSupport.asString(snapshot.get("company_number"));
            Map<String, Object> employee = employees.get(employeeNumber);
            if (employee != null && employee.get("termination_date") != null) {
                continue;
            }
            Company company = companies.get(companyNumber);
            if (company == null) {
                continue;
            }
            insertCharge(snapshot, company, prices, targetDate, employee);
            processed += 1;
        }

        MonthlyChargeRunDto run = new MonthlyChargeRunDto();
        run.setPaymentDate(targetDate.toString());
        run.setRunDate(targetDate.toString());
        run.setMonth(month);
        run.setYear(year);
        run.setProcessedCount(processed);

        MonthlyChargesResultDto result = new MonthlyChargesResultDto();
        result.setRun(run);
        result.setPreviousRuns(listPrevious());
        return result;
    }

    private void insertCharge(
            Map<String, Object> snapshot,
            Company company,
            double[] prices,
            LocalDate asOf,
            Map<String, Object> employee
    ) {
        double imcRate = company.getImc() == null ? 0 : company.getImc();
        double monthlyFactor = Math.pow(1 + imcRate / 100.0, 1.0 / 12.0) - 1;
        if (monthlyFactor < 0) {
            monthlyFactor = 0;
        }
        double adminMonthly = company.getAdminCharges() == null ? 0 : company.getAdminCharges() / 12.0;

        double totalValue = 0;
        double[] ee = new double[10];
        double[] vee = new double[10];
        double[] er = new double[10];
        double[] fundValue = new double[10];
        for (int i = 1; i <= 10; i += 1) {
            ee[i - 1] = fundHoldingsSupport.numberOrZero(snapshot.get("ee" + i));
            vee[i - 1] = fundHoldingsSupport.numberOrZero(snapshot.get("vee" + i));
            er[i - 1] = fundHoldingsSupport.numberOrZero(snapshot.get("er" + i));
            fundValue[i - 1] = (ee[i - 1] + vee[i - 1] + er[i - 1]) * prices[i - 1];
            totalValue += fundValue[i - 1];
        }

        int serial = ((Number) snapshot.getOrDefault("serial", 0)).intValue() + 1;
        StringBuilder columns = new StringBuilder(
                "\"Serial\", \"Description\", \"Modified_Date\", \"Payment_Date\", \"Company_Number\", \"Employee_ID\", \"Employee_Number\", \"Currency\""
        );
        StringBuilder values = new StringBuilder(
                ":serial, 'Monthly Charges', CAST(:asOf AS date), CAST(:asOf AS date), :companyNumber, :employeeId, :employeeNumber, :currency"
        );
        Object employeeId = snapshot.get("employee_id");
        if (employeeId == null && employee != null) {
            employeeId = employee.get("employee_id");
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("serial", serial)
                .addValue("asOf", asOf)
                .addValue("companyNumber", snapshot.get("company_number"))
                .addValue("employeeId", employeeId)
                .addValue("employeeNumber", snapshot.get("employee_number"))
                .addValue("currency", snapshot.get("currency"));

        double imcEe = 0;
        double imcVee = 0;
        double imcEr = 0;
        double adminEe = 0;
        double adminEr = 0;

        for (int i = 1; i <= 10; i += 1) {
            double price = prices[i - 1];
            double imcEeVal = ee[i - 1] * price * monthlyFactor;
            double imcVeeVal = vee[i - 1] * price * monthlyFactor;
            double imcErVal = er[i - 1] * price * monthlyFactor;
            double share = totalValue == 0 ? 0 : fundValue[i - 1] / totalValue;
            double adminEeVal = adminMonthly * share / 2.0;
            double adminErVal = adminMonthly * share / 2.0;
            double txEeUnits = price == 0 ? 0 : -(imcEeVal + adminEeVal) / price;
            double txVeeUnits = price == 0 ? 0 : -imcVeeVal / price;
            double txErUnits = price == 0 ? 0 : -(imcErVal + adminErVal) / price;
            double newEe = ee[i - 1] + txEeUnits;
            double newVee = vee[i - 1] + txVeeUnits;
            double newEr = er[i - 1] + txErUnits;

            addCol(columns, values, "UP" + i, "up" + i);
            params.addValue("up" + i, price);
            addCol(columns, values, "Starting_EE_Units_F" + i, "startEe" + i);
            params.addValue("startEe" + i, ee[i - 1]);
            addCol(columns, values, "Starting_VEE_Units_F" + i, "startVee" + i);
            params.addValue("startVee" + i, vee[i - 1]);
            addCol(columns, values, "Starting_ER_Units_F" + i, "startEr" + i);
            params.addValue("startEr" + i, er[i - 1]);
            addCol(columns, values, "Starting_Total_Units_F" + i, "startTot" + i);
            params.addValue("startTot" + i, ee[i - 1] + vee[i - 1] + er[i - 1]);
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

            imcEe += imcEeVal;
            imcVee += imcVeeVal;
            imcEr += imcErVal;
            adminEe += adminEeVal;
            adminEr += adminErVal;
        }

        addCol(columns, values, "IMC_EE", "imcEe");
        params.addValue("imcEe", imcEe);
        addCol(columns, values, "IMC_VEE", "imcVee");
        params.addValue("imcVee", imcVee);
        addCol(columns, values, "IMC_ER", "imcEr");
        params.addValue("imcEr", imcEr);
        addCol(columns, values, "IMC_Total", "imcTot");
        params.addValue("imcTot", imcEe + imcVee + imcEr);
        addCol(columns, values, "Admin_Charges_EE", "adminEe");
        params.addValue("adminEe", adminEe);
        addCol(columns, values, "Admin_Charges_ER", "adminEr");
        params.addValue("adminEr", adminEr);
        addCol(columns, values, "Admin_Charges_Total", "adminTot");
        params.addValue("adminTot", (int) Math.round(adminEe + adminEr));

        String sql = "INSERT INTO \"Transactions\" (" + columns + ") VALUES (" + values + ")";
        jdbcTemplate.update(sql, params);
    }

    private void addCol(StringBuilder columns, StringBuilder values, String column, String param) {
        columns.append(", \"").append(column).append('"');
        values.append(", :").append(param);
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        return LocalDate.parse(value.toString().substring(0, 10));
    }
}
