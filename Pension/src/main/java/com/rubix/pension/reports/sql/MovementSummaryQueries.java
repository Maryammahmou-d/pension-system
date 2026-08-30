package com.rubix.pension.reports.sql;

public final class MovementSummaryQueries {

    private MovementSummaryQueries() {
    }

    public static final String CHECK_UNIT_PRICE_SQL = """
            SELECT COUNT(*)
            FROM "UnitPrice"
            WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:valuationDate AS date)
            """;

    public static final String GET_UNIT_PRICE_SQL = """
            SELECT "PriceDate",
                   COALESCE("Fund1", 0) AS "Fund1",
                   COALESCE("Fund2", 0) AS "Fund2",
                   COALESCE("Fund3", 0) AS "Fund3",
                   COALESCE("Fund4", 0) AS "Fund4",
                   COALESCE("Fund5", 0) AS "Fund5",
                   COALESCE("Fund6", 0) AS "Fund6",
                   COALESCE("Fund7", 0) AS "Fund7",
                   COALESCE("Fund8", 0) AS "Fund8",
                   COALESCE("Fund9", 0) AS "Fund9",
                   COALESCE("Fund10", 0) AS "Fund10"
            FROM "UnitPrice"
            WHERE ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = CAST(:priceDate AS date)
            ORDER BY "PriceDate" DESC
            LIMIT 1
            """;

