package com.rubix.pension.reports.sql;

public final class EmployeeBalanceQueries {

    private EmployeeBalanceQueries() {
    }

    private static String sumCoalesce(String columnPrefix, int fromFund, int toFund) {
        StringBuilder sb = new StringBuilder();
        for (int i = fromFund; i <= toFund; i += 1) {
            if (!sb.isEmpty()) {
                sb.append(" + ");
            }
            sb.append("COALESCE(t.\"").append(columnPrefix).append("_F").append(i).append("\", 0)");
        }
        return sb.toString();
    }

    private static String sumUnitsTimesPrice(String unitsPrefix, int fromFund, int toFund) {
        StringBuilder sb = new StringBuilder();
        for (int i = fromFund; i <= toFund; i += 1) {
            if (!sb.isEmpty()) {
                sb.append(" + ");
            }
            sb.append("COALESCE(t.\"").append(unitsPrefix).append("_F").append(i)
                    .append("\", 0) * up.\"Fund").append(i).append("\"");
        }
        return sb.toString();
    }

    private static final String TRANSACTIONAL_EE_VALUE = sumCoalesce("Transactional_EE_Value", 1, 10);
    private static final String TRANSACTIONAL_VEE_VALUE = sumCoalesce("Transactional_VEE_Value", 1, 10);
    private static final String TRANSACTIONAL_ER_VALUE =
            sumCoalesce("Transactional_ER_Value", 1, 10) + " + "
                    + sumCoalesce("Terminated_ER_Value", 1, 10);
    private static final String INVESTMENT_EE = sumUnitsTimesPrice("Transactional_EE_Units", 1, 10);
    private static final String INVESTMENT_VEE = sumUnitsTimesPrice("Transactional_VEE_Units", 1, 10);
    private static final String INVESTMENT_TRANSACTIONAL_ER = sumUnitsTimesPrice("Transactional_ER_Units", 1, 10);
    private static final String INVESTMENT_TERMINATED_ER = sumUnitsTimesPrice("Terminated_ER_Units", 1, 10);

