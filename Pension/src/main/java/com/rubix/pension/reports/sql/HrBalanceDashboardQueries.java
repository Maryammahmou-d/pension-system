package com.rubix.pension.reports.sql;

public final class HrBalanceDashboardQueries {

    private HrBalanceDashboardQueries() {
    }

    public static final String CHECK_UNIT_PRICE_SQL = """
            SELECT COUNT(*)
            FROM "UnitPrice"
            WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valuationDate AS date)
            """;

    public static final String CHECK_DUPLICATE_DASHBOARD_SQL = """
            SELECT COUNT(*)
            FROM "HRDashboard"
            WHERE ("Valuation_Date" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valuationDate AS date)
            """;

    public static final String PREVIOUS_RUNS_SQL = """
            SELECT "ValuationDate" AS valuation_date
            FROM "DashboardDates"
            """;

    public static final String INSERT_HR_DASHBOARD_COUNTS_SQL = """
            INSERT INTO "HRDashboard Counts" (
                "Valuation_Date",
                "Company_Number",
                "Joined_Count",
                "Terminated_Count",
                "Active_Count"
            )
            WITH max_emp AS (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Employees"
                WHERE ("Kaf_Joining_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:valDate AS date)
                GROUP BY "Employee_Number"
            ),
            latest_emp_as_of_date AS (
                SELECT e.*
                FROM "Employees" e
                INNER JOIN max_emp m
                   ON e."Employee_Number" = m."Employee_Number"
                  AND e."Serial" = m.max_serial
            )
            SELECT
                CAST(:valDate AS timestamp with time zone) AS "Valuation_Date",
                "Company_Number",
                COUNT(CASE
                    WHEN ("Kaf_Joining_Date" AT TIME ZONE 'Asia/Kuwait')::date
                         BETWEEN date_trunc('month', CAST(:valDate AS date))::date AND CAST(:valDate AS date)
                    THEN 1
                    ELSE NULL
                END) AS "Joined_Count",
                COUNT(CASE
                    WHEN ("Termination_Date" AT TIME ZONE 'Asia/Kuwait')::date
                         BETWEEN date_trunc('month', CAST(:valDate AS date))::date AND CAST(:valDate AS date)
                    THEN 1
                    ELSE NULL
                END) AS "Terminated_Count",
                COUNT(CASE
                    WHEN ("Kaf_Joining_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:valDate AS date)
                     AND ("Termination_Date" IS NULL OR ("Termination_Date" AT TIME ZONE 'Asia/Kuwait')::date > CAST(:valDate AS date))
                    THEN 1
                    ELSE NULL
                END) AS "Active_Count"
            FROM latest_emp_as_of_date
            GROUP BY "Company_Number"
            ORDER BY "Company_Number"
            """;

    public static final String INSERT_UNIT_PRICE_ANNUALIZED_GAIN_SQL = """
            INSERT INTO "UnitPriceAnnualizedGain" (
                "Valuation_Date",
                "Fund1_Gain",
                "Fund2_Gain",
                "Fund3_Gain",
                "Fund4_Gain",
                "Fund5_Gain",
                "Fund6_Gain",
                "Fund7_Gain",
                "Fund8_Gain",
                "Fund9_Gain",
                "Fund10_Gain"
            )
            WITH up_tm AS (
                SELECT *
                FROM "UnitPrice"
                WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:tm AS date)
                LIMIT 1
            ),
            up_lm AS (
                SELECT *
                FROM "UnitPrice"
                WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:lm AS date)
                LIMIT 1
            )
            SELECT
                CAST(:valDate AS timestamp with time zone) AS "Valuation_Date",
                CASE WHEN COALESCE(lm."Fund1", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund1" / lm."Fund1", 12) - 1.0) END AS "Fund1_Gain",
                CASE WHEN COALESCE(lm."Fund2", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund2" / lm."Fund2", 12) - 1.0) END AS "Fund2_Gain",
                CASE WHEN COALESCE(lm."Fund3", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund3" / lm."Fund3", 12) - 1.0) END AS "Fund3_Gain",
                CASE WHEN COALESCE(lm."Fund4", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund4" / lm."Fund4", 12) - 1.0) END AS "Fund4_Gain",
                CASE WHEN COALESCE(lm."Fund5", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund5" / lm."Fund5", 12) - 1.0) END AS "Fund5_Gain",
                CASE WHEN COALESCE(lm."Fund6", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund6" / lm."Fund6", 12) - 1.0) END AS "Fund6_Gain",
                CASE WHEN COALESCE(lm."Fund7", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund7" / lm."Fund7", 12) - 1.0) END AS "Fund7_Gain",
                CASE WHEN COALESCE(lm."Fund8", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund8" / lm."Fund8", 12) - 1.0) END AS "Fund8_Gain",
                CASE WHEN COALESCE(lm."Fund9", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund9" / lm."Fund9", 12) - 1.0) END AS "Fund9_Gain",
                CASE WHEN COALESCE(lm."Fund10", 0) = 0 THEN 0 ELSE 100.0 * (POWER(tm."Fund10" / lm."Fund10", 12) - 1.0) END AS "Fund10_Gain"
            FROM up_tm tm
            CROSS JOIN up_lm lm
            """;

