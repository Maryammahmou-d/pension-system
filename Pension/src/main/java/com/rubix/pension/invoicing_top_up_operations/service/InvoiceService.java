package com.rubix.pension.invoicing_top_up_operations.service;

import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class InvoiceService {

    /** Access Date() calendar day in the pension timezone. */
    private static final ZoneId ACCESS_ZONE = ZoneId.of("Asia/Kuwait");

    private static final String LATEST_EMPLOYEE_LINES = """
            SELECT
                ie."Invoice_Number" AS invoice_number,
                MAX(COALESCE(ie."Company_Name", '')) AS company_name,
                COUNT(*) AS employee_line_count
            FROM "InvoicesEmployee" ie
            INNER JOIN (
                SELECT "Invoice_Number", MAX("Serial") AS max_serial
                FROM "InvoicesEmployee"
                GROUP BY "Invoice_Number"
            ) latest
              ON latest."Invoice_Number" = ie."Invoice_Number"
             AND latest.max_serial = ie."Serial"
            GROUP BY ie."Invoice_Number"
            """;

    private static final String SELECT_BASE = """
            SELECT
                i."Invoice_Number" AS invoice_number,
                i."Company_Number" AS company_number,
                COALESCE(iea.company_name, '') AS company_name,
                i."Invoice_Date"::date AS invoice_date,
                i."Date_From"::date AS date_from,
                i."Date_To"::date AS date_to,
                COALESCE(i."Status", 'Pending') AS status,
                i."Payment_Date"::date AS payment_date,
                COALESCE(i."EGPAmount", 0) AS egp_amount,
                COALESCE(i."USDAmount", 0) AS usd_amount,
                COALESCE(i."EURAmount", 0) AS eur_amount,
                COALESCE(iea.employee_line_count, 0) AS employee_line_count
            FROM "LatestInvoiceRecords" i
            LEFT JOIN (
            """ + LATEST_EMPLOYEE_LINES + """
            ) iea
              ON iea.invoice_number = i."Invoice_Number"
            """;

    private static final String LIST_SQL = SELECT_BASE + """
            ORDER BY i."Invoice_Number"
            """;

    private static final String LIST_BY_STATUS_SQL = SELECT_BASE + """
            WHERE i."Status" = :status
            ORDER BY i."Invoice_Number"
            """;

    private static final String GET_SQL = SELECT_BASE + """
            WHERE i."Invoice_Number" = :invoiceNumber
            ORDER BY i."ID" DESC
            LIMIT 1
            """;

    private static final String DETAILS_LINES_SQL = """
            SELECT
                COALESCE(NULLIF(TRIM(ie."Category"::text), ''), '0') AS category,
                COUNT(*) AS employee_count,
                COALESCE(MAX(ie."Currency"), 'EGP') AS currency,
                COALESCE(SUM(ie."Employee_Contribution"), 0) AS employee_contribution,
                COALESCE(SUM(ie."VEE_Contribution"), 0) AS vee_contribution,
                COALESCE(SUM(ie."Employer_Contribution"), 0) AS employer_contribution,
                COALESCE(SUM(ie."Total_Contribution"), 0) AS total_contribution,
                COALESCE(SUM(ie."Proportional_Stamp_Duty"), 0) AS stamp_duty,
                COALESCE(SUM(ie."Supervisory_Fees"), 0) AS supervisory_fees,
                COALESCE(SUM(ie."FRA_Approval_Fees"), 0) AS fra_approval_fees,
                COALESCE(SUM(ie."FRA_Provision_Fees"), 0) AS fra_provision_fees,
                COALESCE(SUM(ie."Grand_Total"), 0) AS grand_total
            FROM "InvoicesEmployee" ie
            INNER JOIN (
                SELECT MAX("Serial") AS max_serial
                FROM "InvoicesEmployee"
                WHERE "Invoice_Number" = :invoiceNumber
            ) latest
              ON latest.max_serial = ie."Serial"
            WHERE ie."Invoice_Number" = :invoiceNumber
            GROUP BY ie."Category"
            ORDER BY ie."Category"
            """;

    private static final String CREATE_EMPLOYEES_SQL = """
            SELECT *
            FROM public.get_create_invoice_employees(:companyNumber, CAST(:dateFrom AS date), CAST(:dateTo AS date))
            """;

    private static final String INSERT_INVOICE_SQL = """
            INSERT INTO "Invoices" (
                "Serial", "Modified_Date", "Invoice_Number", "Invoice_Date", "Status",
                "Payment_Date", "Date_From", "Date_To", "Company_Number",
                "EGPAmount", "USDAmount", "EURAmount", "UserName"
            ) VALUES (
                :serial, :modifiedDate, :invoiceNumber, :invoiceDate, :status,
                NULL, :dateFrom, :dateTo, :companyNumber,
                :egpAmount, :usdAmount, :eurAmount, :userName
            )
            """;

    private static final String INSERT_INVOICE_EMPLOYEE_SQL = """
            INSERT INTO "InvoicesEmployee" (
                "Serial", "Modified_Date", "Invoice_Number", "Invoice_Date", "Status",
                "Payment_Date", "Date_From", "Date_To", "Company_Number", "Kafs_Company_Number",
                "Company_Name", "Employee_ID", "Employee_Number", "Gross_Salary", "Currency", "Category",
                "EE", "ER", "VEE", "Employee_Contribution", "VEE_Contribution", "Employer_Contribution",
                "Total_Contribution", "Proportional_Stamp_Duty", "Supervisory_Fees", "FRA_Approval_Fees",
                "FRA_Provision_Fees", "Grand_Total", "UserName"
            ) VALUES (
                :serial, :modifiedDate, :invoiceNumber, :invoiceDate, :status,
                NULL, :dateFrom, :dateTo, :companyNumber, :kafsCompanyNumber,
                :companyName, :employeeId, :employeeNumber, :grossSalary, :currency, :category,
                :ee, :er, :vee, :employeeContribution, :veeContribution, :employerContribution,
                :totalContribution, :stampDuty, :supervisoryFees, :fraApprovalFees,
                :fraProvisionFees, :grandTotal, :userName
            )
            """;

    /** Access "Settle Invoice Records": insert a new serial version, leave the previous row unchanged. */
    private static final String SETTLE_INVOICE_SQL = """
            INSERT INTO "Invoices" (
                "Serial", "Modified_Date", "Invoice_Number", "Company_Number", "Invoice_Date",
                "EGPAmount", "USDAmount", "EURAmount", "Status", "Payment_Date", "UserName",
                "Date_From", "Date_To"
            )
            SELECT
                COALESCE("Serial", 0) + 1,
                :modifiedDate,
                "Invoice_Number",
                "Company_Number",
                "Invoice_Date",
                "EGPAmount",
                "USDAmount",
                "EURAmount",
                :status,
                :paymentDate,
                :userName,
                "Date_From",
                "Date_To"
            FROM "Invoices"
            WHERE "Invoice_Number" = :invoiceNumber
              AND "Serial" = (
                  SELECT MAX(s."Serial")
                  FROM "Invoices" s
                  WHERE s."Invoice_Number" = :invoiceNumber
              )
            """;

    /** Access "Settle Invoice Employees Records". */
    private static final String SETTLE_INVOICE_LINES_SQL = """
            INSERT INTO "InvoicesEmployee" (
                "Serial", "Modified_Date", "Invoice_Number", "Invoice_Date", "Company_Number", "Company_Name",
                "Employee_ID", "Employee_Number", "Gross_Salary", "Currency", "Category",
                "EE", "ER", "VEE", "Employee_Contribution", "VEE_Contribution", "Employer_Contribution",
                "Total_Contribution", "Proportional_Stamp_Duty", "Supervisory_Fees", "FRA_Approval_Fees",
                "FRA_Provision_Fees", "Grand_Total", "Status", "Payment_Date", "UserName",
                "Date_From", "Date_To", "Kafs_Company_Number"
            )
            SELECT
                COALESCE("Serial", 0) + 1,
                :modifiedDate,
                "Invoice_Number",
                "Invoice_Date",
                "Company_Number",
                "Company_Name",
                "Employee_ID",
                "Employee_Number",
                "Gross_Salary",
                "Currency",
                "Category",
                "EE", "ER", "VEE",
                "Employee_Contribution", "VEE_Contribution", "Employer_Contribution",
                "Total_Contribution", "Proportional_Stamp_Duty", "Supervisory_Fees", "FRA_Approval_Fees",
                "FRA_Provision_Fees", "Grand_Total",
                :status,
                :paymentDate,
                :userName,
                "Date_From", "Date_To",
                "Kafs_Company_Number"
            FROM "InvoicesEmployee"
            WHERE "Invoice_Number" = :invoiceNumber
              AND "Serial" = (
                  SELECT MAX(s."Serial")
                  FROM "InvoicesEmployee" s
                  WHERE s."Invoice_Number" = :invoiceNumber
              )
            """;

    /** Access "Cancel Invoice Records": insert Cancelled serial; Payment_Date is not written. */
    private static final String CANCEL_INVOICE_SQL = """
            INSERT INTO "Invoices" (
                "Serial", "Modified_Date", "Invoice_Number", "Company_Number", "Invoice_Date",
                "EGPAmount", "USDAmount", "EURAmount", "Status", "UserName", "Date_From", "Date_To"
            )
            SELECT
                COALESCE("Serial", 0) + 1,
                :modifiedDate,
                "Invoice_Number",
                "Company_Number",
                "Invoice_Date",
                "EGPAmount",
                "USDAmount",
                "EURAmount",
                :status,
                :userName,
                "Date_From",
                "Date_To"
            FROM "Invoices"
            WHERE "Invoice_Number" = :invoiceNumber
              AND "Serial" = (
                  SELECT MAX(s."Serial")
                  FROM "Invoices" s
                  WHERE s."Invoice_Number" = :invoiceNumber
              )
            """;

    /** Access "Cancel Invoice Employees Records". */
    private static final String CANCEL_INVOICE_LINES_SQL = """
            INSERT INTO "InvoicesEmployee" (
                "Serial", "Modified_Date", "Invoice_Number", "Invoice_Date", "Company_Number", "Company_Name",
                "Employee_ID", "Employee_Number", "Gross_Salary", "Currency", "Category",
                "EE", "ER", "VEE", "Employee_Contribution", "VEE_Contribution", "Employer_Contribution",
                "Total_Contribution", "Proportional_Stamp_Duty", "Supervisory_Fees", "FRA_Approval_Fees",
                "FRA_Provision_Fees", "Grand_Total", "Status", "UserName",
                "Date_From", "Date_To", "Kafs_Company_Number"
            )
            SELECT
                COALESCE("Serial", 0) + 1,
                :modifiedDate,
                "Invoice_Number",
                "Invoice_Date",
                "Company_Number",
                "Company_Name",
                "Employee_ID",
                "Employee_Number",
                "Gross_Salary",
                "Currency",
                "Category",
                "EE", "ER", "VEE",
                "Employee_Contribution", "VEE_Contribution", "Employer_Contribution",
                "Total_Contribution", "Proportional_Stamp_Duty", "Supervisory_Fees", "FRA_Approval_Fees",
                "FRA_Provision_Fees", "Grand_Total",
                :status,
                :userName,
                "Date_From", "Date_To",
                "Kafs_Company_Number"
            FROM "InvoicesEmployee"
            WHERE "Invoice_Number" = :invoiceNumber
              AND "Serial" = (
                  SELECT MAX(s."Serial")
                  FROM "InvoicesEmployee" s
                  WHERE s."Invoice_Number" = :invoiceNumber
              )
            """;

    private static final String SETTLEMENT_SQL = """
            SELECT public.create_settlement_transactions(:invoiceNumber, CAST(:paymentDate AS date), :userName)
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CompanyRepository companyRepository;
    private final FundHoldingsSupport fundHoldingsSupport;

    public InvoiceService(
            NamedParameterJdbcTemplate jdbcTemplate,
            CompanyRepository companyRepository,
            FundHoldingsSupport fundHoldingsSupport
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.companyRepository = companyRepository;
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    public List<InvoiceDto> list(String status) {
        if (isBlank(status)) {
            return jdbcTemplate.query(
                    LIST_SQL,
                    new MapSqlParameterSource(),
                    invoiceRowMapper()
            );
        }
        return jdbcTemplate.query(
                LIST_BY_STATUS_SQL,
                new MapSqlParameterSource("status", status.trim()),
                invoiceRowMapper()
        );
    }

    public InvoiceDto get(String invoiceNumber) {
        requireNonBlank(invoiceNumber);
        List<InvoiceDto> rows = jdbcTemplate.query(
                GET_SQL,
                new MapSqlParameterSource("invoiceNumber", invoiceNumber.trim()),
                invoiceRowMapper()
        );
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice not found.");
        }
        return rows.get(0);
    }

    public InvoiceDetailsDto getDetails(String invoiceNumber) {
        InvoiceDto invoice = get(invoiceNumber);
        List<InvoiceCategorySummaryDto> categories = jdbcTemplate.query(
                DETAILS_LINES_SQL,
                new MapSqlParameterSource("invoiceNumber", invoiceNumber.trim()),
                (rs, rowNum) -> new InvoiceCategorySummaryDto(
                        integerText(rs.getString("category")),
                        rs.getInt("employee_count"),
                        blankTo(rs.getString("currency"), "EGP"),
                        rs.getDouble("employee_contribution"),
                        rs.getDouble("vee_contribution"),
                        rs.getDouble("employer_contribution"),
                        rs.getDouble("total_contribution"),
                        rs.getDouble("stamp_duty"),
                        rs.getDouble("supervisory_fees"),
                        rs.getDouble("fra_approval_fees"),
                        rs.getDouble("fra_provision_fees"),
                        rs.getDouble("grand_total")
                )
        );
        String currency = categories.stream()
                .map(InvoiceCategorySummaryDto::currency)
                .filter(value -> !isBlank(value))
                .findFirst()
                .orElse(invoice.egpAmount() > 0 ? "EGP" : invoice.usdAmount() > 0 ? "USD" : invoice.eurAmount() > 0 ? "EUR" : "EGP");
        return new InvoiceDetailsDto(
                invoice.invoiceNumber(),
                invoice.companyNumber(),
                invoice.companyName(),
                invoice.invoiceDate(),
                invoice.dateFrom(),
                invoice.dateTo(),
                invoice.status(),
                invoice.paymentDate(),
                currency,
                categories
        );
    }

    @Transactional
    public CreateInvoiceResult create(CreateInvoiceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        requireNonBlank(request.companyNumber(), request.dateFrom(), request.dateTo());
        String companyNumber = request.companyNumber().trim();
        LocalDate dateFrom = fundHoldingsSupport.parseRequiredDate(request.dateFrom());
        LocalDate dateTo = fundHoldingsSupport.parseRequiredDate(request.dateTo());

        companyRepository.findLatestByCompanyNumber(companyNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found."));

        List<Map<String, Object>> employeeRows = jdbcTemplate.queryForList(
                CREATE_EMPLOYEES_SQL,
                new MapSqlParameterSource()
                        .addValue("companyNumber", companyNumber)
                        .addValue("dateFrom", dateFrom)
                        .addValue("dateTo", dateTo)
        );
        if (employeeRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice could not be created");
        }

        String invoiceNumber = asString(employeeRows.get(0).get("Invoice_Number"));
        String userName = blankTo(request.userName(), "system");
        LocalDate invoiceDate = accessToday();
        OffsetDateTime modifiedDate = accessDate(invoiceDate);
        OffsetDateTime invoiceDateValue = accessDate(invoiceDate);
        OffsetDateTime dateFromValue = accessDate(dateFrom);
        OffsetDateTime dateToValue = accessDate(dateTo);

        double egpAmount = 0;
        double usdAmount = 0;
        double eurAmount = 0;
        for (Map<String, Object> row : employeeRows) {
            double grandTotal = number(row.get("Grand_Total"));
            String currency = blankTo(asString(row.get("Salary_Currency")), "EGP").toUpperCase();
            if (Objects.equals(currency, "USD")) {
                usdAmount += grandTotal;
            } else if (Objects.equals(currency, "EUR")) {
                eurAmount += grandTotal;
            } else {
                egpAmount += grandTotal;
            }
        }

        jdbcTemplate.update(
                INSERT_INVOICE_SQL,
                new MapSqlParameterSource()
                        .addValue("serial", 1)
                        .addValue("modifiedDate", modifiedDate)
                        .addValue("invoiceNumber", invoiceNumber)
                        .addValue("invoiceDate", invoiceDateValue)
                        .addValue("status", "Pending")
                        .addValue("dateFrom", dateFromValue)
                        .addValue("dateTo", dateToValue)
                        .addValue("companyNumber", companyNumber)
                        .addValue("egpAmount", egpAmount)
                        .addValue("usdAmount", usdAmount)
                        .addValue("eurAmount", (int) Math.round(eurAmount))
                        .addValue("userName", userName)
        );

        for (Map<String, Object> row : employeeRows) {
            jdbcTemplate.update(
                    INSERT_INVOICE_EMPLOYEE_SQL,
                    new MapSqlParameterSource()
                            .addValue("serial", 1)
                            .addValue("modifiedDate", modifiedDate)
                            .addValue("invoiceNumber", invoiceNumber)
                            .addValue("invoiceDate", invoiceDateValue)
                            .addValue("status", "Pending")
                            .addValue("dateFrom", dateFromValue)
                            .addValue("dateTo", dateToValue)
                            .addValue("companyNumber", companyNumber)
                            .addValue("kafsCompanyNumber", asString(row.get("Kafs_Company_Number")))
                            .addValue("companyName", asString(row.get("Company_Name")))
                            .addValue("employeeId", integer(row.get("Employee_ID")))
                            .addValue("employeeNumber", asString(row.get("Employee_Number")))
                            .addValue("grossSalary", number(row.get("Gross_Salary")))
                            .addValue("currency", asString(row.get("Salary_Currency")))
                            .addValue("category", asString(row.get("Category")))
                            .addValue("ee", number(row.get("EE")))
                            .addValue("er", number(row.get("ER")))
                            .addValue("vee", number(row.get("VEE")))
                            .addValue("employeeContribution", number(row.get("Employee_Contribution")))
                            .addValue("veeContribution", number(row.get("VEE_Contribution")))
                            .addValue("employerContribution", number(row.get("Employer_Contribution")))
                            .addValue("totalContribution", number(row.get("Total_Contribution")))
                            .addValue("stampDuty", number(row.get("Proportional_Stamp_Duty")))
                            .addValue("supervisoryFees", number(row.get("Supervisory_Fees")))
                            .addValue("fraApprovalFees", number(row.get("FRA_Approval_Fees")))
                            .addValue("fraProvisionFees", number(row.get("FRA_Provision_Fees")))
                            .addValue("grandTotal", number(row.get("Grand_Total")))
                            .addValue("userName", userName)
            );
        }

        return new CreateInvoiceResult(invoiceNumber, employeeRows.size(), egpAmount, usdAmount, eurAmount);
    }

    @Transactional
    public InvoiceDto settle(String invoiceNumber, SettleInvoiceRequest request) {
        requireNonBlank(invoiceNumber);
        if (request == null || isBlank(request.paymentDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }

        InvoiceDto invoice = get(invoiceNumber);
        if (!Objects.equals(invoice.status(), "Pending")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice is already " + invoice.status() + ".");
        }

        LocalDate paymentDate = fundHoldingsSupport.parseRequiredDate(request.paymentDate());
        fundHoldingsSupport.requireUnitPrice(paymentDate);

        int level = request.userSecurityLevel() == null ? 3 : request.userSecurityLevel();
        long age = ChronoUnit.DAYS.between(paymentDate, accessToday());
        if (level != 1 && level != 5 && age > 7) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please note that invoices older than 7 days cannot be settled without supervisor approval."
            );
        }

        String userName = blankTo(request.userName(), "system");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", "Paid")
                .addValue("modifiedDate", accessDate(accessToday()))
                .addValue("paymentDate", accessDate(paymentDate))
                .addValue("invoiceNumber", invoiceNumber.trim())
                .addValue("userName", userName);
        jdbcTemplate.update(SETTLE_INVOICE_SQL, params);
        jdbcTemplate.update(SETTLE_INVOICE_LINES_SQL, params);

        Integer createdRows = jdbcTemplate.queryForObject(
                SETTLEMENT_SQL,
                new MapSqlParameterSource()
                        .addValue("invoiceNumber", invoiceNumber.trim())
                        .addValue("paymentDate", paymentDate)
                        .addValue("userName", userName),
                Integer.class
        );
        if (createdRows == null || createdRows <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Settlement could not be posted.");
        }
        return get(invoiceNumber.trim());
    }

    @Transactional
    public InvoiceDto cancel(String invoiceNumber, CancelInvoiceRequest request) {
        requireNonBlank(invoiceNumber);
        if (request == null || isBlank(request.cancellationDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }

        InvoiceDto invoice = get(invoiceNumber);
        if (Objects.equals(invoice.status(), "Cancelled")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice is already cancelled.");
        }
        if (Objects.equals(invoice.status(), "Paid")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paid invoices cannot be cancelled.");
        }

        fundHoldingsSupport.parseRequiredDate(request.cancellationDate());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", "Cancelled")
                .addValue("modifiedDate", accessDate(accessToday()))
                .addValue("invoiceNumber", invoiceNumber.trim())
                .addValue("userName", blankTo(request.userName(), "system"));
        jdbcTemplate.update(CANCEL_INVOICE_SQL, params);
        jdbcTemplate.update(CANCEL_INVOICE_LINES_SQL, params);
        return get(invoiceNumber.trim());
    }

    private RowMapper<InvoiceDto> invoiceRowMapper() {
        return (rs, rowNum) -> new InvoiceDto(
                rs.getString("invoice_number"),
                rs.getString("company_number"),
                rs.getString("company_name"),
                dateText(rs.getObject("invoice_date")),
                dateText(rs.getObject("date_from")),
                dateText(rs.getObject("date_to")),
                blankTo(rs.getString("status"), "Pending"),
                dateText(rs.getObject("payment_date")),
                rs.getDouble("egp_amount"),
                rs.getDouble("usd_amount"),
                rs.getDouble("eur_amount"),
                rs.getInt("employee_line_count")
        );
    }

    private void requireNonBlank(String... values) {
        for (String value : values) {
            if (isBlank(value)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private String blankTo(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private double number(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }

    private Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private int integerText(String value) {
        if (isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private LocalDate accessToday() {
        return LocalDate.now(ACCESS_ZONE);
    }

    /** Access Date() persisted as Asia/Kuwait midnight on a timestamptz column. */
    private OffsetDateTime accessDate(LocalDate day) {
        return day.atStartOfDay(ACCESS_ZONE).toOffsetDateTime();
    }

    private String dateText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        String text = value.toString();
        return text.length() >= 10 ? text.substring(0, 10) : text;
    }

    public record CreateInvoiceRequest(
            String companyNumber,
            Integer year,
            Integer month,
            String dateFrom,
            String dateTo,
            String path,
            String userName
    ) {}

    public record CreateInvoiceResult(
            String invoiceNumber,
            int employeeLineCount,
            double egpAmount,
            double usdAmount,
            double eurAmount
    ) {}

    public record SettleInvoiceRequest(
            String paymentDate,
            Integer userSecurityLevel,
            String userName
    ) {}

    public record CancelInvoiceRequest(
            String cancellationDate,
            String userName
    ) {}

    public record InvoiceDto(
            String invoiceNumber,
            String companyNumber,
            String companyName,
            String invoiceDate,
            String dateFrom,
            String dateTo,
            String status,
            String paymentDate,
            double egpAmount,
            double usdAmount,
            double eurAmount,
            int employeeLineCount
    ) {}

    public record InvoiceCategorySummaryDto(
            int category,
            int employeeCount,
            String currency,
            double employeeContribution,
            double veeContribution,
            double employerContribution,
            double totalContribution,
            double stampDuty,
            double supervisoryFees,
            double fraApprovalFees,
            double fraProvisionFees,
            double grandTotal
    ) {}

    public record InvoiceDetailsDto(
            String invoiceNumber,
            String companyNumber,
            String companyName,
            String invoiceDate,
            String dateFrom,
            String dateTo,
            String status,
            String paymentDate,
            String currency,
            List<InvoiceCategorySummaryDto> categories
    ) {}
}