    public static final String EMPLOYEE_BALANCE_SQL = """
            WITH base AS (
                SELECT
                    t."Serial" AS serial,
                    t."Currency" AS currency,
                    up."PriceDate" AS price_date,
                    up."Fund1" AS up1,
                    up."Fund2" AS up2,
                    up."Fund3" AS up3,
                    up."Fund4" AS up4,
                    up."Fund5" AS up5,
                    up."Fund6" AS up6,
                    up."Fund7" AS up7,
                    up."Fund8" AS up8,
                    up."Fund9" AS up9,
                    up."Fund10" AS up10,
                    ler."Full_Name" AS full_name,
                    ler."Kaf_Joining_Date" AS kaf_joining_date,
                    t."Description" AS description,
                    CASE
                        WHEN t."Description" = 'Settlement' THEN 'Contribution'
                        WHEN t."Description" = 'NewEmployee' THEN 'Starting Fund'
                        ELSE t."Description"
                    END AS new_description,
                    t."Payment_Date" AS payment_date,
                    t."Company_Number" AS company_number,
                    t."Employee_Number" AS employee_number,
                    t."Employee_ID" AS employee_id,
                    t."Contribution_EE" AS contribution_ee,
                    t."Contribution_VEE" AS contribution_vee,
                    t."Contribution_ER" AS contribution_er,
                    t."TopUp_EE" AS top_up_ee,
                    t."TopUp_VEE" AS top_up_vee,
                    t."TopUp_ER" AS top_up_er,
                    COALESCE(t."Contribution_EE", 0) + COALESCE(t."TopUp_EE", 0) AS gross_ee_contribution,
                    COALESCE(t."Contribution_VEE", 0) + COALESCE(t."TopUp_VEE", 0) AS gross_vee_contribution,
                    COALESCE(t."Contribution_ER", 0) + COALESCE(t."TopUp_ER", 0) AS gross_er_contribution,
                    (%s) AS transactional_ee_value,
                    (%s) AS transactional_vee_value,
                    (%s) AS transactional_er_value,
                    (%s) AS investment_ee,
                    (%s) AS investment_vee,
                    (%s) AS investment_transactional_er,
                    (%s) AS investment_terminated_er,
                    t."Retro_Date" AS retro_date
                FROM "Transactions" t
                INNER JOIN "LatestEmployeeRecords" ler
                    ON t."Employee_Number" = ler."Employee_Number"
                INNER JOIN "UnitPrice" up
                    ON (up."PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valuationDate AS date)
                WHERE ler."Kaf_Joining_Date" <= CAST(:valuationDate AS date)
                  AND t."Retro_Date" <= CAST(:valuationDate AS date)
                  AND t."Company_Number" = :companyNumber
                  AND t."Employee_Number" = :employeeNumber
            ),
            employee_balance_report AS (
                SELECT
                    base.*,
                    base.investment_transactional_er + base.investment_terminated_er AS investment_er,
                    base.investment_ee + base.investment_vee
                        + base.investment_transactional_er + base.investment_terminated_er AS investment_total,
                    base.investment_ee - base.transactional_ee_value AS investment_return_ee,
                    base.investment_vee - base.transactional_vee_value AS investment_return_vee,
                    (base.investment_transactional_er + base.investment_terminated_er)
                        - base.transactional_er_value AS investment_return_er,
                    (base.investment_ee - base.transactional_ee_value)
                        + (base.investment_vee - base.transactional_vee_value)
                        + ((base.investment_transactional_er + base.investment_terminated_er)
                            - base.transactional_er_value) AS total_investment_return
                FROM base
            ),
            monthly_charges AS (
                SELECT
                    COALESCE(SUM(employee_balance_report.transactional_ee_value), 0) AS sum_of_transactional_ee_value,
                    COALESCE(SUM(employee_balance_report.transactional_vee_value), 0) AS sum_of_transactional_vee_value,
                    COALESCE(SUM(employee_balance_report.transactional_er_value), 0) AS sum_of_transactional_er_value,
                    COALESCE(SUM(employee_balance_report.investment_ee), 0) AS sum_of_investment_ee,
                    COALESCE(SUM(employee_balance_report.investment_vee), 0) AS sum_of_investment_vee,
                    COALESCE(SUM(employee_balance_report.investment_transactional_er), 0) AS sum_of_investment_transactional_er,
                    COALESCE(SUM(employee_balance_report.investment_terminated_er), 0) AS sum_of_investment_terminated_er,
                    COALESCE(SUM(employee_balance_report.investment_er), 0) AS sum_of_investment_er,
                    COALESCE(SUM(employee_balance_report.investment_total), 0) AS sum_of_investment_total
                FROM employee_balance_report
                WHERE employee_balance_report.description = 'Monthly Charges'
            ),
            other_totals AS (
                SELECT
                    COALESCE(SUM(employee_balance_report.investment_ee), 0) AS sum_of_investment_ee,
                    COALESCE(SUM(employee_balance_report.investment_vee), 0) AS sum_of_investment_vee,
                    COALESCE(SUM(employee_balance_report.investment_transactional_er), 0) AS sum_of_investment_transactional_er,
                    COALESCE(SUM(employee_balance_report.investment_terminated_er), 0) AS sum_of_investment_terminated_er,
                    COALESCE(SUM(employee_balance_report.investment_er), 0) AS sum_of_investment_er,
                    COALESCE(SUM(employee_balance_report.investment_total), 0) AS sum_of_investment_total
                FROM employee_balance_report
                WHERE employee_balance_report.description <> 'Monthly Charges'
            ),
            step_4 AS (
                SELECT
                    employee_balance_report.serial,
                    employee_balance_report.currency,
                    employee_balance_report.price_date,
                    employee_balance_report.full_name,
                    employee_balance_report.payment_date,
                    employee_balance_report.retro_date,
                    employee_balance_report.company_number,
                    employee_balance_report.employee_number,
                    employee_balance_report.employee_id,
                    employee_balance_report.new_description,
                    employee_balance_report.gross_ee_contribution,
                    employee_balance_report.gross_vee_contribution,
                    employee_balance_report.gross_er_contribution,
                    employee_balance_report.contribution_ee,
                    employee_balance_report.contribution_vee,
                    employee_balance_report.contribution_er,
                    employee_balance_report.transactional_ee_value,
                    employee_balance_report.transactional_vee_value,
                    employee_balance_report.transactional_er_value,
                    employee_balance_report.investment_ee
                        + CASE
                            WHEN other_totals.sum_of_investment_ee = 0 THEN 0
                            ELSE monthly_charges.sum_of_investment_ee
                                * employee_balance_report.investment_ee
                                / other_totals.sum_of_investment_ee
                          END AS investment_ee,
                    employee_balance_report.investment_vee
                        + CASE
                            WHEN other_totals.sum_of_investment_vee = 0 THEN 0
                            ELSE monthly_charges.sum_of_investment_vee
                                * employee_balance_report.investment_vee
                                / other_totals.sum_of_investment_vee
                          END AS investment_vee,
                    employee_balance_report.investment_transactional_er
                        + CASE
                            WHEN other_totals.sum_of_investment_transactional_er = 0 THEN 0
                            ELSE monthly_charges.sum_of_investment_transactional_er
                                * employee_balance_report.investment_transactional_er
                                / other_totals.sum_of_investment_transactional_er
                          END AS investment_transactional_er,
                    employee_balance_report.investment_terminated_er
                        + CASE
                            WHEN other_totals.sum_of_investment_terminated_er = 0 THEN 0
                            ELSE monthly_charges.sum_of_investment_terminated_er
                                * employee_balance_report.investment_terminated_er
                                / other_totals.sum_of_investment_terminated_er
                          END AS investment_terminated_er,
                    employee_balance_report.investment_er
                        + CASE
                            WHEN other_totals.sum_of_investment_er = 0 THEN 0
                            ELSE monthly_charges.sum_of_investment_er
                                * employee_balance_report.investment_er
                                / other_totals.sum_of_investment_er
                          END AS investment_er,
                    employee_balance_report.investment_total
                        + CASE
                            WHEN other_totals.sum_of_investment_total = 0 THEN 0
                            ELSE monthly_charges.sum_of_investment_total
                                * employee_balance_report.investment_total
                                / other_totals.sum_of_investment_total
                          END AS investment_total
                FROM employee_balance_report
                CROSS JOIN monthly_charges
                CROSS JOIN other_totals
                WHERE employee_balance_report.description <> 'Monthly Charges'
            )
            SELECT
                serial,
                currency,
                price_date,
                full_name,
                payment_date,
                retro_date,
                company_number,
                employee_number,
                employee_id,
                new_description,
                gross_ee_contribution,
                gross_vee_contribution,
                gross_er_contribution,
                contribution_ee,
                contribution_vee,
                contribution_er,
                transactional_ee_value,
                transactional_vee_value,
                transactional_er_value,
                investment_ee,
                investment_vee,
                investment_transactional_er,
                investment_terminated_er,
                investment_er,
                investment_total,
                investment_ee - transactional_ee_value AS investment_return_ee,
                investment_vee - transactional_vee_value AS investment_return_vee,
                investment_er - transactional_er_value AS investment_return_er,
                (investment_ee - transactional_ee_value)
                    + (investment_vee - transactional_vee_value)
                    + (investment_er - transactional_er_value) AS total_investment_return
            FROM step_4
            ORDER BY serial
            """.formatted(
            TRANSACTIONAL_EE_VALUE,
            TRANSACTIONAL_VEE_VALUE,
            TRANSACTIONAL_ER_VALUE,
            INVESTMENT_EE,
            INVESTMENT_VEE,
            INVESTMENT_TRANSACTIONAL_ER,
            INVESTMENT_TERMINATED_ER
    );

    public static final String COMPANY_EMPLOYEE_NUMBERS_SQL = """
            SELECT ler."Employee_Number"
            FROM "LatestEmployeeRecords" ler
            WHERE ler."Company_Number" = :companyNumber
              AND (:activeOnly = false
                   OR ler."Termination_Date" IS NULL
                   OR ler."Termination_Date" > CAST(:valuationDate AS date))
            ORDER BY ler."Employee_Number"
            """;
}