    public static final String INSERT_HR_DASHBOARD_SQL = """
            INSERT INTO "HRDashboard" (
                "Valuation_Date", "Company_Number", "Employee_Number", "Employee_ID",
                "Contribution_EE", "Contribution_VEE", "Contribution_ER", "Contribution_Total",
                "TopUp_EE", "TopUp_VEE", "TopUp_ER", "TopUp_Total",
                "Withdrawal_EE", "Withdrawal_VEE", "Withdrawal_ER", "Withdrawal_Total",
                "Surrender_EE", "Surrender_VEE", "Surrender_ER", "Surrender_Total",
                "IMC_EE", "IMC_VEE", "IMC_ER", "IMC_Total",
                "Admin_Charges_EE", "Admin_Charges_ER", "Admin_Charges_Total",
                "Contribution_Charges_EE", "Contribution_Charges_VEE", "Contribution_Charges_ER", "Contribution_Charges_Total",
                "Surrender_Charges_EE", "Surrender_Charges_VEE", "Surrender_Charges_ER", "Surrender_Charges_Total",
                "Top_Up_Charges_EE", "Top_Up_Charges_VEE", "Top_Up_Charges_ER", "Top_Up_Charges_Total",
                "Withdrawal_Charges_EE", "Withdrawal_Charges_VEE", "Withdrawal_Charges_ER", "Withdrawal_Charges_Total",
                "InvestmentReturn_EE", "InvestmentReturn_VEE", "InvestmentReturn_ER", "InvestmentReturn_Total",
                "Fund_Value_EE", "Fund_Value_VEE", "Fund_Value_ER", "Fund_Value_Total",
                "Vested_Amount",
                "Weight_F1_EE", "Weight_F2_EE", "Weight_F3_EE", "Weight_F4_EE", "Weight_F5_EE",
                "Weight_F1_ER", "Weight_F2_ER", "Weight_F3_ER", "Weight_F4_ER", "Weight_F5_ER"
            )
            WITH max_emp AS (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Employees"
                WHERE ("Kaf_Joining_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:valDate AS date)
                GROUP BY "Employee_Number"
            ),
            latest_emp_as_of_date AS (
                SELECT e.*
                FROM "Employees" e
                INNER JOIN max_emp m
                   ON e."Employee_Number" = m."Employee_Number"
                  AND e."Serial" = m.max_serial
            ),
            max_comp AS (
                SELECT "Company_Number", MAX("Serial") AS max_serial
                FROM "Companies"
                WHERE ("Modified_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:valDate AS date)
                GROUP BY "Company_Number"
            ),
            latest_comp_as_of_date AS (
                SELECT c.*
                FROM "Companies" c
                INNER JOIN max_comp mc
                   ON c."Company_Number" = mc."Company_Number"
                  AND c."Serial" = mc.max_serial
            ),
            vesting_rules AS (
                SELECT
                    v."Company_Number",
                    le."Employee_Number",
                    CASE
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 1 THEN v."Year1"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 2 THEN v."Year2"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 3 THEN v."Year3"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 4 THEN v."Year4"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 5 THEN v."Year5"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 6 THEN v."Year6"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 7 THEN v."Year7"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 8 THEN v."Year8"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 9 THEN v."Year9"
                        WHEN (CAST(:valDate AS date) - (CASE WHEN lc."Vesting_On_Hire" = true THEN le."Hire_Date" ELSE le."Pension_Start_Date" END)::date) / 365.26 < 10 THEN v."Year10"
                        ELSE 100.0
                    END / 100.0 AS "VestingRule"
                FROM "Vesting" v
                INNER JOIN latest_comp_as_of_date lc ON v."Company_Number" = lc."Company_Number"
                INNER JOIN latest_emp_as_of_date le ON v."Company_Number" = le."Company_Number"
            ),
            temp_calc AS (
                SELECT
                    CAST(:valDate AS timestamp with time zone) AS "Valuation_Date",
                    t."Company_Number",
                    t."Employee_Number",
                    t."Employee_ID",
                    SUM(COALESCE(t."Contribution_EE", 0)) AS "Contribution_EE",
                    SUM(COALESCE(t."Contribution_VEE", 0)) AS "Contribution_VEE",
                    SUM(COALESCE(t."Contribution_ER", 0)) AS "Contribution_ER",
                    SUM(COALESCE(t."Contribution_Total", 0)) AS "Contribution_Total",
                    SUM(COALESCE(t."TopUp_EE", 0)) AS "TopUp_EE",
                    SUM(COALESCE(t."TopUp_VEE", 0)) AS "TopUp_VEE",
                    SUM(COALESCE(t."TopUp_ER", 0)) AS "TopUp_ER",
                    SUM(COALESCE(t."TopUp_Total", 0)) AS "TopUp_Total",
                    SUM(COALESCE(t."Withdrawal_EE", 0)) AS "Withdrawal_EE",
                    SUM(COALESCE(t."Withdrawal_VEE", 0)) AS "Withdrawal_VEE",
                    SUM(COALESCE(t."Withdrawal_ER", 0)) AS "Withdrawal_ER",
                    SUM(COALESCE(t."Withdrawal_Total", 0)) AS "Withdrawal_Total",
                    - SUM(CASE
                        WHEN t."Description" = 'Termination' THEN
                            COALESCE(t."Transactional_EE_Value_F1", 0) + COALESCE(t."Transactional_EE_Value_F2", 0) +
                            COALESCE(t."Transactional_EE_Value_F3", 0) + COALESCE(t."Transactional_EE_Value_F4", 0) +
                            COALESCE(t."Transactional_EE_Value_F5", 0) + COALESCE(t."Transactional_EE_Value_F6", 0) +
                            COALESCE(t."Transactional_EE_Value_F7", 0) + COALESCE(t."Transactional_EE_Value_F8", 0) +
                            COALESCE(t."Transactional_EE_Value_F9", 0) + COALESCE(t."Transactional_EE_Value_F10", 0)
                        ELSE 0
                    END) AS "Surrender_EE",
                    - SUM(CASE
                        WHEN t."Description" = 'Termination' THEN
                            COALESCE(t."Transactional_VEE_Value_F1", 0) + COALESCE(t."Transactional_VEE_Value_F2", 0) +
                            COALESCE(t."Transactional_VEE_Value_F3", 0) + COALESCE(t."Transactional_VEE_Value_F4", 0) +
                            COALESCE(t."Transactional_VEE_Value_F5", 0) + COALESCE(t."Transactional_VEE_Value_F6", 0) +
                            COALESCE(t."Transactional_VEE_Value_F7", 0) + COALESCE(t."Transactional_VEE_Value_F8", 0) +
                            COALESCE(t."Transactional_VEE_Value_F9", 0) + COALESCE(t."Transactional_VEE_Value_F10", 0)
                        ELSE 0
                    END) AS "Surrender_VEE",
                    - SUM(CASE
                        WHEN t."Description" = 'Termination' THEN
                            COALESCE(t."Transactional_ER_Value_F1", 0) + COALESCE(t."Transactional_ER_Value_F2", 0) +
                            COALESCE(t."Transactional_ER_Value_F3", 0) + COALESCE(t."Transactional_ER_Value_F4", 0) +
                            COALESCE(t."Transactional_ER_Value_F5", 0) + COALESCE(t."Transactional_ER_Value_F6", 0) +
                            COALESCE(t."Transactional_ER_Value_F7", 0) + COALESCE(t."Transactional_ER_Value_F8", 0) +
                            COALESCE(t."Transactional_ER_Value_F9", 0) + COALESCE(t."Transactional_ER_Value_F10", 0) +
                            COALESCE(t."Terminated_ER_Value_F1", 0) + COALESCE(t."Terminated_ER_Value_F2", 0) +
                            COALESCE(t."Terminated_ER_Value_F3", 0) + COALESCE(t."Terminated_ER_Value_F4", 0) +
                            COALESCE(t."Terminated_ER_Value_F5", 0) + COALESCE(t."Terminated_ER_Value_F6", 0) +
                            COALESCE(t."Terminated_ER_Value_F7", 0) + COALESCE(t."Terminated_ER_Value_F8", 0) +
                            COALESCE(t."Terminated_ER_Value_F9", 0) + COALESCE(t."Terminated_ER_Value_F10", 0)
                        ELSE 0
                    END) AS "Surrender_ER",
                    SUM(COALESCE(t."IMC_EE", 0)) AS "IMC_EE",
                    SUM(COALESCE(t."IMC_VEE", 0)) AS "IMC_VEE",
                    SUM(COALESCE(t."IMC_ER", 0)) AS "IMC_ER",
                    SUM(COALESCE(t."IMC_Total", 0)) AS "IMC_Total",
                    SUM(COALESCE(t."Admin_Charges_EE", 0)) AS "Admin_Charges_EE",
                    SUM(COALESCE(t."Admin_Charges_ER", 0)) AS "Admin_Charges_ER",
                    SUM(COALESCE(t."Admin_Charges_Total", 0)) AS "Admin_Charges_Total",
                    SUM(COALESCE(t."Contribution_Charges_EE", 0)) AS "Contribution_Charges_EE",
                    SUM(COALESCE(t."Contribution_Charges_VEE", 0)) AS "Contribution_Charges_VEE",
                    SUM(COALESCE(t."Contribution_Charges_ER", 0)) AS "Contribution_Charges_ER",
                    SUM(COALESCE(t."Contribution_Charges_Total", 0)) AS "Contribution_Charges_Total",
                    SUM(COALESCE(t."Surrender_Charges_EE", 0)) AS "Surrender_Charges_EE",
                    SUM(COALESCE(t."Surrender_Charges_VEE", 0)) AS "Surrender_Charges_VEE",
                    SUM(COALESCE(t."Surrender_Charges_ER", 0)) AS "Surrender_Charges_ER",
                    SUM(COALESCE(t."Surrender_Charges_Total", 0)) AS "Surrender_Charges_Total",
                    SUM(COALESCE(t."Top_Up_Charges_EE", 0)) AS "Top_Up_Charges_EE",
                    SUM(COALESCE(t."Top_Up_Charges_VEE", 0)) AS "Top_Up_Charges_VEE",
                    SUM(COALESCE(t."Top_Up_Charges_ER", 0)) AS "Top_Up_Charges_ER",
                    SUM(COALESCE(t."Top_Up_Charges_Total", 0)) AS "Top_Up_Charges_Total",
                    SUM(COALESCE(t."Withdrawal_Charges_EE", 0)) AS "Withdrawal_Charges_EE",
                    SUM(COALESCE(t."Withdrawal_Charges_VEE", 0)) AS "Withdrawal_Charges_VEE",
                    SUM(COALESCE(t."Withdrawal_Charges_ER", 0)) AS "Withdrawal_Charges_ER",
                    SUM(COALESCE(t."Withdrawal_Charges_Total", 0)) AS "Withdrawal_Charges_Total",
                    SUM(
                        COALESCE(t."Transactional_EE_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_EE_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_EE_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_EE_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_EE_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_EE_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_EE_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_EE_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_EE_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_EE_Units_F10", 0) * up."Fund10" -
                        t."Transactional_EE_Value"
                    ) AS "InvestmentReturn_EE",
                    SUM(
                        COALESCE(t."Transactional_VEE_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_VEE_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_VEE_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_VEE_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_VEE_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_VEE_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_VEE_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_VEE_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_VEE_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_VEE_Units_F10", 0) * up."Fund10" -
                        t."Transactional_VEE_Value"
                    ) AS "InvestmentReturn_VEE",
                    SUM(
                        COALESCE(t."Transactional_ER_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_ER_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_ER_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_ER_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_ER_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_ER_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_ER_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_ER_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_ER_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_ER_Units_F10", 0) * up."Fund10" +
                        COALESCE(t."Terminated_ER_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Terminated_ER_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Terminated_ER_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Terminated_ER_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Terminated_ER_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Terminated_ER_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Terminated_ER_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Terminated_ER_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Terminated_ER_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Terminated_ER_Units_F10", 0) * up."Fund10" -
                        t."Transactional_ER_Value" -
                        COALESCE(t."Terminated_ER_Value", 0)
                    ) AS "InvestmentReturn_ER",
                    SUM(
                        COALESCE(t."Transactional_Total_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_Total_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_Total_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_Total_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_Total_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_Total_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_Total_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_Total_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_Total_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_Total_Units_F10", 0) * up."Fund10" -
                        t."Transactional_Total_Value"
                    ) AS "InvestmentReturn_Total",
                    SUM(
                        COALESCE(t."Transactional_EE_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_EE_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_EE_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_EE_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_EE_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_EE_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_EE_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_EE_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_EE_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_EE_Units_F10", 0) * up."Fund10"
                    ) AS "Fund_Value_EE",
                    SUM(
                        COALESCE(t."Transactional_VEE_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_VEE_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_VEE_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_VEE_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_VEE_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_VEE_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_VEE_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_VEE_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_VEE_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_VEE_Units_F10", 0) * up."Fund10"
                    ) AS "Fund_Value_VEE",
                    SUM(
                        COALESCE(t."Transactional_ER_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_ER_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_ER_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_ER_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_ER_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_ER_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_ER_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_ER_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_ER_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_ER_Units_F10", 0) * up."Fund10" +
                        COALESCE(t."Terminated_ER_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Terminated_ER_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Terminated_ER_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Terminated_ER_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Terminated_ER_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Terminated_ER_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Terminated_ER_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Terminated_ER_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Terminated_ER_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Terminated_ER_Units_F10", 0) * up."Fund10"
                    ) AS "Fund_Value_ER",
                    SUM(
                        COALESCE(t."Transactional_Total_Units_F1", 0) * up."Fund1" +
                        COALESCE(t."Transactional_Total_Units_F2", 0) * up."Fund2" +
                        COALESCE(t."Transactional_Total_Units_F3", 0) * up."Fund3" +
                        COALESCE(t."Transactional_Total_Units_F4", 0) * up."Fund4" +
                        COALESCE(t."Transactional_Total_Units_F5", 0) * up."Fund5" +
                        COALESCE(t."Transactional_Total_Units_F6", 0) * up."Fund6" +
                        COALESCE(t."Transactional_Total_Units_F7", 0) * up."Fund7" +
                        COALESCE(t."Transactional_Total_Units_F8", 0) * up."Fund8" +
                        COALESCE(t."Transactional_Total_Units_F9", 0) * up."Fund9" +
                        COALESCE(t."Transactional_Total_Units_F10", 0) * up."Fund10"
                    ) AS "Fund_Value_Total",
                    vr."VestingRule",
                    le."Weight_F1_EE", le."Weight_F2_EE", le."Weight_F3_EE", le."Weight_F4_EE", le."Weight_F5_EE",
                    le."Weight_F1_ER", le."Weight_F2_ER", le."Weight_F3_ER", le."Weight_F4_ER", le."Weight_F5_ER"
                FROM "Transactions" t
                INNER JOIN vesting_rules vr ON t."Employee_Number" = vr."Employee_Number"
                INNER JOIN latest_emp_as_of_date le ON t."Employee_Number" = le."Employee_Number"
                INNER JOIN "UnitPrice" up ON (up."PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valDate AS date)
                WHERE (t."Modified_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:valDate AS date)
                GROUP BY
                    t."Company_Number", t."Employee_Number", t."Employee_ID",
                    up."Fund1", up."Fund2", up."Fund3", up."Fund4", up."Fund5",
                    up."Fund6", up."Fund7", up."Fund8", up."Fund9", up."Fund10",
                    vr."VestingRule",
                    le."Weight_F1_EE", le."Weight_F2_EE", le."Weight_F3_EE", le."Weight_F4_EE", le."Weight_F5_EE",
                    le."Weight_F1_ER", le."Weight_F2_ER", le."Weight_F3_ER", le."Weight_F4_ER", le."Weight_F5_ER"
            )
            SELECT
                "Valuation_Date", "Company_Number", "Employee_Number", "Employee_ID",
                "Contribution_EE", "Contribution_VEE", "Contribution_ER", "Contribution_Total",
                "TopUp_EE", "TopUp_VEE", "TopUp_ER", "TopUp_Total",
                "Withdrawal_EE", "Withdrawal_VEE", "Withdrawal_ER", "Withdrawal_Total",
                "Surrender_EE", "Surrender_VEE", "Surrender_ER",
                "Surrender_EE" + "Surrender_VEE" + "Surrender_ER" AS "Surrender_Total",
                "IMC_EE", "IMC_VEE", "IMC_ER", "IMC_Total",
                "Admin_Charges_EE", "Admin_Charges_ER", "Admin_Charges_Total",
                "Contribution_Charges_EE", "Contribution_Charges_VEE", "Contribution_Charges_ER", "Contribution_Charges_Total",
                "Surrender_Charges_EE", "Surrender_Charges_VEE", "Surrender_Charges_ER", "Surrender_Charges_Total",
                "Top_Up_Charges_EE", "Top_Up_Charges_VEE", "Top_Up_Charges_ER", "Top_Up_Charges_Total",
                "Withdrawal_Charges_EE", "Withdrawal_Charges_VEE", "Withdrawal_Charges_ER", "Withdrawal_Charges_Total",
                "InvestmentReturn_EE", "InvestmentReturn_VEE", "InvestmentReturn_ER", "InvestmentReturn_Total",
                "Fund_Value_EE", "Fund_Value_VEE", "Fund_Value_ER", "Fund_Value_Total",
                "Fund_Value_EE" + "Fund_Value_VEE" + ("Fund_Value_ER" * "VestingRule") AS "Vested_Amount",
                "Weight_F1_EE", "Weight_F2_EE", "Weight_F3_EE", "Weight_F4_EE", "Weight_F5_EE",
                "Weight_F1_ER", "Weight_F2_ER", "Weight_F3_ER", "Weight_F4_ER", "Weight_F5_ER"
            FROM temp_calc
            ORDER BY "Company_Number", "Employee_ID"
            """;