    public static final String DAILY_MOVEMENT_SUMMARY_SQL = """
            SELECT
                t."Modified_Date",
                t."Payment_Date",
                t."Description",
                t."UP1", t."UP2", t."UP3", t."UP4", t."UP5",
                t."UP6", t."UP7", t."UP8", t."UP9", t."UP10",
                SUM(t."Contribution_EE") AS "SumOfContribution_EE",
                SUM(t."Contribution_VEE") AS "SumOfContribution_VEE",
                SUM(t."Contribution_ER") AS "SumOfContribution_ER",
                SUM(COALESCE(t."Contribution_EE",0) + COALESCE(t."Contribution_VEE",0) + COALESCE(t."Contribution_ER",0)) AS "Contribution_Total",
                SUM(-COALESCE(t."Withdrawal_EE",0)) AS "WithdrawalEE",
                SUM(-COALESCE(t."Withdrawal_VEE",0)) AS "WithdrawalVEE",
                SUM(-COALESCE(t."Withdrawal_ER",0)) AS "WithdrawalER",
                SUM(-COALESCE(t."Withdrawal_Total",0)) AS "WithdrawalTotal",
                SUM(t."TopUp_EE") AS "SumOfTopUp_EE",
                SUM(t."TopUp_VEE") AS "SumOfTopUp_VEE",
                SUM(t."TopUp_ER") AS "SumOfTopUp_ER",
                SUM(COALESCE(t."TopUp_EE",0) + COALESCE(t."TopUp_VEE",0) + COALESCE(t."TopUp_ER",0)) AS "TopUp_Total",
                SUM(-COALESCE(t."IMC_EE",0)) AS "IMCEE",
                SUM(-COALESCE(t."IMC_VEE",0)) AS "IMCVEE",
                SUM(-COALESCE(t."IMC_ER",0)) AS "IMCER",
                -SUM(COALESCE(t."IMC_EE",0) + COALESCE(t."IMC_VEE",0) + COALESCE(t."IMC_ER",0)) AS "IMC_Total",
                SUM(-COALESCE(t."Admin_Charges_EE",0)) AS "AdminCharges_EE",
                SUM(-COALESCE(t."Admin_Charges_ER",0)) AS "AdminCharges_ER",
                -SUM(COALESCE(t."Admin_Charges_EE",0) + COALESCE(t."Admin_Charges_ER",0)) AS "AdminCharges_Total",
                SUM(-COALESCE(t."Contribution_Charges_EE",0)) AS "ContributionCharges_EE",
                SUM(-COALESCE(t."Contribution_Charges_VEE",0)) AS "ContributionCharges_VEE",
                SUM(-COALESCE(t."Contribution_Charges_ER",0)) AS "ContributionCharges_ER",
                -SUM(COALESCE(t."Contribution_Charges_EE",0) + COALESCE(t."Contribution_Charges_VEE",0) + COALESCE(t."Contribution_Charges_ER",0)) AS "ContributionCharges_Total",
                SUM(-COALESCE(t."Surrender_Charges_EE",0)) AS "SurrenderCharges_EE",
                SUM(-COALESCE(t."Surrender_Charges_VEE",0)) AS "SurrenderCharges_VEE",
                SUM(-COALESCE(t."Surrender_Charges_ER",0)) AS "SurrenderCharges_ER",
                -SUM(COALESCE(t."Surrender_Charges_EE",0) + COALESCE(t."Surrender_Charges_VEE",0) + COALESCE(t."Surrender_Charges_ER",0)) AS "SurrenderCharges_Total",
                SUM(-COALESCE(t."Top_Up_Charges_EE",0)) AS "TopUp_Charges_EE",
                SUM(-COALESCE(t."Top_Up_Charges_VEE",0)) AS "TopUp_Charges_VEE",
                SUM(-COALESCE(t."Top_Up_Charges_ER",0)) AS "TopUp_Charges_ER",
                -SUM(COALESCE(t."Top_Up_Charges_EE",0) + COALESCE(t."Top_Up_Charges_VEE",0) + COALESCE(t."Top_Up_Charges_ER",0)) AS "TopUp_Charges_Total",
                SUM(-COALESCE(t."Withdrawal_Charges_EE",0)) AS "WithdrawalCharges_EE",
                SUM(-COALESCE(t."Withdrawal_Charges_VEE",0)) AS "WithdrawalCharges_VEE",
                SUM(-COALESCE(t."Withdrawal_Charges_ER",0)) AS "WithdrawalCharges_ER",
                -SUM(COALESCE(t."Withdrawal_Charges_EE",0) + COALESCE(t."Withdrawal_Charges_VEE",0) + COALESCE(t."Withdrawal_Charges_ER",0)) AS "WithdrawalCharges_Total",
                
                SUM(COALESCE(t."Transactional_EE_Value_F1",0)) AS "SumOfTransactional_EE_Value_F1",
                SUM(COALESCE(t."Transactional_EE_Value_F2",0)) AS "SumOfTransactional_EE_Value_F2",
                SUM(COALESCE(t."Transactional_EE_Value_F3",0)) AS "SumOfTransactional_EE_Value_F3",
                SUM(COALESCE(t."Transactional_EE_Value_F4",0)) AS "SumOfTransactional_EE_Value_F4",
                SUM(COALESCE(t."Transactional_EE_Value_F5",0)) AS "SumOfTransactional_EE_Value_F5",
                SUM(COALESCE(t."Transactional_EE_Value_F6",0)) AS "SumOfTransactional_EE_Value_F6",
                SUM(COALESCE(t."Transactional_EE_Value_F7",0)) AS "SumOfTransactional_EE_Value_F7",
                SUM(COALESCE(t."Transactional_EE_Value_F8",0)) AS "SumOfTransactional_EE_Value_F8",
                SUM(COALESCE(t."Transactional_EE_Value_F9",0)) AS "SumOfTransactional_EE_Value_F9",
                SUM(COALESCE(t."Transactional_EE_Value_F10",0)) AS "SumOfTransactional_EE_Value_F10",
                
                SUM(COALESCE(t."Transactional_VEE_Value_F1",0)) AS "SumOfTransactional_VEE_Value_F1",
                SUM(COALESCE(t."Transactional_VEE_Value_F2",0)) AS "SumOfTransactional_VEE_Value_F2",
                SUM(COALESCE(t."Transactional_VEE_Value_F3",0)) AS "SumOfTransactional_VEE_Value_F3",
                SUM(COALESCE(t."Transactional_VEE_Value_F4",0)) AS "SumOfTransactional_VEE_Value_F4",
                SUM(COALESCE(t."Transactional_VEE_Value_F5",0)) AS "SumOfTransactional_VEE_Value_F5",
                SUM(COALESCE(t."Transactional_VEE_Value_F6",0)) AS "SumOfTransactional_VEE_Value_F6",
                SUM(COALESCE(t."Transactional_VEE_Value_F7",0)) AS "SumOfTransactional_VEE_Value_F7",
                SUM(COALESCE(t."Transactional_VEE_Value_F8",0)) AS "SumOfTransactional_VEE_Value_F8",
                SUM(COALESCE(t."Transactional_VEE_Value_F9",0)) AS "SumOfTransactional_VEE_Value_F9",
                SUM(COALESCE(t."Transactional_VEE_Value_F10",0)) AS "SumOfTransactional_VEE_Value_F10",
                
                SUM(COALESCE(t."Transactional_ER_Value_F1",0)) AS "SumOfTransactional_ER_Value_F1",
                SUM(COALESCE(t."Transactional_ER_Value_F2",0)) AS "SumOfTransactional_ER_Value_F2",
                SUM(COALESCE(t."Transactional_ER_Value_F3",0)) AS "SumOfTransactional_ER_Value_F3",
                SUM(COALESCE(t."Transactional_ER_Value_F4",0)) AS "SumOfTransactional_ER_Value_F4",
                SUM(COALESCE(t."Transactional_ER_Value_F5",0)) AS "SumOfTransactional_ER_Value_F5",
                SUM(COALESCE(t."Transactional_ER_Value_F6",0)) AS "SumOfTransactional_ER_Value_F6",
                SUM(COALESCE(t."Transactional_ER_Value_F7",0)) AS "SumOfTransactional_ER_Value_F7",
                SUM(COALESCE(t."Transactional_ER_Value_F8",0)) AS "SumOfTransactional_ER_Value_F8",
                SUM(COALESCE(t."Transactional_ER_Value_F9",0)) AS "SumOfTransactional_ER_Value_F9",
                SUM(COALESCE(t."Transactional_ER_Value_F10",0)) AS "SumOfTransactional_ER_Value_F10",

                SUM(COALESCE(t."Terminated_ER_Value_F1",0)) AS "SumOfTerminated_ER_Value_F1",
                SUM(COALESCE(t."Terminated_ER_Value_F2",0)) AS "SumOfTerminated_ER_Value_F2",
                SUM(COALESCE(t."Terminated_ER_Value_F3",0)) AS "SumOfTerminated_ER_Value_F3",
                SUM(COALESCE(t."Terminated_ER_Value_F4",0)) AS "SumOfTerminated_ER_Value_F4",
                SUM(COALESCE(t."Terminated_ER_Value_F5",0)) AS "SumOfTerminated_ER_Value_F5",
                SUM(COALESCE(t."Terminated_ER_Value_F6",0)) AS "SumOfTerminated_ER_Value_F6",
                SUM(COALESCE(t."Terminated_ER_Value_F7",0)) AS "SumOfTerminated_ER_Value_F7",
                SUM(COALESCE(t."Terminated_ER_Value_F8",0)) AS "SumOfTerminated_ER_Value_F8",
                SUM(COALESCE(t."Terminated_ER_Value_F9",0)) AS "SumOfTerminated_ER_Value_F9",
                SUM(COALESCE(t."Terminated_ER_Value_F10",0)) AS "SumOfTerminated_ER_Value_F10",

                SUM(COALESCE(t."Transactional_EE_Units_F1",0)) AS "SumOfTransactional_EE_Units_F1",
                SUM(COALESCE(t."Transactional_EE_Units_F2",0)) AS "SumOfTransactional_EE_Units_F2",
                SUM(COALESCE(t."Transactional_EE_Units_F3",0)) AS "SumOfTransactional_EE_Units_F3",
                SUM(COALESCE(t."Transactional_EE_Units_F4",0)) AS "SumOfTransactional_EE_Units_F4",
                SUM(COALESCE(t."Transactional_EE_Units_F5",0)) AS "SumOfTransactional_EE_Units_F5",
                SUM(COALESCE(t."Transactional_EE_Units_F6",0)) AS "SumOfTransactional_EE_Units_F6",
                SUM(COALESCE(t."Transactional_EE_Units_F7",0)) AS "SumOfTransactional_EE_Units_F7",
                SUM(COALESCE(t."Transactional_EE_Units_F8",0)) AS "SumOfTransactional_EE_Units_F8",
                SUM(COALESCE(t."Transactional_EE_Units_F9",0)) AS "SumOfTransactional_EE_Units_F9",
                SUM(COALESCE(t."Transactional_EE_Units_F10",0)) AS "SumOfTransactional_EE_Units_F10",

                SUM(COALESCE(t."Transactional_VEE_Units_F1",0)) AS "SumOfTransactional_VEE_Units_F1",
                SUM(COALESCE(t."Transactional_VEE_Units_F2",0)) AS "SumOfTransactional_VEE_Units_F2",
                SUM(COALESCE(t."Transactional_VEE_Units_F3",0)) AS "SumOfTransactional_VEE_Units_F3",
                SUM(COALESCE(t."Transactional_VEE_Units_F4",0)) AS "SumOfTransactional_VEE_Units_F4",
                SUM(COALESCE(t."Transactional_VEE_Units_F5",0)) AS "SumOfTransactional_VEE_Units_F5",
                SUM(COALESCE(t."Transactional_VEE_Units_F6",0)) AS "SumOfTransactional_VEE_Units_F6",
                SUM(COALESCE(t."Transactional_VEE_Units_F7",0)) AS "SumOfTransactional_VEE_Units_F7",
                SUM(COALESCE(t."Transactional_VEE_Units_F8",0)) AS "SumOfTransactional_VEE_Units_F8",
                SUM(COALESCE(t."Transactional_VEE_Units_F9",0)) AS "SumOfTransactional_VEE_Units_F9",
                SUM(COALESCE(t."Transactional_VEE_Units_F10",0)) AS "SumOfTransactional_VEE_Units_F10",

                SUM(COALESCE(t."Transactional_ER_Units_F1",0)) AS "SumOfTransactional_ER_Units_F1",
                SUM(COALESCE(t."Transactional_ER_Units_F2",0)) AS "SumOfTransactional_ER_Units_F2",
                SUM(COALESCE(t."Transactional_ER_Units_F3",0)) AS "SumOfTransactional_ER_Units_F3",
                SUM(COALESCE(t."Transactional_ER_Units_F4",0)) AS "SumOfTransactional_ER_Units_F4",
                SUM(COALESCE(t."Transactional_ER_Units_F5",0)) AS "SumOfTransactional_ER_Units_F5",
                SUM(COALESCE(t."Transactional_ER_Units_F6",0)) AS "SumOfTransactional_ER_Units_F6",
                SUM(COALESCE(t."Transactional_ER_Units_F7",0)) AS "SumOfTransactional_ER_Units_F7",
                SUM(COALESCE(t."Transactional_ER_Units_F8",0)) AS "SumOfTransactional_ER_Units_F8",
                SUM(COALESCE(t."Transactional_ER_Units_F9",0)) AS "SumOfTransactional_ER_Units_F9",
                SUM(COALESCE(t."Transactional_ER_Units_F10",0)) AS "SumOfTransactional_ER_Units_F10",

                SUM(COALESCE(t."Terminated_ER_Units_F1",0)) AS "SumOfTerminated_ER_Units_F1",
                SUM(COALESCE(t."Terminated_ER_Units_F2",0)) AS "SumOfTerminated_ER_Units_F2",
                SUM(COALESCE(t."Terminated_ER_Units_F3",0)) AS "SumOfTerminated_ER_Units_F3",
                SUM(COALESCE(t."Terminated_ER_Units_F4",0)) AS "SumOfTerminated_ER_Units_F4",
                SUM(COALESCE(t."Terminated_ER_Units_F5",0)) AS "SumOfTerminated_ER_Units_F5",
                SUM(COALESCE(t."Terminated_ER_Units_F6",0)) AS "SumOfTerminated_ER_Units_F6",
                SUM(COALESCE(t."Terminated_ER_Units_F7",0)) AS "SumOfTerminated_ER_Units_F7",
                SUM(COALESCE(t."Terminated_ER_Units_F8",0)) AS "SumOfTerminated_ER_Units_F8",
                SUM(COALESCE(t."Terminated_ER_Units_F9",0)) AS "SumOfTerminated_ER_Units_F9",
                SUM(COALESCE(t."Terminated_ER_Units_F10",0)) AS "SumOfTerminated_ER_Units_F10",

                SUM(COALESCE(t."Total_EE_Units_F1",0)) AS "SumOfTotal_EE_Units_F1",
                SUM(COALESCE(t."Total_EE_Units_F2",0)) AS "SumOfTotal_EE_Units_F2",
                SUM(COALESCE(t."Total_EE_Units_F3",0)) AS "SumOfTotal_EE_Units_F3",
                SUM(COALESCE(t."Total_EE_Units_F4",0)) AS "SumOfTotal_EE_Units_F4",
                SUM(COALESCE(t."Total_EE_Units_F5",0)) AS "SumOfTotal_EE_Units_F5",
                SUM(COALESCE(t."Total_EE_Units_F6",0)) AS "SumOfTotal_EE_Units_F6",
                SUM(COALESCE(t."Total_EE_Units_F7",0)) AS "SumOfTotal_EE_Units_F7",
                SUM(COALESCE(t."Total_EE_Units_F8",0)) AS "SumOfTotal_EE_Units_F8",
                SUM(COALESCE(t."Total_EE_Units_F9",0)) AS "SumOfTotal_EE_Units_F9",
                SUM(COALESCE(t."Total_EE_Units_F10",0)) AS "SumOfTotal_EE_Units_F10",

                SUM(COALESCE(t."Total_VEE_Units_F1",0)) AS "SumOfTotal_VEE_Units_F1",
                SUM(COALESCE(t."Total_VEE_Units_F2",0)) AS "SumOfTotal_VEE_Units_F2",
                SUM(COALESCE(t."Total_VEE_Units_F3",0)) AS "SumOfTotal_VEE_Units_F3",
                SUM(COALESCE(t."Total_VEE_Units_F4",0)) AS "SumOfTotal_VEE_Units_F4",
                SUM(COALESCE(t."Total_VEE_Units_F5",0)) AS "SumOfTotal_VEE_Units_F5",
                SUM(COALESCE(t."Total_VEE_Units_F6",0)) AS "SumOfTotal_VEE_Units_F6",
                SUM(COALESCE(t."Total_VEE_Units_F7",0)) AS "SumOfTotal_VEE_Units_F7",
                SUM(COALESCE(t."Total_VEE_Units_F8",0)) AS "SumOfTotal_VEE_Units_F8",
                SUM(COALESCE(t."Total_VEE_Units_F9",0)) AS "SumOfTotal_VEE_Units_F9",
                SUM(COALESCE(t."Total_VEE_Units_F10",0)) AS "SumOfTotal_VEE_Units_F10",

                SUM(COALESCE(t."Total_ER_Units_F1",0)) AS "SumOfTotal_ER_Units_F1",
                SUM(COALESCE(t."Total_ER_Units_F2",0)) AS "SumOfTotal_ER_Units_F2",
                SUM(COALESCE(t."Total_ER_Units_F3",0)) AS "SumOfTotal_ER_Units_F3",
                SUM(COALESCE(t."Total_ER_Units_F4",0)) AS "SumOfTotal_ER_Units_F4",
                SUM(COALESCE(t."Total_ER_Units_F5",0)) AS "SumOfTotal_ER_Units_F5",
                SUM(COALESCE(t."Total_ER_Units_F6",0)) AS "SumOfTotal_ER_Units_F6",
                SUM(COALESCE(t."Total_ER_Units_F7",0)) AS "SumOfTotal_ER_Units_F7",
                SUM(COALESCE(t."Total_ER_Units_F8",0)) AS "SumOfTotal_ER_Units_F8",
                SUM(COALESCE(t."Total_ER_Units_F9",0)) AS "SumOfTotal_ER_Units_F9",
                SUM(COALESCE(t."Total_ER_Units_F10",0)) AS "SumOfTotal_ER_Units_F10",

                SUM(COALESCE(t."Total_Units_F1",0)) AS "SumOfTotal_Units_F1",
                SUM(COALESCE(t."Total_Units_F2",0)) AS "SumOfTotal_Units_F2",
                SUM(COALESCE(t."Total_Units_F3",0)) AS "SumOfTotal_Units_F3",
                SUM(COALESCE(t."Total_Units_F4",0)) AS "SumOfTotal_Units_F4",
                SUM(COALESCE(t."Total_Units_F5",0)) AS "SumOfTotal_Units_F5",
                SUM(COALESCE(t."Total_Units_F6",0)) AS "SumOfTotal_Units_F6",
                SUM(COALESCE(t."Total_Units_F7",0)) AS "SumOfTotal_Units_F7",
                SUM(COALESCE(t."Total_Units_F8",0)) AS "SumOfTotal_Units_F8",
                SUM(COALESCE(t."Total_Units_F9",0)) AS "SumOfTotal_Units_F9",
                SUM(COALESCE(t."Total_Units_F10",0)) AS "SumOfTotal_Units_F10",

                SUM(CASE WHEN t."Description" = 'Termination' THEN 
                    COALESCE(t."Transactional_EE_Value_F1",0) + COALESCE(t."Transactional_EE_Value_F2",0) + COALESCE(t."Transactional_EE_Value_F3",0) + 
                    COALESCE(t."Transactional_EE_Value_F4",0) + COALESCE(t."Transactional_EE_Value_F5",0) + COALESCE(t."Transactional_EE_Value_F6",0) + 
                    COALESCE(t."Transactional_EE_Value_F7",0) + COALESCE(t."Transactional_EE_Value_F8",0) + COALESCE(t."Transactional_EE_Value_F9",0) + 
                    COALESCE(t."Transactional_EE_Value_F10",0) ELSE 0 END) AS "Termination_EE",
                SUM(CASE WHEN t."Description" = 'Termination' THEN 
                    COALESCE(t."Transactional_VEE_Value_F1",0) + COALESCE(t."Transactional_VEE_Value_F2",0) + COALESCE(t."Transactional_VEE_Value_F3",0) + 
                    COALESCE(t."Transactional_VEE_Value_F4",0) + COALESCE(t."Transactional_VEE_Value_F5",0) + COALESCE(t."Transactional_VEE_Value_F6",0) + 
                    COALESCE(t."Transactional_VEE_Value_F7",0) + COALESCE(t."Transactional_VEE_Value_F8",0) + COALESCE(t."Transactional_VEE_Value_F9",0) + 
                    COALESCE(t."Transactional_VEE_Value_F10",0) ELSE 0 END) AS "Termination_VEE",
                SUM(CASE WHEN t."Description" = 'Termination' THEN 
                    COALESCE(t."Transactional_ER_Value_F1",0) + COALESCE(t."Transactional_ER_Value_F2",0) + COALESCE(t."Transactional_ER_Value_F3",0) + 
                    COALESCE(t."Transactional_ER_Value_F4",0) + COALESCE(t."Transactional_ER_Value_F5",0) + COALESCE(t."Transactional_ER_Value_F6",0) + 
                    COALESCE(t."Transactional_ER_Value_F7",0) + COALESCE(t."Transactional_ER_Value_F8",0) + COALESCE(t."Transactional_ER_Value_F9",0) + 
                    COALESCE(t."Transactional_ER_Value_F10",0) + COALESCE(t."Terminated_ER_Value_F1",0) + COALESCE(t."Terminated_ER_Value_F2",0) + 
                    COALESCE(t."Terminated_ER_Value_F3",0) + COALESCE(t."Terminated_ER_Value_F4",0) + COALESCE(t."Terminated_ER_Value_F5",0) + 
                    COALESCE(t."Terminated_ER_Value_F6",0) + COALESCE(t."Terminated_ER_Value_F7",0) + COALESCE(t."Terminated_ER_Value_F8",0) + 
                    COALESCE(t."Terminated_ER_Value_F9",0) + COALESCE(t."Terminated_ER_Value_F10",0) ELSE 0 END) AS "Termination_ER",
                (SUM(CASE WHEN t."Description" = 'Termination' THEN 
                    COALESCE(t."Transactional_EE_Value_F1",0) + COALESCE(t."Transactional_EE_Value_F2",0) + COALESCE(t."Transactional_EE_Value_F3",0) + 
                    COALESCE(t."Transactional_EE_Value_F4",0) + COALESCE(t."Transactional_EE_Value_F5",0) + COALESCE(t."Transactional_EE_Value_F6",0) + 
                    COALESCE(t."Transactional_EE_Value_F7",0) + COALESCE(t."Transactional_EE_Value_F8",0) + COALESCE(t."Transactional_EE_Value_F9",0) + 
                    COALESCE(t."Transactional_EE_Value_F10",0) ELSE 0 END) +
                 SUM(CASE WHEN t."Description" = 'Termination' THEN 
                    COALESCE(t."Transactional_VEE_Value_F1",0) + COALESCE(t."Transactional_VEE_Value_F2",0) + COALESCE(t."Transactional_VEE_Value_F3",0) + 
                    COALESCE(t."Transactional_VEE_Value_F4",0) + COALESCE(t."Transactional_VEE_Value_F5",0) + COALESCE(t."Transactional_VEE_Value_F6",0) + 
                    COALESCE(t."Transactional_VEE_Value_F7",0) + COALESCE(t."Transactional_VEE_Value_F8",0) + COALESCE(t."Transactional_VEE_Value_F9",0) + 
                    COALESCE(t."Transactional_VEE_Value_F10",0) ELSE 0 END) +
                 SUM(CASE WHEN t."Description" = 'Termination' THEN 
                    COALESCE(t."Transactional_ER_Value_F1",0) + COALESCE(t."Transactional_ER_Value_F2",0) + COALESCE(t."Transactional_ER_Value_F3",0) + 
                    COALESCE(t."Transactional_ER_Value_F4",0) + COALESCE(t."Transactional_ER_Value_F5",0) + COALESCE(t."Transactional_ER_Value_F6",0) + 
                    COALESCE(t."Transactional_ER_Value_F7",0) + COALESCE(t."Transactional_ER_Value_F8",0) + COALESCE(t."Transactional_ER_Value_F9",0) + 
                    COALESCE(t."Transactional_ER_Value_F10",0) + COALESCE(t."Terminated_ER_Value_F1",0) + COALESCE(t."Terminated_ER_Value_F2",0) + 
                    COALESCE(t."Terminated_ER_Value_F3",0) + COALESCE(t."Terminated_ER_Value_F4",0) + COALESCE(t."Terminated_ER_Value_F5",0) + 
                    COALESCE(t."Terminated_ER_Value_F6",0) + COALESCE(t."Terminated_ER_Value_F7",0) + COALESCE(t."Terminated_ER_Value_F8",0) + 
                    COALESCE(t."Terminated_ER_Value_F9",0) + COALESCE(t."Terminated_ER_Value_F10",0) ELSE 0 END)) AS "Termination_Total",

                SUM(COALESCE(t."Transactional_Total_Units_F1",0)) AS "SumOfTransactional_Total_Units_F1",
                SUM(COALESCE(t."Transactional_Total_Units_F2",0)) AS "SumOfTransactional_Total_Units_F2",
                SUM(COALESCE(t."Transactional_Total_Units_F3",0)) AS "SumOfTransactional_Total_Units_F3",
                SUM(COALESCE(t."Transactional_Total_Units_F4",0)) AS "SumOfTransactional_Total_Units_F4",
                SUM(COALESCE(t."Transactional_Total_Units_F5",0)) AS "SumOfTransactional_Total_Units_F5",
                SUM(COALESCE(t."Transactional_Total_Units_F6",0)) AS "SumOfTransactional_Total_Units_F6",
                SUM(COALESCE(t."Transactional_Total_Units_F7",0)) AS "SumOfTransactional_Total_Units_F7",
                SUM(COALESCE(t."Transactional_Total_Units_F8",0)) AS "SumOfTransactional_Total_Units_F8",
                SUM(COALESCE(t."Transactional_Total_Units_F9",0)) AS "SumOfTransactional_Total_Units_F9",
                SUM(COALESCE(t."Transactional_Total_Units_F10",0)) AS "SumOfTransactional_Total_Units_F10",

                SUM(COALESCE(t."Transactional_Total_Value_F1",0)) AS "SumOfTransactional_Total_Value_F1",
                SUM(COALESCE(t."Transactional_Total_Value_F2",0)) AS "SumOfTransactional_Total_Value_F2",
                SUM(COALESCE(t."Transactional_Total_Value_F3",0)) AS "SumOfTransactional_Total_Value_F3",
                SUM(COALESCE(t."Transactional_Total_Value_F4",0)) AS "SumOfTransactional_Total_Value_F4",
                SUM(COALESCE(t."Transactional_Total_Value_F5",0)) AS "SumOfTransactional_Total_Value_F5",
                SUM(COALESCE(t."Transactional_Total_Value_F6",0)) AS "SumOfTransactional_Total_Value_F6",
                SUM(COALESCE(t."Transactional_Total_Value_F7",0)) AS "SumOfTransactional_Total_Value_F7",
                SUM(COALESCE(t."Transactional_Total_Value_F8",0)) AS "SumOfTransactional_Total_Value_F8",
                SUM(COALESCE(t."Transactional_Total_Value_F9",0)) AS "SumOfTransactional_Total_Value_F9",
                SUM(COALESCE(t."Transactional_Total_Value_F10",0)) AS "SumOfTransactional_Total_Value_F10"

            FROM "Transactions" t
            WHERE t."Modified_Date" = CAST(:targetDate AS date)
            GROUP BY 
                t."Modified_Date", t."Payment_Date", t."Description",
                t."UP1", t."UP2", t."UP3", t."UP4", t."UP5",
                t."UP6", t."UP7", t."UP8", t."UP9", t."UP10"
            """;

