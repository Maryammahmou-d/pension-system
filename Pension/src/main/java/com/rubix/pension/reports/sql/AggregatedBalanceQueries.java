package com.rubix.pension.reports.sql;

/**
 * Access query chain: Create Aggregated Employee Balance Report_Temporary → Step_1 → Extract.
 */
public final class AggregatedBalanceQueries {

    private AggregatedBalanceQueries() {
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

    public static final String AGGREGATED_BALANCE_SQL = """
            WITH temporary_base AS (
                SELECT
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
                    t."Company_Number" AS company_number,
                    t."Employee_Number" AS employee_number,
                    t."Employee_ID" AS employee_id,
                    t."Currency" AS currency,
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
                    (%s) AS investment_terminated_er
                FROM "Transactions" t
                INNER JOIN "LatestEmployeeRecords" ler
                    ON t."Employee_Number" = ler."Employee_Number"
                INNER JOIN "UnitPrice" up
                    ON (up."PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valuationDate AS date)
                WHERE ler."Kaf_Joining_Date" <= CAST(:valuationDate AS date)
                  AND t."Company_Number" = :companyNumber
            ),
            temporary AS (
                SELECT
                    temporary_base.*,
                    temporary_base.investment_transactional_er + temporary_base.investment_terminated_er AS investment_er,
                    temporary_base.investment_ee
                        + temporary_base.investment_vee
                        + temporary_base.investment_transactional_er
                        + temporary_base.investment_terminated_er AS investment_total,
                    temporary_base.investment_ee - temporary_base.transactional_ee_value AS investment_return_ee,
                    temporary_base.investment_vee - temporary_base.transactional_vee_value AS investment_return_vee,
                    (temporary_base.investment_transactional_er + temporary_base.investment_terminated_er)
                        - temporary_base.transactional_er_value AS investment_return_er,
                    (temporary_base.investment_ee - temporary_base.transactional_ee_value)
                        + (temporary_base.investment_vee - temporary_base.transactional_vee_value)
                        + ((temporary_base.investment_transactional_er + temporary_base.investment_terminated_er)
                            - temporary_base.transactional_er_value) AS total_investment_return
                FROM temporary_base
            ),
            step_1 AS (
                SELECT
                    temporary.price_date,
                    temporary.up1,
                    temporary.up2,
                    temporary.up3,
                    temporary.up4,
                    temporary.up5,
                    temporary.up6,
                    temporary.up7,
                    temporary.up8,
                    temporary.up9,
                    temporary.up10,
                    temporary.full_name,
                    temporary.kaf_joining_date,
                    temporary.company_number,
                    temporary.employee_number,
                    temporary.employee_id,
                    temporary.currency,
                    SUM(temporary.contribution_ee) AS sum_of_contribution_ee,
                    SUM(temporary.contribution_vee) AS sum_of_contribution_vee,
                    SUM(temporary.contribution_er) AS sum_of_contribution_er,
                    SUM(temporary.top_up_ee) AS sum_of_top_up_ee,
                    SUM(temporary.top_up_vee) AS sum_of_top_up_vee,
                    SUM(temporary.top_up_er) AS sum_of_top_up_er,
                    SUM(temporary.gross_ee_contribution) AS sum_of_gross_ee_contribution,
                    SUM(temporary.gross_vee_contribution) AS sum_of_gross_vee_contribution,
                    SUM(temporary.gross_er_contribution) AS sum_of_gross_er_contribution,
                    SUM(temporary.transactional_ee_value) AS sum_of_transactional_ee_value,
                    SUM(temporary.transactional_vee_value) AS sum_of_transactional_vee_value,
                    SUM(temporary.transactional_er_value) AS sum_of_transactional_er_value,
                    SUM(temporary.investment_ee) AS sum_of_investment_ee,
                    SUM(temporary.investment_vee) AS sum_of_investment_vee,
                    SUM(temporary.investment_transactional_er) AS sum_of_investment_transactional_er,
                    SUM(temporary.investment_terminated_er) AS sum_of_investment_terminated_er,
                    SUM(temporary.investment_er) AS sum_of_investment_er,
                    SUM(temporary.investment_total) AS sum_of_investment_total,
                    SUM(temporary.investment_return_ee) AS sum_of_investment_return_ee,
                    SUM(temporary.investment_return_vee) AS sum_of_investment_return_vee,
                    SUM(temporary.investment_return_er) AS sum_of_investment_return_er,
                    SUM(temporary.total_investment_return) AS sum_of_total_investment_return
                FROM temporary
                GROUP BY
                    temporary.price_date,
                    temporary.up1,
                    temporary.up2,
                    temporary.up3,
                    temporary.up4,
                    temporary.up5,
                    temporary.up6,
                    temporary.up7,
                    temporary.up8,
                    temporary.up9,
                    temporary.up10,
                    temporary.full_name,
                    temporary.kaf_joining_date,
                    temporary.company_number,
                    temporary.employee_number,
                    temporary.employee_id,
                    temporary.currency
            )
            SELECT
                full_name,
                employee_number,
                currency,
                sum_of_gross_ee_contribution,
                sum_of_gross_vee_contribution,
                sum_of_gross_er_contribution,
                sum_of_transactional_ee_value,
                sum_of_transactional_vee_value,
                sum_of_transactional_er_value,
                sum_of_investment_return_ee,
                sum_of_investment_return_vee,
                sum_of_investment_return_er,
                sum_of_total_investment_return,
                sum_of_investment_ee,
                sum_of_investment_vee,
                sum_of_investment_er,
                sum_of_investment_total
            FROM step_1
            """.formatted(
            TRANSACTIONAL_EE_VALUE,
            TRANSACTIONAL_VEE_VALUE,
            TRANSACTIONAL_ER_VALUE,
            INVESTMENT_EE,
            INVESTMENT_VEE,
            INVESTMENT_TRANSACTIONAL_ER,
            INVESTMENT_TERMINATED_ER
    );
}