    public static final String INSERT_MONTHLY_HR_DASHBOARD_SQL = """
            INSERT INTO "HRDashboard Monthly" (
                "Valuation_Date", "Company_Number", "Employee_Number", "Employee_ID",
                "Contribution_EE", "Contribution_VEE", "Contribution_ER", "Contribution_Total",
                "TopUp_EE", "TopUp_VEE", "TopUp_ER", "TopUp_Total",
                "Withdrawal_EE", "Withdrawal_VEE", "Withdrawal_ER", "Withdrawal_Total",
                "Surrender_EE", "Surrender_VEE", "Surrender_ER", "Surrender_Total",
                "IMC_EE", "IMC_VEE", "IMC_ER", "IMC_Total",
                "Admin_Charges_EE", "Admin_Charges_ER", "Admin_Charges_Total",
                "Contribution_Charges_EE", "Contribution_Charges_VEE", "Contribution_Charges_ER", "Contribution_Charges_Total",
                "Surrender_Charges_EE", "Surrender_Charges_VEE", "Surrender_Charges_ER", "Surrender_Charges_Total",
                "Top_Up_Charges_EE", "Top_Up_Charges_VEE", "Top_Up_Charges_ER", "Top_Up_Charges_Total",
                "Withdrawal_Charges_EE", "Withdrawal_Charges_VEE", "Withdrawal_Charges_ER", "Withdrawal_Charges_Total",
                "Fund_Value_EE", "Fund_Value_VEE", "Fund_Value_ER", "Fund_Value_Total",
                "InvestmentReturn_EE", "InvestmentReturn_VEE", "InvestmentReturn_ER", "InvestmentReturn_Total"
            )
            WITH max_tx_tm AS (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Transactions"
                WHERE ("Modified_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:tm AS date)
                GROUP BY "Employee_Number"
            ),
            fund_value_tm AS (
                SELECT
                    t."Company_Number", t."Employee_Number", t."Employee_ID",
                    COALESCE(t."Total_EE_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_EE_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_EE_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_EE_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_EE_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_EE_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_EE_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_EE_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_EE_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_EE_Units_F10", 0) * up."Fund10" AS "Total_EE_Value_TM",
                    COALESCE(t."Total_VEE_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_VEE_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_VEE_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_VEE_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_VEE_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_VEE_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_VEE_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_VEE_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_VEE_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_VEE_Units_F10", 0) * up."Fund10" AS "Total_VEE_Value_TM",
                    COALESCE(t."Total_ER_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_ER_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_ER_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_ER_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_ER_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_ER_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_ER_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_ER_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_ER_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_ER_Units_F10", 0) * up."Fund10" AS "Total_ER_Value_TM",
                    COALESCE(t."Total_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_Units_F10", 0) * up."Fund10" AS "Total_Value_TM"
                FROM "Transactions" t
                INNER JOIN max_tx_tm m
                   ON t."Employee_Number" = m."Employee_Number"
                  AND t."Serial" = m.max_serial
                INNER JOIN "UnitPrice" up
                   ON (up."PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:tm AS date)
            ),
            max_tx_lm AS (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Transactions"
                WHERE ("Modified_Date" AT TIME ZONE 'Asia/Kuwait')::date <= CAST(:lm AS date)
                GROUP BY "Employee_Number"
            ),
            fund_value_lm AS (
                SELECT
                    t."Company_Number", t."Employee_Number", t."Employee_ID",
                    COALESCE(t."Total_EE_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_EE_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_EE_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_EE_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_EE_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_EE_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_EE_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_EE_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_EE_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_EE_Units_F10", 0) * up."Fund10" AS "Total_EE_Value_LM",
                    COALESCE(t."Total_VEE_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_VEE_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_VEE_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_VEE_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_VEE_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_VEE_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_VEE_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_VEE_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_VEE_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_VEE_Units_F10", 0) * up."Fund10" AS "Total_VEE_Value_LM",
                    COALESCE(t."Total_ER_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_ER_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_ER_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_ER_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_ER_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_ER_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_ER_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_ER_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_ER_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_ER_Units_F10", 0) * up."Fund10" AS "Total_ER_Value_LM",
                    COALESCE(t."Total_Units_F1", 0) * up."Fund1" +
                    COALESCE(t."Total_Units_F2", 0) * up."Fund2" +
                    COALESCE(t."Total_Units_F3", 0) * up."Fund3" +
                    COALESCE(t."Total_Units_F4", 0) * up."Fund4" +
                    COALESCE(t."Total_Units_F5", 0) * up."Fund5" +
                    COALESCE(t."Total_Units_F6", 0) * up."Fund6" +
                    COALESCE(t."Total_Units_F7", 0) * up."Fund7" +
                    COALESCE(t."Total_Units_F8", 0) * up."Fund8" +
                    COALESCE(t."Total_Units_F9", 0) * up."Fund9" +
                    COALESCE(t."Total_Units_F10", 0) * up."Fund10" AS "Total_Value_LM"
                FROM "Transactions" t
                INNER JOIN max_tx_lm m
                   ON t."Employee_Number" = m."Employee_Number"
                  AND t."Serial" = m.max_serial
                INNER JOIN "UnitPrice" up
                   ON (up."PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:lm AS date)
            ),
            fund_comparison AS (
                SELECT
                    tm."Company_Number",
                    tm."Employee_Number",
                    tm."Employee_ID",
                    tm."Total_EE_Value_TM",
                    tm."Total_VEE_Value_TM",
                    tm."Total_ER_Value_TM",
                    tm."Total_Value_TM",
                    lm."Total_EE_Value_LM",
                    lm."Total_VEE_Value_LM",
                    lm."Total_ER_Value_LM",
                    lm."Total_Value_LM"
                FROM fund_value_tm tm
                LEFT JOIN fund_value_lm lm ON tm."Employee_Number" = lm."Employee_Number"
            ),
            month_tx AS (
                SELECT *
                FROM "Transactions"
                WHERE ("Modified_Date" AT TIME ZONE 'Asia/Kuwait')::date
                      BETWEEN date_trunc('month', CAST(:tm AS date))::date AND CAST(:tm AS date)
            ),
            temp_calc AS (
                SELECT
                    CAST(:tm AS timestamp with time zone) AS "Valuation_Date",
                    fc."Company_Number",
                    fc."Employee_Number",
                    fc."Employee_ID",
                    COALESCE(SUM(tx."Contribution_EE"), 0) AS "Contribution_EE",
                    COALESCE(SUM(tx."Contribution_VEE"), 0) AS "Contribution_VEE",
                    COALESCE(SUM(tx."Contribution_ER"), 0) AS "Contribution_ER",
                    COALESCE(SUM(tx."Contribution_Total"), 0) AS "Contribution_Total",
                    COALESCE(SUM(tx."TopUp_EE"), 0) AS "TopUp_EE",
                    COALESCE(SUM(tx."TopUp_VEE"), 0) AS "TopUp_VEE",
                    COALESCE(SUM(tx."TopUp_ER"), 0) AS "TopUp_ER",
                    COALESCE(SUM(tx."TopUp_Total"), 0) AS "TopUp_Total",
                    COALESCE(SUM(COALESCE(tx."Withdrawal_EE", 0)), 0) AS "Withdrawal_EE",
                    COALESCE(SUM(COALESCE(tx."Withdrawal_VEE", 0)), 0) AS "Withdrawal_VEE",
                    COALESCE(SUM(COALESCE(tx."Withdrawal_ER", 0)), 0) AS "Withdrawal_ER",
                    COALESCE(SUM(COALESCE(tx."Withdrawal_Total", 0)), 0) AS "Withdrawal_Total",
                    COALESCE(-SUM(CASE
                        WHEN tx."Description" = 'Termination' THEN
                            COALESCE(tx."Transactional_EE_Value_F1", 0) + COALESCE(tx."Transactional_EE_Value_F2", 0) +
                            COALESCE(tx."Transactional_EE_Value_F3", 0) + COALESCE(tx."Transactional_EE_Value_F4", 0) +
                            COALESCE(tx."Transactional_EE_Value_F5", 0) + COALESCE(tx."Transactional_EE_Value_F6", 0) +
                            COALESCE(tx."Transactional_EE_Value_F7", 0) + COALESCE(tx."Transactional_EE_Value_F8", 0) +
                            COALESCE(tx."Transactional_EE_Value_F9", 0) + COALESCE(tx."Transactional_EE_Value_F10", 0)
                        ELSE 0
                    END), 0) AS "Surrender_EE",
                    COALESCE(-SUM(CASE
                        WHEN tx."Description" = 'Termination' THEN
                            COALESCE(tx."Transactional_VEE_Value_F1", 0) + COALESCE(tx."Transactional_VEE_Value_F2", 0) +
                            COALESCE(tx."Transactional_VEE_Value_F3", 0) + COALESCE(tx."Transactional_VEE_Value_F4", 0) +
                            COALESCE(tx."Transactional_VEE_Value_F5", 0) + COALESCE(tx."Transactional_VEE_Value_F6", 0) +
                            COALESCE(tx."Transactional_VEE_Value_F7", 0) + COALESCE(tx."Transactional_VEE_Value_F8", 0) +
                            COALESCE(tx."Transactional_VEE_Value_F9", 0) + COALESCE(tx."Transactional_VEE_Value_F10", 0)
                        ELSE 0
                    END), 0) AS "Surrender_VEE",
                    COALESCE(-SUM(CASE
                        WHEN tx."Description" = 'Termination' THEN
                            COALESCE(tx."Transactional_ER_Value_F1", 0) + COALESCE(tx."Transactional_ER_Value_F2", 0) +
                            COALESCE(tx."Transactional_ER_Value_F3", 0) + COALESCE(tx."Transactional_ER_Value_F4", 0) +
                            COALESCE(tx."Transactional_ER_Value_F5", 0) + COALESCE(tx."Transactional_ER_Value_F6", 0) +
                            COALESCE(tx."Transactional_ER_Value_F7", 0) + COALESCE(tx."Transactional_ER_Value_F8", 0) +
                            COALESCE(tx."Transactional_ER_Value_F9", 0) + COALESCE(tx."Transactional_ER_Value_F10", 0) +
                            COALESCE(tx."Terminated_ER_Value_F1", 0) + COALESCE(tx."Terminated_ER_Value_F2", 0) +
                            COALESCE(tx."Terminated_ER_Value_F3", 0) + COALESCE(tx."Terminated_ER_Value_F4", 0) +
                            COALESCE(tx."Terminated_ER_Value_F5", 0) + COALESCE(tx."Terminated_ER_Value_F6", 0) +
                            COALESCE(tx."Terminated_ER_Value_F7", 0) + COALESCE(tx."Terminated_ER_Value_F8", 0) +
                            COALESCE(tx."Terminated_ER_Value_F9", 0) + COALESCE(tx."Terminated_ER_Value_F10", 0)
                        ELSE 0
                    END), 0) AS "Surrender_ER",
                    COALESCE(SUM(tx."IMC_EE"), 0) AS "IMC_EE",
                    COALESCE(SUM(tx."IMC_VEE"), 0) AS "IMC_VEE",
                    COALESCE(SUM(tx."IMC_ER"), 0) AS "IMC_ER",
                    COALESCE(SUM(tx."IMC_Total"), 0) AS "IMC_Total",
                    COALESCE(SUM(tx."Admin_Charges_EE"), 0) AS "Admin_Charges_EE",
                    COALESCE(SUM(tx."Admin_Charges_ER"), 0) AS "Admin_Charges_ER",
                    COALESCE(SUM(tx."Admin_Charges_Total"), 0) AS "Admin_Charges_Total",
                    COALESCE(SUM(tx."Contribution_Charges_EE"), 0) AS "Contribution_Charges_EE",
                    COALESCE(SUM(tx."Contribution_Charges_VEE"), 0) AS "Contribution_Charges_VEE",
                    COALESCE(SUM(tx."Contribution_Charges_ER"), 0) AS "Contribution_Charges_ER",
                    COALESCE(SUM(tx."Contribution_Charges_Total"), 0) AS "Contribution_Charges_Total",
                    COALESCE(SUM(tx."Surrender_Charges_EE"), 0) AS "Surrender_Charges_EE",
                    COALESCE(SUM(tx."Surrender_Charges_VEE"), 0) AS "Surrender_Charges_VEE",
                    COALESCE(SUM(tx."Surrender_Charges_ER"), 0) AS "Surrender_Charges_ER",
                    COALESCE(SUM(tx."Surrender_Charges_Total"), 0) AS "Surrender_Charges_Total",
                    COALESCE(SUM(tx."Top_Up_Charges_EE"), 0) AS "Top_Up_Charges_EE",
                    COALESCE(SUM(tx."Top_Up_Charges_VEE"), 0) AS "Top_Up_Charges_VEE",
                    COALESCE(SUM(tx."Top_Up_Charges_ER"), 0) AS "Top_Up_Charges_ER",
                    COALESCE(SUM(tx."Top_Up_Charges_Total"), 0) AS "Top_Up_Charges_Total",
                    COALESCE(SUM(tx."Withdrawal_Charges_EE"), 0) AS "Withdrawal_Charges_EE",
                    COALESCE(SUM(tx."Withdrawal_Charges_VEE"), 0) AS "Withdrawal_Charges_VEE",
                    COALESCE(SUM(tx."Withdrawal_Charges_ER"), 0) AS "Withdrawal_Charges_ER",
                    COALESCE(SUM(tx."Withdrawal_Charges_Total"), 0) AS "Withdrawal_Charges_Total",
                    fc."Total_EE_Value_TM",
                    fc."Total_VEE_Value_TM",
                    fc."Total_ER_Value_TM",
                    fc."Total_Value_TM",
                    fc."Total_EE_Value_LM",
                    fc."Total_VEE_Value_LM",
                    fc."Total_ER_Value_LM",
                    fc."Total_Value_LM"
                FROM fund_comparison fc
                LEFT JOIN month_tx tx ON fc."Employee_Number" = tx."Employee_Number"
                GROUP BY
                    fc."Company_Number", fc."Employee_Number", fc."Employee_ID",
                    fc."Total_EE_Value_TM", fc."Total_VEE_Value_TM", fc."Total_ER_Value_TM", fc."Total_Value_TM",
                    fc."Total_EE_Value_LM", fc."Total_VEE_Value_LM", fc."Total_ER_Value_LM", fc."Total_Value_LM"
            )
            SELECT
                "Valuation_Date", "Company_Number", "Employee_Number", "Employee_ID",
                "Contribution_EE", "Contribution_VEE", "Contribution_ER", "Contribution_Total",
                "TopUp_EE", "TopUp_VEE", "TopUp_ER", "TopUp_Total",
                "Withdrawal_EE", "Withdrawal_VEE", "Withdrawal_ER", "Withdrawal_Total",
                "Surrender_EE", "Surrender_VEE", "Surrender_ER",
                "Surrender_EE" + "Surrender_VEE" + "Surrender_ER" AS "Surrender_Total",
                "IMC_EE", "IMC_VEE", "IMC_ER", "IMC_Total",
                "Admin_Charges_EE", "Admin_Charges_ER", "Admin_Charges_Total",
                "Contribution_Charges_EE", "Contribution_Charges_VEE", "Contribution_Charges_ER", "Contribution_Charges_Total",
                "Surrender_Charges_EE", "Surrender_Charges_VEE", "Surrender_Charges_ER", "Surrender_Charges_Total",
                "Top_Up_Charges_EE", "Top_Up_Charges_VEE", "Top_Up_Charges_ER", "Top_Up_Charges_Total",
                "Withdrawal_Charges_EE", "Withdrawal_Charges_VEE", "Withdrawal_Charges_ER", "Withdrawal_Charges_Total",
                "Total_EE_Value_TM" AS "Fund_Value_EE",
                "Total_VEE_Value_TM" AS "Fund_Value_VEE",
                "Total_ER_Value_TM" AS "Fund_Value_ER",
                "Total_Value_TM" AS "Fund_Value_Total",
                "Total_EE_Value_TM" - (COALESCE("Total_EE_Value_LM", 0) + "Contribution_EE" + "TopUp_EE" - "Withdrawal_EE" - "Surrender_EE" - "IMC_EE" - "Admin_Charges_EE" - "Contribution_Charges_EE" - "Surrender_Charges_EE" - "Top_Up_Charges_EE" - "Withdrawal_Charges_EE") AS "InvestmentReturn_EE",
                "Total_VEE_Value_TM" - (COALESCE("Total_VEE_Value_LM", 0) + "Contribution_VEE" + "TopUp_VEE" - "Withdrawal_VEE" - "Surrender_VEE" - "IMC_VEE" - "Contribution_Charges_VEE" - "Surrender_Charges_VEE" - "Top_Up_Charges_VEE" - "Withdrawal_Charges_VEE") AS "InvestmentReturn_VEE",
                "Total_ER_Value_TM" - (COALESCE("Total_ER_Value_LM", 0) + "Contribution_ER" + "TopUp_ER" - "Withdrawal_ER" - "Surrender_ER" - "IMC_ER" - "Admin_Charges_ER" - "Contribution_Charges_ER" - "Surrender_Charges_ER" - "Top_Up_Charges_ER" - "Withdrawal_Charges_ER") AS "InvestmentReturn_ER",
                "Total_Value_TM" - (COALESCE("Total_Value_LM", 0) + "Contribution_Total" + "TopUp_Total" - "Withdrawal_Total" - "Surrender_EE" - "Surrender_VEE" - "Surrender_ER" - "IMC_Total" - "Admin_Charges_Total" - "Contribution_Charges_Total" - "Surrender_Charges_Total" - "Top_Up_Charges_Total" - "Withdrawal_Charges_Total") AS "InvestmentReturn_Total"
            FROM temp_calc
            ORDER BY "Company_Number", "Employee_ID"
            """;
}