    public static final String SUMMARY_MOVEMENTS_BETWEEN_DATES_SQL = """
            SELECT
                t."Modified_Date" AS "modified_date",
                t."Payment_Date" AS "payment_date",
                t."UP1" AS "up1", t."UP2" AS "up2", t."UP3" AS "up3", t."UP4" AS "up4", t."UP5" AS "up5",
                t."UP6" AS "up6", t."UP7" AS "up7", t."UP8" AS "up8", t."UP9" AS "up9", t."UP10" AS "up10",
                
                SUM(COALESCE(t."Transactional_EE_Units_F1",0)) AS "sum_tx_ee_units_f1",
                SUM(COALESCE(t."Transactional_EE_Units_F2",0)) AS "sum_tx_ee_units_f2",
                SUM(COALESCE(t."Transactional_EE_Units_F3",0)) AS "sum_tx_ee_units_f3",
                SUM(COALESCE(t."Transactional_EE_Units_F4",0)) AS "sum_tx_ee_units_f4",
                SUM(COALESCE(t."Transactional_EE_Units_F5",0)) AS "sum_tx_ee_units_f5",
                SUM(COALESCE(t."Transactional_EE_Units_F6",0)) AS "sum_tx_ee_units_f6",
                SUM(COALESCE(t."Transactional_EE_Units_F7",0)) AS "sum_tx_ee_units_f7",
                SUM(COALESCE(t."Transactional_EE_Units_F8",0)) AS "sum_tx_ee_units_f8",
                SUM(COALESCE(t."Transactional_EE_Units_F9",0)) AS "sum_tx_ee_units_f9",
                SUM(COALESCE(t."Transactional_EE_Units_F10",0)) AS "sum_tx_ee_units_f10",

                SUM(COALESCE(t."Transactional_VEE_Units_F1",0)) AS "sum_tx_vee_units_f1",
                SUM(COALESCE(t."Transactional_VEE_Units_F2",0)) AS "sum_tx_vee_units_f2",
                SUM(COALESCE(t."Transactional_VEE_Units_F3",0)) AS "sum_tx_vee_units_f3",
                SUM(COALESCE(t."Transactional_VEE_Units_F4",0)) AS "sum_tx_vee_units_f4",
                SUM(COALESCE(t."Transactional_VEE_Units_F5",0)) AS "sum_tx_vee_units_f5",
                SUM(COALESCE(t."Transactional_VEE_Units_F6",0)) AS "sum_tx_vee_units_f6",
                SUM(COALESCE(t."Transactional_VEE_Units_F7",0)) AS "sum_tx_vee_units_f7",
                SUM(COALESCE(t."Transactional_VEE_Units_F8",0)) AS "sum_tx_vee_units_f8",
                SUM(COALESCE(t."Transactional_VEE_Units_F9",0)) AS "sum_tx_vee_units_f9",
                SUM(COALESCE(t."Transactional_VEE_Units_F10",0)) AS "sum_tx_vee_units_f10",

                SUM(COALESCE(t."Transactional_ER_Units_F1",0)) AS "sum_tx_er_units_f1",
                SUM(COALESCE(t."Transactional_ER_Units_F2",0)) AS "sum_tx_er_units_f2",
                SUM(COALESCE(t."Transactional_ER_Units_F3",0)) AS "sum_tx_er_units_f3",
                SUM(COALESCE(t."Transactional_ER_Units_F4",0)) AS "sum_tx_er_units_f4",
                SUM(COALESCE(t."Transactional_ER_Units_F5",0)) AS "sum_tx_er_units_f5",
                SUM(COALESCE(t."Transactional_ER_Units_F6",0)) AS "sum_tx_er_units_f6",
                SUM(COALESCE(t."Transactional_ER_Units_F7",0)) AS "sum_tx_er_units_f7",
                SUM(COALESCE(t."Transactional_ER_Units_F8",0)) AS "sum_tx_er_units_f8",
                SUM(COALESCE(t."Transactional_ER_Units_F9",0)) AS "sum_tx_er_units_f9",
                SUM(COALESCE(t."Transactional_ER_Units_F10",0)) AS "sum_tx_er_units_f10",

                SUM(COALESCE(t."Terminated_ER_Units_F1",0)) AS "sum_term_er_units_f1",
                SUM(COALESCE(t."Terminated_ER_Units_F2",0)) AS "sum_term_er_units_f2",
                SUM(COALESCE(t."Terminated_ER_Units_F3",0)) AS "sum_term_er_units_f3",
                SUM(COALESCE(t."Terminated_ER_Units_F4",0)) AS "sum_term_er_units_f4",
                SUM(COALESCE(t."Terminated_ER_Units_F5",0)) AS "sum_term_er_units_f5",
                SUM(COALESCE(t."Terminated_ER_Units_F6",0)) AS "sum_term_er_units_f6",
                SUM(COALESCE(t."Terminated_ER_Units_F7",0)) AS "sum_term_er_units_f7",
                SUM(COALESCE(t."Terminated_ER_Units_F8",0)) AS "sum_term_er_units_f8",
                SUM(COALESCE(t."Terminated_ER_Units_F9",0)) AS "sum_term_er_units_f9",
                SUM(COALESCE(t."Terminated_ER_Units_F10",0)) AS "sum_term_er_units_f10",

                SUM(COALESCE(t."Transactional_EE_Value_F1",0)) AS "sum_tx_ee_val_f1",
                SUM(COALESCE(t."Transactional_EE_Value_F2",0)) AS "sum_tx_ee_val_f2",
                SUM(COALESCE(t."Transactional_EE_Value_F3",0)) AS "sum_tx_ee_val_f3",
                SUM(COALESCE(t."Transactional_EE_Value_F4",0)) AS "sum_tx_ee_val_f4",
                SUM(COALESCE(t."Transactional_EE_Value_F5",0)) AS "sum_tx_ee_val_f5",
                SUM(COALESCE(t."Transactional_EE_Value_F6",0)) AS "sum_tx_ee_val_f6",
                SUM(COALESCE(t."Transactional_EE_Value_F7",0)) AS "sum_tx_ee_val_f7",
                SUM(COALESCE(t."Transactional_EE_Value_F8",0)) AS "sum_tx_ee_val_f8",
                SUM(COALESCE(t."Transactional_EE_Value_F9",0)) AS "sum_tx_ee_val_f9",
                SUM(COALESCE(t."Transactional_EE_Value_F10",0)) AS "sum_tx_ee_val_f10",

                SUM(COALESCE(t."Transactional_VEE_Value_F1",0)) AS "sum_tx_vee_val_f1",
                SUM(COALESCE(t."Transactional_VEE_Value_F2",0)) AS "sum_tx_vee_val_f2",
                SUM(COALESCE(t."Transactional_VEE_Value_F3",0)) AS "sum_tx_vee_val_f3",
                SUM(COALESCE(t."Transactional_VEE_Value_F4",0)) AS "sum_tx_vee_val_f4",
                SUM(COALESCE(t."Transactional_VEE_Value_F5",0)) AS "sum_tx_vee_val_f5",
                SUM(COALESCE(t."Transactional_VEE_Value_F6",0)) AS "sum_tx_vee_val_f6",
                SUM(COALESCE(t."Transactional_VEE_Value_F7",0)) AS "sum_tx_vee_val_f7",
                SUM(COALESCE(t."Transactional_VEE_Value_F8",0)) AS "sum_tx_vee_val_f8",
                SUM(COALESCE(t."Transactional_VEE_Value_F9",0)) AS "sum_tx_vee_val_f9",
                SUM(COALESCE(t."Transactional_VEE_Value_F10",0)) AS "sum_tx_vee_val_f10",

                SUM(COALESCE(t."Transactional_ER_Value_F1",0)) AS "sum_tx_er_val_f1",
                SUM(COALESCE(t."Transactional_ER_Value_F2",0)) AS "sum_tx_er_val_f2",
                SUM(COALESCE(t."Transactional_ER_Value_F3",0)) AS "sum_tx_er_val_f3",
                SUM(COALESCE(t."Transactional_ER_Value_F4",0)) AS "sum_tx_er_val_f4",
                SUM(COALESCE(t."Transactional_ER_Value_F5",0)) AS "sum_tx_er_val_f5",
                SUM(COALESCE(t."Transactional_ER_Value_F6",0)) AS "sum_tx_er_val_f6",
                SUM(COALESCE(t."Transactional_ER_Value_F7",0)) AS "sum_tx_er_val_f7",
                SUM(COALESCE(t."Transactional_ER_Value_F8",0)) AS "sum_tx_er_val_f8",
                SUM(COALESCE(t."Transactional_ER_Value_F9",0)) AS "sum_tx_er_val_f9",
                SUM(COALESCE(t."Transactional_ER_Value_F10",0)) AS "sum_tx_er_val_f10",

                SUM(COALESCE(t."Terminated_ER_Value_F1",0)) AS "sum_term_er_val_f1",
                SUM(COALESCE(t."Terminated_ER_Value_F2",0)) AS "sum_term_er_val_f2",
                SUM(COALESCE(t."Terminated_ER_Value_F3",0)) AS "sum_term_er_val_f3",
                SUM(COALESCE(t."Terminated_ER_Value_F4",0)) AS "sum_term_er_val_f4",
                SUM(COALESCE(t."Terminated_ER_Value_F5",0)) AS "sum_term_er_val_f5",
                SUM(COALESCE(t."Terminated_ER_Value_F6",0)) AS "sum_term_er_val_f6",
                SUM(COALESCE(t."Terminated_ER_Value_F7",0)) AS "sum_term_er_val_f7",
                SUM(COALESCE(t."Terminated_ER_Value_F8",0)) AS "sum_term_er_val_f8",
                SUM(COALESCE(t."Terminated_ER_Value_F9",0)) AS "sum_term_er_val_f9",
                SUM(COALESCE(t."Terminated_ER_Value_F10",0)) AS "sum_term_er_val_f10",

                SUM(COALESCE(t."Transactional_Total_Units_F1",0)) AS "sum_tx_tot_units_f1",
                SUM(COALESCE(t."Transactional_Total_Units_F2",0)) AS "sum_tx_tot_units_f2",
                SUM(COALESCE(t."Transactional_Total_Units_F3",0)) AS "sum_tx_tot_units_f3",
                SUM(COALESCE(t."Transactional_Total_Units_F4",0)) AS "sum_tx_tot_units_f4",
                SUM(COALESCE(t."Transactional_Total_Units_F5",0)) AS "sum_tx_tot_units_f5",
                SUM(COALESCE(t."Transactional_Total_Units_F6",0)) AS "sum_tx_tot_units_f6",
                SUM(COALESCE(t."Transactional_Total_Units_F7",0)) AS "sum_tx_tot_units_f7",
                SUM(COALESCE(t."Transactional_Total_Units_F8",0)) AS "sum_tx_tot_units_f8",
                SUM(COALESCE(t."Transactional_Total_Units_F9",0)) AS "sum_tx_tot_units_f9",
                SUM(COALESCE(t."Transactional_Total_Units_F10",0)) AS "sum_tx_tot_units_f10"
            FROM "Transactions" t
            WHERE t."Modified_Date" >= CAST(:startDate AS date)
              AND t."Modified_Date" <= CAST(:endDate AS date)
            GROUP BY t."Modified_Date", t."Payment_Date",
                     t."UP1", t."UP2", t."UP3", t."UP4", t."UP5",
                     t."UP6", t."UP7", t."UP8", t."UP9", t."UP10"
            ORDER BY t."Modified_Date", t."Payment_Date"
            """;

