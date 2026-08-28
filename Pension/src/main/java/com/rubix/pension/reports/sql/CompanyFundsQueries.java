package com.rubix.pension.reports.sql;

/**
 * SQL that reproduces the legacy Access "Extract Companies Funds" make-table query.
 * It builds the equivalent of the Access [Companies Funds] source (latest employee
 * holdings as of a valuation date, cross-joined with the unit price for that date),
 * then aggregates all fund unit/value columns by Company_Number so the output
 * matches the TempFunds staging table layout.
 */
public final class CompanyFundsQueries {

    private CompanyFundsQueries() {
    }

    private static final int FUND_COUNT = 10;

    public static final String COMPANY_FUNDS_SQL = """
            WITH latest_holdings AS (
                SELECT
                    t."Company_Number",
                    %s
                FROM "Transactions" t
                INNER JOIN (
                    SELECT "Employee_Number", MAX("Serial") AS max_serial
                    FROM "Transactions"
                    WHERE "Modified_Date" <= CAST(:valuationDate AS date)
                    GROUP BY "Employee_Number"
                ) latest ON t."Employee_Number" = latest."Employee_Number"
                         AND t."Serial" = latest.max_serial
            ),
            latest_unit_prices AS (
                SELECT %s
                FROM "UnitPrice"
                WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valuationDate AS date)
                ORDER BY "ID" DESC
                LIMIT 1
            )
            SELECT
                h."Company_Number" AS "Company_Number",
                %s
            FROM latest_holdings h
            CROSS JOIN latest_unit_prices p
            GROUP BY h."Company_Number"
            ORDER BY h."Company_Number"
            """.formatted(
                    unitColumns(),
                    priceColumns(),
                    aggregateColumns()
            );

    private static String unitColumns() {
        StringBuilder sb = new StringBuilder();
        appendUnitColumns(sb, "Total_EE_Units_F", "ee");
        appendUnitColumns(sb, "Total_VEE_Units_F", "vee");
        appendUnitColumns(sb, "Total_ER_Units_F", "er");
        return sb.toString();
    }

    private static void appendUnitColumns(StringBuilder sb, String prefix, String alias) {
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            if (!sb.isEmpty()) {
                sb.append(",\n                    ");
            }
            sb.append("COALESCE(t.\"").append(prefix).append(i).append("\", 0) AS ").append(alias).append(i);
        }
    }

    private static String priceColumns() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            if (i > 1) {
                sb.append(", ");
            }
            sb.append("COALESCE(\"Fund").append(i).append("\", 0) AS f").append(i);
        }
        return sb.toString();
    }

    private static String aggregateColumns() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb, "SUM(h.ee" + i + ")", "Total_EE_Units_F" + i);
        }
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb, "SUM(h.vee" + i + ")", "Total_VEE_Units_F" + i);
        }
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb, "SUM(h.er" + i + ")", "Total_ER_Units_F" + i);
        }
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb, "SUM(h.ee" + i + " * p.f" + i + ")", "Total_EE_Value_F" + i);
        }
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb, "SUM(h.vee" + i + " * p.f" + i + ")", "Total_VEE_Value_F" + i);
        }
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb, "SUM(h.er" + i + " * p.f" + i + ")", "Total_ER_Value_F" + i);
        }
        for (int i = 1; i <= FUND_COUNT; i += 1) {
            appendColumn(sb,
                    "SUM(h.ee" + i + " * p.f" + i
                            + " + h.vee" + i + " * p.f" + i
                            + " + h.er" + i + " * p.f" + i + ")",
                    "Total_Value_F" + i);
        }
        return sb.toString();
    }

    private static void appendColumn(StringBuilder sb, String expression, String alias) {
        if (!sb.isEmpty()) {
            sb.append(",\n                ");
        }
        sb.append(expression).append(" AS \"").append(alias).append("\"");
    }
}
