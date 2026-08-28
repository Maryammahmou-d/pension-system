package com.rubix.pension.reports.sql;

/**
 * Access chain: Employee Extract Temporary → Employee Extract for App Temp → TempAppData.
 * Vesting years match Access DateDiff("yyyy", start, reportDate).
 * Vesting join matches Access Vesting Rules (all Vesting rows per company — can duplicate employees).
 */
public final class EmployeeExtractAppQueries {

    private EmployeeExtractAppQueries() {
    }

    private static String fundValueSum(String unitsAlias, String unitsPrefix, String priceAlias) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 10; i += 1) {
            if (i > 1) {
                sb.append(" + ");
            }
            sb.append("COALESCE(").append(priceAlias).append(".\"Fund").append(i)
                    .append("\", 0) * COALESCE(").append(unitsAlias).append(".\"")
                    .append(unitsPrefix).append("_F").append(i).append("\", 0)");
        }
        return sb.toString();
    }

    private static String fundValueAt(String unitsAlias, String unitsPrefix, String priceAlias, int fund) {
        return "COALESCE(" + priceAlias + ".\"Fund" + fund + "\", 0) * COALESCE("
                + unitsAlias + ".\"" + unitsPrefix + "_F" + fund + "\", 0)";
    }

    private static String annualizedGain(int fund) {
        return """
                CASE WHEN COALESCE(up_lm."Fund%d", 0) = 0 THEN 0
                     ELSE 100 * (POWER(COALESCE(up_tm."Fund%d", 0) / up_lm."Fund%d", 12) - 1)
                END
                """.formatted(fund, fund, fund).strip();
    }

    private static final String EE_VALUE = fundValueSum("hold", "Total_EE_Units", "up_tm");
    private static final String VEE_VALUE = fundValueSum("hold", "Total_VEE_Units", "up_tm");
    private static final String ER_VALUE = fundValueSum("hold", "Total_ER_Units", "up_tm");

    private static final String EE_F1 = fundValueAt("hold", "Total_EE_Units", "up_tm", 1);
    private static final String EE_F2 = fundValueAt("hold", "Total_EE_Units", "up_tm", 2);
    private static final String EE_F3 = fundValueAt("hold", "Total_EE_Units", "up_tm", 3);
    private static final String EE_F4 = fundValueAt("hold", "Total_EE_Units", "up_tm", 4);
    private static final String EE_F5 = fundValueAt("hold", "Total_EE_Units", "up_tm", 5);
    private static final String EE_F6 = fundValueAt("hold", "Total_EE_Units", "up_tm", 6);
    private static final String EE_F7 = fundValueAt("hold", "Total_EE_Units", "up_tm", 7);
    private static final String EE_F8 = fundValueAt("hold", "Total_EE_Units", "up_tm", 8);
    private static final String EE_F9 = fundValueAt("hold", "Total_EE_Units", "up_tm", 9);
    private static final String EE_F10 = fundValueAt("hold", "Total_EE_Units", "up_tm", 10);

    private static final String VEE_F1 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 1);
    private static final String VEE_F2 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 2);
    private static final String VEE_F3 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 3);
    private static final String VEE_F4 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 4);
    private static final String VEE_F5 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 5);
    private static final String VEE_F6 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 6);
    private static final String VEE_F7 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 7);
    private static final String VEE_F8 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 8);
    private static final String VEE_F9 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 9);
    private static final String VEE_F10 = fundValueAt("hold", "Total_VEE_Units", "up_tm", 10);

    private static final String ER_F1 = fundValueAt("hold", "Total_ER_Units", "up_tm", 1);
    private static final String ER_F2 = fundValueAt("hold", "Total_ER_Units", "up_tm", 2);
    private static final String ER_F3 = fundValueAt("hold", "Total_ER_Units", "up_tm", 3);
    private static final String ER_F4 = fundValueAt("hold", "Total_ER_Units", "up_tm", 4);
    private static final String ER_F5 = fundValueAt("hold", "Total_ER_Units", "up_tm", 5);
    private static final String ER_F6 = fundValueAt("hold", "Total_ER_Units", "up_tm", 6);
    private static final String ER_F7 = fundValueAt("hold", "Total_ER_Units", "up_tm", 7);
    private static final String ER_F8 = fundValueAt("hold", "Total_ER_Units", "up_tm", 8);
    private static final String ER_F9 = fundValueAt("hold", "Total_ER_Units", "up_tm", 9);
    private static final String ER_F10 = fundValueAt("hold", "Total_ER_Units", "up_tm", 10);

    /**
     * Final TempAppData columns for Access Employee Extract for App.
     * Params: reportDate, lastMonthDate.
     */
    public static final String TEMP_APP_DATA_SQL = """
            WITH tmp AS MATERIALIZED (
                SELECT * FROM get_employee_extract_temporary(CAST(:reportDate AS date))
            ),
            /* Inline DISTINCT ON — same as get_latest_transaction_records_asofdate, one scan. */
            hold AS MATERIALIZED (
                SELECT DISTINCT ON (t."Employee_Number")
                    t."Employee_Number",
                    t."Total_EE_Units_F1", t."Total_EE_Units_F2", t."Total_EE_Units_F3",
                    t."Total_EE_Units_F4", t."Total_EE_Units_F5", t."Total_EE_Units_F6",
                    t."Total_EE_Units_F7", t."Total_EE_Units_F8", t."Total_EE_Units_F9",
                    t."Total_EE_Units_F10",
                    t."Total_VEE_Units_F1", t."Total_VEE_Units_F2", t."Total_VEE_Units_F3",
                    t."Total_VEE_Units_F4", t."Total_VEE_Units_F5", t."Total_VEE_Units_F6",
                    t."Total_VEE_Units_F7", t."Total_VEE_Units_F8", t."Total_VEE_Units_F9",
                    t."Total_VEE_Units_F10",
                    t."Total_ER_Units_F1", t."Total_ER_Units_F2", t."Total_ER_Units_F3",
                    t."Total_ER_Units_F4", t."Total_ER_Units_F5", t."Total_ER_Units_F6",
                    t."Total_ER_Units_F7", t."Total_ER_Units_F8", t."Total_ER_Units_F9",
                    t."Total_ER_Units_F10"
                FROM "Transactions" t
                WHERE t."Payment_Date" <= CAST(:reportDate AS date)
                ORDER BY t."Employee_Number", t."Serial" DESC NULLS LAST
            ),
            up_tm AS MATERIALIZED (
                SELECT *
                FROM "UnitPrice"
                WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:reportDate AS date)
                ORDER BY "ID" DESC
                LIMIT 1
            ),
            up_lm AS MATERIALIZED (
                SELECT *
                FROM "UnitPrice"
                WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:lastMonthDate AS date)
                ORDER BY "ID" DESC
                LIMIT 1
            ),
            vesting AS MATERIALIZED (
                /* Access Vesting Rules joins all Vesting rows for the company (no latest-only). */
                SELECT
                    "Company_Number",
                    "Year1", "Year2", "Year3", "Year4", "Year5",
                    "Year6", "Year7", "Year8", "Year9", "Year10"
                FROM "Vesting"
            ),
            contrib AS MATERIALIZED (
                SELECT DISTINCT "Company_Number", "Category"
                FROM "Contributions"
            ),
            calc AS (
                SELECT
                    tmp."Company_Number" AS company_number,
                    tmp."Company_Name" AS company_name,
                    tmp."Employee_Number" AS employee_number,
                    tmp."Full_Name" AS full_name,
                    tmp."National_ID" AS national_id,
                    tmp."DOB" AS dob,
                    tmp."Gender" AS gender,
                    tmp."Occupation" AS occupation,
                    tmp."Pension_Start_Date" AS pension_start_date,
                    tmp."E-mail" AS email,
                    tmp."Termination_Date" AS termination_date,
                    tmp."ShowAvailableWithdrawal (Yes) / HideVesting (No)" AS show_available_withdrawal,
                    (up_tm."PriceDate" AT TIME ZONE 'Asia/Kuwait')::date AS price_date,
                    GREATEST(0,
                        EXTRACT(YEAR FROM CAST(:reportDate AS date))::int
                        - EXTRACT(YEAR FROM (
                            CASE WHEN COALESCE(lcr."Vesting_On_Hire", false)
                                 THEN tmp."Hire_Date"
                                 ELSE COALESCE(tmp."Pension_Start_Date", tmp."Kaf_Joining_Date", tmp."Hire_Date")
                            END
                        ))::int
                    ) AS vesting_years,
                    v."Year1", v."Year2", v."Year3", v."Year4", v."Year5",
                    v."Year6", v."Year7", v."Year8", v."Year9", v."Year10",
                    (%s) AS available_withdrawal_ee,
                    (%s) AS available_withdrawal_vee,
                    (%s) AS total_fund_value_ee,
                    (%s) AS total_fund_value_vee,
                    (%s) AS total_fund_value_er,
                    (%s) AS ee_f1, (%s) AS ee_f2, (%s) AS ee_f3, (%s) AS ee_f4, (%s) AS ee_f5,
                    (%s) AS ee_f6, (%s) AS ee_f7, (%s) AS ee_f8, (%s) AS ee_f9, (%s) AS ee_f10,
                    (%s) AS vee_f1, (%s) AS vee_f2, (%s) AS vee_f3, (%s) AS vee_f4, (%s) AS vee_f5,
                    (%s) AS vee_f6, (%s) AS vee_f7, (%s) AS vee_f8, (%s) AS vee_f9, (%s) AS vee_f10,
                    (%s) AS er_f1, (%s) AS er_f2, (%s) AS er_f3, (%s) AS er_f4, (%s) AS er_f5,
                    (%s) AS er_f6, (%s) AS er_f7, (%s) AS er_f8, (%s) AS er_f9, (%s) AS er_f10,
                    COALESCE(tmp."Paid_Contribution_EE", 0) + COALESCE(tmp."Starting_EE", 0) + COALESCE(tmp."TopUp_EE", 0)
                        AS gross_ee_contribution,
                    COALESCE(tmp."Paid_Contribution_VEE", 0) + COALESCE(tmp."Starting_VEE", 0) + COALESCE(tmp."TopUp_VEE", 0)
                        AS gross_vee_contribution,
                    COALESCE(tmp."Paid_Contribution_ER", 0) + COALESCE(tmp."Starting_ER", 0) + COALESCE(tmp."TopUp_ER", 0)
                        AS gross_er_contribution,
                    COALESCE(tmp."Admin_Charges_EE", 0) AS admin_charges_ee,
                    COALESCE(tmp."Contribution_Charges_EE", 0) AS contribution_charges_ee,
                    COALESCE(tmp."IMC_EE", 0) AS imc_ee,
                    COALESCE(tmp."Top_Up_Charges_EE", 0) AS top_up_charges_ee,
                    COALESCE(tmp."Contribution_Charges_VEE", 0) AS contribution_charges_vee,
                    COALESCE(tmp."IMC_VEE", 0) AS imc_vee,
                    COALESCE(tmp."Top_Up_Charges_VEE", 0) AS top_up_charges_vee,
                    COALESCE(tmp."Admin_Charges_ER", 0) AS admin_charges_er,
                    COALESCE(tmp."Contribution_Charges_ER", 0) AS contribution_charges_er,
                    COALESCE(tmp."IMC_ER", 0) AS imc_er,
                    COALESCE(tmp."Top_Up_Charges_ER", 0) AS top_up_charges_er,
                    tmp."Termination_Date" AS asof_termination_date,
                    (%s) AS fund1_gain,
                    (%s) AS fund2_gain,
                    (%s) AS fund3_gain,
                    (%s) AS fund4_gain,
                    (%s) AS fund5_gain,
                    (%s) AS fund6_gain,
                    (%s) AS fund7_gain,
                    (%s) AS fund8_gain,
                    (%s) AS fund9_gain,
                    (%s) AS fund10_gain
                FROM tmp
                INNER JOIN hold ON hold."Employee_Number" = tmp."Employee_Number"
                INNER JOIN vesting v ON v."Company_Number" = tmp."Company_Number"
                INNER JOIN contrib c
                    ON c."Company_Number" = tmp."Company_Number"
                   AND c."Category" IS NOT DISTINCT FROM tmp."Category"
                INNER JOIN "LatestCompanyRecords" lcr
                    ON lcr."Company_Number" = tmp."Company_Number"
                CROSS JOIN up_tm
                CROSS JOIN up_lm
            )
            SELECT
                company_number,
                company_name,
                employee_number,
                full_name,
                national_id,
                dob,
                gender,
                occupation,
                pension_start_date,
                email,
                termination_date,
                CASE WHEN COALESCE(show_available_withdrawal, false)
                     THEN available_withdrawal_ee + available_withdrawal_vee
                          + (
                                CASE
                                    WHEN vesting_years >= 10 THEN COALESCE("Year10", 0) / 100.0
                                    WHEN vesting_years = 9 THEN COALESCE("Year9", 0) / 100.0
                                    WHEN vesting_years = 8 THEN COALESCE("Year8", 0) / 100.0
                                    WHEN vesting_years = 7 THEN COALESCE("Year7", 0) / 100.0
                                    WHEN vesting_years = 6 THEN COALESCE("Year6", 0) / 100.0
                                    WHEN vesting_years = 5 THEN COALESCE("Year5", 0) / 100.0
                                    WHEN vesting_years = 4 THEN COALESCE("Year4", 0) / 100.0
                                    WHEN vesting_years = 3 THEN COALESCE("Year3", 0) / 100.0
                                    WHEN vesting_years = 2 THEN COALESCE("Year2", 0) / 100.0
                                    WHEN vesting_years = 1 THEN COALESCE("Year1", 0) / 100.0
                                    ELSE 0.0
                                END
                            ) * (er_f1+er_f2+er_f3+er_f4+er_f5+er_f6+er_f7+er_f8+er_f9+er_f10)
                     ELSE NULL
                END AS available_withdrawal,
                (total_fund_value_ee + total_fund_value_vee + total_fund_value_er) AS total_fund_value,
                gross_ee_contribution,
                gross_vee_contribution,
                gross_er_contribution,
                gross_ee_contribution - admin_charges_ee - contribution_charges_ee - imc_ee - top_up_charges_ee
                    AS net_ee_contribution,
                gross_vee_contribution - contribution_charges_vee - imc_vee - top_up_charges_vee
                    AS net_vee_contribution,
                gross_er_contribution - admin_charges_er - contribution_charges_er - imc_er - top_up_charges_er
                    AS net_er_contribution,
                CASE WHEN asof_termination_date IS NULL
                     THEN (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)
                          - gross_ee_contribution - gross_er_contribution - gross_vee_contribution
                     ELSE 0
                END AS gain_value,
                CASE WHEN (total_fund_value_ee + total_fund_value_vee + total_fund_value_er) = 0 THEN 0
                     ELSE (
                        ((ee_f1+er_f1+vee_f1) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund1_gain
                      + ((ee_f2+er_f2+vee_f2) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund2_gain
                      + ((ee_f3+er_f3+vee_f3) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund3_gain
                      + ((ee_f4+er_f4+vee_f4) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund4_gain
                      + ((ee_f5+er_f5+vee_f5) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund5_gain
                      + ((ee_f6+er_f6+vee_f6) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund6_gain
                      + ((ee_f7+er_f7+vee_f7) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund7_gain
                      + ((ee_f8+er_f8+vee_f8) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund8_gain
                      + ((ee_f9+er_f9+vee_f9) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund9_gain
                      + ((ee_f10+er_f10+vee_f10) / (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)) * fund10_gain
                     )
                END AS percentage_gain_unit_price,
                CASE WHEN (gross_ee_contribution + gross_er_contribution + gross_vee_contribution) = 0 THEN NULL
                     ELSE 100.0 * (
                        CASE WHEN asof_termination_date IS NULL
                             THEN (total_fund_value_ee + total_fund_value_vee + total_fund_value_er)
                                  - gross_ee_contribution - gross_er_contribution - gross_vee_contribution
                             ELSE 0
                        END
                     ) / (gross_ee_contribution + gross_er_contribution + gross_vee_contribution)
                END AS percentage_gain_paid,
                price_date
            FROM calc
            ORDER BY company_number, employee_number
            """.formatted(
            EE_VALUE, VEE_VALUE,
            EE_VALUE, VEE_VALUE, ER_VALUE,
            EE_F1, EE_F2, EE_F3, EE_F4, EE_F5, EE_F6, EE_F7, EE_F8, EE_F9, EE_F10,
            VEE_F1, VEE_F2, VEE_F3, VEE_F4, VEE_F5, VEE_F6, VEE_F7, VEE_F8, VEE_F9, VEE_F10,
            ER_F1, ER_F2, ER_F3, ER_F4, ER_F5, ER_F6, ER_F7, ER_F8, ER_F9, ER_F10,
            annualizedGain(1), annualizedGain(2), annualizedGain(3), annualizedGain(4), annualizedGain(5),
            annualizedGain(6), annualizedGain(7), annualizedGain(8), annualizedGain(9), annualizedGain(10)
    );
}