    public static final String AS_OF_UNITS_SUM_SQL = """
            WITH max_tx AS (
                SELECT "Employee_Number", MAX("Serial") AS max_serial
                FROM "Transactions"
                WHERE "Modified_Date" <= CAST(:asOfDate AS date)
                GROUP BY "Employee_Number"
            )
            SELECT
                COALESCE(SUM(t."Total_EE_Units_F1"), 0) AS ee1, COALESCE(SUM(t."Total_EE_Units_F2"), 0) AS ee2,
                COALESCE(SUM(t."Total_EE_Units_F3"), 0) AS ee3, COALESCE(SUM(t."Total_EE_Units_F4"), 0) AS ee4,
                COALESCE(SUM(t."Total_EE_Units_F5"), 0) AS ee5, COALESCE(SUM(t."Total_EE_Units_F6"), 0) AS ee6,
                COALESCE(SUM(t."Total_EE_Units_F7"), 0) AS ee7, COALESCE(SUM(t."Total_EE_Units_F8"), 0) AS ee8,
                COALESCE(SUM(t."Total_EE_Units_F9"), 0) AS ee9, COALESCE(SUM(t."Total_EE_Units_F10"), 0) AS ee10,

                COALESCE(SUM(t."Total_VEE_Units_F1"), 0) AS vee1, COALESCE(SUM(t."Total_VEE_Units_F2"), 0) AS vee2,
                COALESCE(SUM(t."Total_VEE_Units_F3"), 0) AS vee3, COALESCE(SUM(t."Total_VEE_Units_F4"), 0) AS vee4,
                COALESCE(SUM(t."Total_VEE_Units_F5"), 0) AS vee5, COALESCE(SUM(t."Total_VEE_Units_F6"), 0) AS vee6,
                COALESCE(SUM(t."Total_VEE_Units_F7"), 0) AS vee7, COALESCE(SUM(t."Total_VEE_Units_F8"), 0) AS vee8,
                COALESCE(SUM(t."Total_VEE_Units_F9"), 0) AS vee9, COALESCE(SUM(t."Total_VEE_Units_F10"), 0) AS vee10,

                COALESCE(SUM(t."Total_ER_Units_F1"), 0) AS er1, COALESCE(SUM(t."Total_ER_Units_F2"), 0) AS er2,
                COALESCE(SUM(t."Total_ER_Units_F3"), 0) AS er3, COALESCE(SUM(t."Total_ER_Units_F4"), 0) AS er4,
                COALESCE(SUM(t."Total_ER_Units_F5"), 0) AS er5, COALESCE(SUM(t."Total_ER_Units_F6"), 0) AS er6,
                COALESCE(SUM(t."Total_ER_Units_F7"), 0) AS er7, COALESCE(SUM(t."Total_ER_Units_F8"), 0) AS er8,
                COALESCE(SUM(t."Total_ER_Units_F9"), 0) AS er9, COALESCE(SUM(t."Total_ER_Units_F10"), 0) AS er10
            FROM "Transactions" t
            INNER JOIN max_tx m
               ON t."Employee_Number" = m."Employee_Number"
              AND t."Serial" = m.max_serial
            """;
}
