package com.rubix.pension.invoicing_top_up_operations.service;

import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TopUpService {

    private static final ZoneId ACCESS_ZONE = ZoneId.of("Asia/Kuwait");

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

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final FundHoldingsSupport fundHoldingsSupport;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TopUpService(
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            FundHoldingsSupport fundHoldingsSupport,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.fundHoldingsSupport = fundHoldingsSupport;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public TopUpResult create(TopUpRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        fundHoldingsSupport.requireNonBlank(request.companyNumber(), request.employeeNumber(), request.topUpDate());
        double total = nz(request.topUpEE()) + nz(request.topUpVEE()) + nz(request.topUpER());
        if (total <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total Top Up should be > 0");
        }
        Employee employee = employeeRepository.findLatestByCompanyAndEmployeeNumber(
                        request.companyNumber().trim(), request.employeeNumber().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found for this company."));
        return postForEmployee(employee, request.topUpDate(), request.topUpEE(), request.topUpVEE(),
                request.topUpER(), request.userName());
    }

    @Transactional
    public BulkTopUpResult bulk(BulkTopUpRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        List<BulkTopUpRowResult> rows = new ArrayList<>();
        List<TopUpResult> posted = new ArrayList<>();
        int succeeded = 0;
        List<BulkTopUpRow> inputRows = request.rows() == null ? List.of() : request.rows();
        for (BulkTopUpRow row : inputRows) {
            try {
                if (isBlank(row.employeeNumber())) {
                    continue;
                }
                String employeeNumber = row.employeeNumber().trim();
                if (employeeNumber.toUpperCase().startsWith("SAMPLE")) {
                    rows.add(new BulkTopUpRowResult(
                            employeeNumber, false, "Sample row skipped. Replace with a real employee number.", null, null));
                    continue;
                }
                Employee employee = resolveBulkEmployee(request.companyNumber(), employeeNumber);
                if (employee == null) {
                    rows.add(new BulkTopUpRowResult(
                            employeeNumber, false, "Employee not found. Use the Employee_Number from Add Top Up.", null, null));
                    continue;
                }
                double total = nz(row.ee()) + nz(row.vee()) + nz(row.er());
                if (total <= 0) {
                    rows.add(new BulkTopUpRowResult(employeeNumber, false, "Total Top Up should be > 0", null, null));
                    continue;
                }
                TopUpResult result = postForEmployee(
                        employee, row.unitPriceDate(), row.ee(), row.vee(), row.er(), request.userName());
                posted.add(result);
                rows.add(new BulkTopUpRowResult(employeeNumber, true, "Done", total, result));
                succeeded += 1;
            } catch (ResponseStatusException ex) {
                rows.add(new BulkTopUpRowResult(row.employeeNumber(), false, ex.getReason(), null, null));
            } catch (Exception ex) {
                rows.add(new BulkTopUpRowResult(row.employeeNumber(), false, ex.getMessage(), null, null));
            }
        }
        return new BulkTopUpResult(rows.size(), succeeded, rows.size() - succeeded, rows, posted);
    }

    private Employee resolveBulkEmployee(String companyNumber, String employeeNumber) {
        if (!isBlank(companyNumber)) {
            Employee inCompany = employeeRepository
                    .findLatestByCompanyAndEmployeeNumber(companyNumber.trim(), employeeNumber)
                    .orElse(null);
            if (inCompany != null) {
                return inCompany;
            }
            Integer employeeId = parseEmployeeId(employeeNumber);
            if (employeeId != null) {
                Employee byId = employeeRepository
                        .findLatestByCompanyAndEmployeeId(companyNumber.trim(), employeeId)
                        .orElse(null);
                if (byId != null) {
                    return byId;
                }
            }
        }
        return employeeRepository.findLatestByEmployeeNumber(employeeNumber).orElse(null);
    }

    private Integer parseEmployeeId(String employeeNumber) {
        // Employee_ID is a small serial, not National ID / long numbers typed in Excel.
        if (employeeNumber == null || !employeeNumber.matches("\\d{1,6}")) {
            return null;
        }
        try {
            return Integer.valueOf(employeeNumber);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private TopUpResult postForEmployee(
            Employee employee,
            String topUpDate,
            Double topUpEE,
            Double topUpVEE,
            Double topUpER,
            String userName
    ) {
        fundHoldingsSupport.requireNonBlank(employee.getEmployeeNumber(), topUpDate);
        LocalDate paymentDate = fundHoldingsSupport.parseRequiredDate(topUpDate);
        UnitPrice unitPrice;
        try {
            unitPrice = fundHoldingsSupport.requireUnitPrice(paymentDate);
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    " There is no unit price for that date. Please add Top Up after the unit price is entered."
            );
        }
        Company company = companyRepository.findLatestByCompanyNumber(employee.getCompanyNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found."));
        Map<String, Object> snapshot = latestSnapshot(employee.getEmployeeNumber().trim());
        int nextSerial = ((Number) snapshot.getOrDefault("serial", 0)).intValue() + 1;
        return insertTopUp(company, employee, unitPrice, snapshot, nextSerial, paymentDate,
                nz(topUpEE), nz(topUpVEE), nz(topUpER), blankTo(userName, employee.getUsername()));
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

    private TopUpResult insertTopUp(
            Company company,
            Employee employee,
            UnitPrice unitPrice,
            Map<String, Object> snapshot,
            int serial,
            LocalDate paymentDate,
            double topUpEE,
            double topUpVEE,
            double topUpER,
            String userName
    ) {
        OffsetDateTime modifiedDate = accessDate(LocalDate.now(ACCESS_ZONE));
        OffsetDateTime priceDate = accessDate(paymentDate);
        StringBuilder columns = new StringBuilder(
                "\"Serial\", \"Description\", \"Modified_Date\", \"Payment_Date\", \"Retro_Date\", \"Company_Number\", \"Employee_ID\", \"Employee_Number\", \"Currency\", \"UserName\""
        );
        StringBuilder values = new StringBuilder(
                ":serial, 'Top Up', :modifiedDate, :paymentDate, :retroDate, :companyNumber, :employeeId, :employeeNumber, :currency, :userName"
        );
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("serial", serial)
                .addValue("modifiedDate", modifiedDate)
                .addValue("paymentDate", priceDate)
                .addValue("retroDate", priceDate)
                .addValue("companyNumber", company.getCompanyNumber())
                .addValue("employeeId", employee.getEmployeeId())
                .addValue("employeeNumber", employee.getEmployeeNumber())
                .addValue("currency", blankTo(employee.getSalaryCurrency(), snapshot.get("currency") == null ? "EGP" : snapshot.get("currency").toString()))
                .addValue("userName", blankTo(userName, "system"));

        double[] prices = fundHoldingsSupport.pricesOf(unitPrice);
        double chargePct = company.getTopUpCharges() == null ? 0 : company.getTopUpCharges() / 100.0;
        double chargeEe = topUpEE * chargePct;
        double chargeVee = topUpVEE * chargePct;
        double chargeEr = topUpER * chargePct;
        double imcEe = imcAmount(topUpEE, company.getImc(), paymentDate);
        double imcVee = imcAmount(topUpVEE, company.getImc(), paymentDate);
        double imcEr = imcAmount(topUpER, company.getImc(), paymentDate);
        double netEe = topUpEE - chargeEe - imcEe;
        double netVee = topUpVEE - chargeVee - imcVee;
        double netEr = topUpER - chargeEr - imcEr;

        double txEeTotal = 0;
        double txVeeTotal = 0;
        double txErTotal = 0;
        double totalEeValue = 0;
        double totalVeeValue = 0;
        double totalErValue = 0;
        List<FundAllocation> funds = new ArrayList<>();

        for (int i = 1; i <= 10; i += 1) {
            double price = prices[i - 1];
            double startEe = fundHoldingsSupport.numberOrZero(snapshot.get("ee" + i));
            double startVee = fundHoldingsSupport.numberOrZero(snapshot.get("vee" + i));
            double startEr = fundHoldingsSupport.numberOrZero(snapshot.get("er" + i));
            double txEeVal = netEe * eeWeight(employee, i) / 100.0;
            double txVeeVal = netVee * eeWeight(employee, i) / 100.0;
            double txErVal = netEr * erWeight(employee, i) / 100.0;
            double txEeUnits = price == 0 ? 0 : txEeVal / price;
            double txVeeUnits = price == 0 ? 0 : txVeeVal / price;
            double txErUnits = price == 0 ? 0 : txErVal / price;
            double newEe = startEe + txEeUnits;
            double newVee = startVee + txVeeUnits;
            double newEr = startEr + txErUnits;
            txEeTotal += txEeVal;
            txVeeTotal += txVeeVal;
            txErTotal += txErVal;
            totalEeValue += newEe * price;
            totalVeeValue += newVee * price;
            totalErValue += newEr * price;

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
            params.addValue("txEeVal" + i, txEeVal);
            addCol(columns, values, "Transactional_VEE_Value_F" + i, "txVeeVal" + i);
            params.addValue("txVeeVal" + i, txVeeVal);
            addCol(columns, values, "Transactional_ER_Value_F" + i, "txErVal" + i);
            params.addValue("txErVal" + i, txErVal);
            addCol(columns, values, "Transactional_Total_Value_F" + i, "txTotVal" + i);
            params.addValue("txTotVal" + i, txEeVal + txVeeVal + txErVal);
            funds.add(new FundAllocation(i, price, txEeVal, txVeeVal, txErVal, txEeUnits, txVeeUnits, txErUnits));
        }

        addCol(columns, values, "TopUp_EE", "topUpEe");
        params.addValue("topUpEe", topUpEE);
        addCol(columns, values, "TopUp_VEE", "topUpVee");
        params.addValue("topUpVee", topUpVEE);
        addCol(columns, values, "TopUp_ER", "topUpEr");
        params.addValue("topUpEr", topUpER);
        addCol(columns, values, "TopUp_Total", "topUpTot");
        params.addValue("topUpTot", topUpEE + topUpVEE + topUpER);
        addCol(columns, values, "Top_Up_Charges_EE", "chargeEe");
        params.addValue("chargeEe", chargeEe);
        addCol(columns, values, "Top_Up_Charges_VEE", "chargeVee");
        params.addValue("chargeVee", chargeVee);
        addCol(columns, values, "Top_Up_Charges_ER", "chargeEr");
        params.addValue("chargeEr", chargeEr);
        addCol(columns, values, "Top_Up_Charges_Total", "chargeTot");
        params.addValue("chargeTot", chargeEe + chargeVee + chargeEr);
        addCol(columns, values, "IMC_EE", "imcEe");
        params.addValue("imcEe", imcEe);
        addCol(columns, values, "IMC_VEE", "imcVee");
        params.addValue("imcVee", imcVee);
        addCol(columns, values, "IMC_ER", "imcEr");
        params.addValue("imcEr", imcEr);
        addCol(columns, values, "IMC_Total", "imcTot");
        params.addValue("imcTot", imcEe + imcVee + imcEr);
        addCol(columns, values, "Transactional_EE_Value", "txEeVal");
        params.addValue("txEeVal", txEeTotal);
        addCol(columns, values, "Transactional_VEE_Value", "txVeeVal");
        params.addValue("txVeeVal", txVeeTotal);
        addCol(columns, values, "Transactional_ER_Value", "txErVal");
        params.addValue("txErVal", txErTotal);
        addCol(columns, values, "Transactional_Total_Value", "txTotVal");
        params.addValue("txTotVal", txEeTotal + txVeeTotal + txErTotal);

        String sql = "INSERT INTO \"Transactions\" (" + columns + ") VALUES (" + values + ") RETURNING \"ID\"";
        Integer transactionId = jdbcTemplate.queryForObject(sql, params, Integer.class);
        if (transactionId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create top-up transaction.");
        }

        return new TopUpResult(
                employee.getEmployeeNumber(),
                blankTo(employee.getFullName(), employee.getEmployeeNumber()),
                company.getCompanyNumber(),
                paymentDate.toString(),
                accessDateText(modifiedDate),
                transactionId,
                serial,
                employee.getEmployeeId(),
                employee.getNationalId(),
                employee.getCategory(),
                accessDateText(employee.getPensionStartDate()),
                blankTo(employee.getSalaryCurrency(), "EGP"),
                topUpEE,
                topUpVEE,
                topUpER,
                topUpEE + topUpVEE + topUpER,
                chargeEe,
                chargeVee,
                chargeEr,
                imcEe,
                imcVee,
                imcEr,
                txEeTotal,
                txVeeTotal,
                txErTotal,
                txEeTotal + txVeeTotal + txErTotal,
                totalEeValue,
                totalVeeValue,
                totalErValue,
                funds
        );
    }

    private void addCol(StringBuilder columns, StringBuilder values, String column, String param) {
        columns.append(", \"").append(column).append('"');
        values.append(", :").append(param);
    }

    private double eeWeight(Employee employee, int fund) {
        return switch (fund) {
            case 1 -> nz(employee.getWeightF1Ee());
            case 2 -> nz(employee.getWeightF2Ee());
            case 3 -> nz(employee.getWeightF3Ee());
            case 4 -> nz(employee.getWeightF4Ee());
            case 5 -> nz(employee.getWeightF5Ee());
            case 6 -> nz(employee.getWeightF6Ee());
            case 7 -> nz(employee.getWeightF7Ee());
            case 8 -> nz(employee.getWeightF8Ee());
            case 9 -> nz(employee.getWeightF9Ee());
            case 10 -> nz(employee.getWeightF10Ee());
            default -> 0;
        };
    }

    private double erWeight(Employee employee, int fund) {
        return switch (fund) {
            case 1 -> nz(employee.getWeightF1Er());
            case 2 -> nz(employee.getWeightF2Er());
            case 3 -> nz(employee.getWeightF3Er());
            case 4 -> nz(employee.getWeightF4Er());
            case 5 -> nz(employee.getWeightF5Er());
            case 6 -> nz(employee.getWeightF6Er());
            case 7 -> nz(employee.getWeightF7Er());
            case 8 -> nz(employee.getWeightF8Er());
            case 9 -> nz(employee.getWeightF9Er());
            case 10 -> nz(employee.getWeightF10Er());
            default -> 0;
        };
    }

    private double imcAmount(double principal, Double imcPercent, LocalDate topUpDate) {
        double rate = imcPercent == null ? 0 : imcPercent / 100.0;
        LocalDate nextMonthStart = topUpDate.withDayOfMonth(1).plusMonths(1);
        double years = ChronoUnit.DAYS.between(topUpDate, nextMonthStart) / 365.25;
        double factor = Math.pow(1 + rate, years) - 1;
        if (factor < 0) {
            factor = 0;
        }
        return factor * principal;
    }

    private OffsetDateTime accessDate(LocalDate day) {
        return day.atStartOfDay(ACCESS_ZONE).toOffsetDateTime();
    }

    private String accessDateText(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.toInstant().atZone(ACCESS_ZONE).toLocalDate().toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankTo(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private double nz(Double value) {
        return value == null ? 0 : value;
    }

    public record TopUpRequest(
            String companyNumber,
            String employeeNumber,
            String topUpDate,
            Double topUpEE,
            Double topUpVEE,
            Double topUpER,
            String path,
            String userName
    ) {}

    public record TopUpResult(
            String employeeNumber,
            String employeeName,
            String companyNumber,
            String topUpDate,
            String modifiedDate,
            Integer transactionId,
            Integer serial,
            Integer employeeId,
            String nationalId,
            String category,
            String pensionStartDate,
            String currency,
            double topUpEE,
            double topUpVEE,
            double topUpER,
            double total,
            double chargesEe,
            double chargesVee,
            double chargesEr,
            double imcEe,
            double imcVee,
            double imcEr,
            double transactionalEe,
            double transactionalVee,
            double transactionalEr,
            double transactionalTotal,
            double totalEeValue,
            double totalVeeValue,
            double totalErValue,
            List<FundAllocation> funds
    ) {}

    public record FundAllocation(
            int fund,
            double unitPrice,
            double eeValue,
            double veeValue,
            double erValue,
            double eeUnits,
            double veeUnits,
            double erUnits
    ) {}

    public record BulkTopUpRequest(
            String companyNumber,
            List<BulkTopUpRow> rows,
            String path,
            String userName
    ) {}

    public record BulkTopUpRow(
            String employeeNumber,
            Double ee,
            Double er,
            Double vee,
            String unitPriceDate
    ) {}

    public record BulkTopUpRowResult(
            String employeeNumber,
            boolean ok,
            String message,
            Double total,
            TopUpResult report
    ) {}

    public record BulkTopUpResult(
            int processed,
            int succeeded,
            int failed,
            List<BulkTopUpRowResult> rows,
            List<TopUpResult> posted
    ) {}
}
