package com.rubix.pension.reports.dto;

import java.util.Map;

public class DailyMovementRowDto {
    private String modifiedDate;
    private String paymentDate;
    private String description;
    private double[] up = new double[10];

    private double contributionEe;
    private double contributionVee;
    private double contributionEr;
    private double contributionTotal;

    private double withdrawalEe;
    private double withdrawalVee;
    private double withdrawalEr;
    private double withdrawalTotal;

    private double topUpEe;
    private double topUpVee;
    private double topUpEr;
    private double topUpTotal;

    private double imcEe;
    private double imcVee;
    private double imcEr;
    private double imcTotal;

    private double adminChargesEe;
    private double adminChargesEr;
    private double adminChargesTotal;

    private double contributionChargesEe;
    private double contributionChargesVee;
    private double contributionChargesEr;
    private double contributionChargesTotal;

    private double surrenderChargesEe;
    private double surrenderChargesVee;
    private double surrenderChargesEr;
    private double surrenderChargesTotal;

    private double topUpChargesEe;
    private double topUpChargesVee;
    private double topUpChargesEr;
    private double topUpChargesTotal;

    private double withdrawalChargesEe;
    private double withdrawalChargesVee;
    private double withdrawalChargesEr;
    private double withdrawalChargesTotal;

    private double[] txEeValue = new double[10];
    private double[] txVeeValue = new double[10];
    private double[] txErValue = new double[10];
    private double[] termErValue = new double[10];

    private double[] txEeUnits = new double[10];
    private double[] txVeeUnits = new double[10];
    private double[] txErUnits = new double[10];
    private double[] termErUnits = new double[10];

    private double[] totEeUnits = new double[10];
    private double[] totVeeUnits = new double[10];
    private double[] totErUnits = new double[10];
    private double[] totUnits = new double[10];

    private double terminationEe;
    private double terminationVee;
    private double terminationEr;
    private double terminationTotal;

    private double[] txTotUnits = new double[10];
    private double[] txTotValue = new double[10];

    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double[] getUp() { return up; }
    public void setUp(double[] up) { this.up = up; }

    public double getContributionEe() { return contributionEe; }
    public void setContributionEe(double contributionEe) { this.contributionEe = contributionEe; }

    public double getContributionVee() { return contributionVee; }
    public void setContributionVee(double contributionVee) { this.contributionVee = contributionVee; }

    public double getContributionEr() { return contributionEr; }
    public void setContributionEr(double contributionEr) { this.contributionEr = contributionEr; }

    public double getContributionTotal() { return contributionTotal; }
    public void setContributionTotal(double contributionTotal) { this.contributionTotal = contributionTotal; }

    public double getWithdrawalEe() { return withdrawalEe; }
    public void setWithdrawalEe(double withdrawalEe) { this.withdrawalEe = withdrawalEe; }

    public double getWithdrawalVee() { return withdrawalVee; }
    public void setWithdrawalVee(double withdrawalVee) { this.withdrawalVee = withdrawalVee; }

    public double getWithdrawalEr() { return withdrawalEr; }
    public void setWithdrawalEr(double withdrawalEr) { this.withdrawalEr = withdrawalEr; }

    public double getWithdrawalTotal() { return withdrawalTotal; }
    public void setWithdrawalTotal(double withdrawalTotal) { this.withdrawalTotal = withdrawalTotal; }

    public double getTopUpEe() { return topUpEe; }
    public void setTopUpEe(double topUpEe) { this.topUpEe = topUpEe; }

    public double getTopUpVee() { return topUpVee; }
    public void setTopUpVee(double topUpVee) { this.topUpVee = topUpVee; }

    public double getTopUpEr() { return topUpEr; }
    public void setTopUpEr(double topUpEr) { this.topUpEr = topUpEr; }

    public double getTopUpTotal() { return topUpTotal; }
    public void setTopUpTotal(double topUpTotal) { this.topUpTotal = topUpTotal; }

    public double getImcEe() { return imcEe; }
    public void setImcEe(double imcEe) { this.imcEe = imcEe; }

    public double getImcVee() { return imcVee; }
    public void setImcVee(double imcVee) { this.imcVee = imcVee; }

    public double getImcEr() { return imcEr; }
    public void setImcEr(double imcEr) { this.imcEr = imcEr; }

    public double getImcTotal() { return imcTotal; }
    public void setImcTotal(double imcTotal) { this.imcTotal = imcTotal; }

    public double getAdminChargesEe() { return adminChargesEe; }
    public void setAdminChargesEe(double adminChargesEe) { this.adminChargesEe = adminChargesEe; }

    public double getAdminChargesEr() { return adminChargesEr; }
    public void setAdminChargesEr(double adminChargesEr) { this.adminChargesEr = adminChargesEr; }

    public double getAdminChargesTotal() { return adminChargesTotal; }
    public void setAdminChargesTotal(double adminChargesTotal) { this.adminChargesTotal = adminChargesTotal; }

    public double getContributionChargesEe() { return contributionChargesEe; }
    public void setContributionChargesEe(double contributionChargesEe) { this.contributionChargesEe = contributionChargesEe; }

    public double getContributionChargesVee() { return contributionChargesVee; }
    public void setContributionChargesVee(double contributionChargesVee) { this.contributionChargesVee = contributionChargesVee; }

    public double getContributionChargesEr() { return contributionChargesEr; }
    public void setContributionChargesEr(double contributionChargesEr) { this.contributionChargesEr = contributionChargesEr; }

    public double getContributionChargesTotal() { return contributionChargesTotal; }
    public void setContributionChargesTotal(double contributionChargesTotal) { this.contributionChargesTotal = contributionChargesTotal; }

    public double getSurrenderChargesEe() { return surrenderChargesEe; }
    public void setSurrenderChargesEe(double surrenderChargesEe) { this.surrenderChargesEe = surrenderChargesEe; }

    public double getSurrenderChargesVee() { return surrenderChargesVee; }
    public void setSurrenderChargesVee(double surrenderChargesVee) { this.surrenderChargesVee = surrenderChargesVee; }

    public double getSurrenderChargesEr() { return surrenderChargesEr; }
    public void setSurrenderChargesEr(double surrenderChargesEr) { this.surrenderChargesEr = surrenderChargesEr; }

    public double getSurrenderChargesTotal() { return surrenderChargesTotal; }
    public void setSurrenderChargesTotal(double surrenderChargesTotal) { this.surrenderChargesTotal = surrenderChargesTotal; }

    public double getTopUpChargesEe() { return topUpChargesEe; }
    public void setTopUpChargesEe(double topUpChargesEe) { this.topUpChargesEe = topUpChargesEe; }

    public double getTopUpChargesVee() { return topUpChargesVee; }
    public void setTopUpChargesVee(double topUpChargesVee) { this.topUpChargesVee = topUpChargesVee; }

    public double getTopUpChargesEr() { return topUpChargesEr; }
    public void setTopUpChargesEr(double topUpChargesEr) { this.topUpChargesEr = topUpChargesEr; }

    public double getTopUpChargesTotal() { return topUpChargesTotal; }
    public void setTopUpChargesTotal(double topUpChargesTotal) { this.topUpChargesTotal = topUpChargesTotal; }

    public double getWithdrawalChargesEe() { return withdrawalChargesEe; }
    public void setWithdrawalChargesEe(double withdrawalChargesEe) { this.withdrawalChargesEe = withdrawalChargesEe; }

    public double getWithdrawalChargesVee() { return withdrawalChargesVee; }
    public void setWithdrawalChargesVee(double withdrawalChargesVee) { this.withdrawalChargesVee = withdrawalChargesVee; }

    public double getWithdrawalChargesEr() { return withdrawalChargesEr; }
    public void setWithdrawalChargesEr(double withdrawalChargesEr) { this.withdrawalChargesEr = withdrawalChargesEr; }

    public double getWithdrawalChargesTotal() { return withdrawalChargesTotal; }
    public void setWithdrawalChargesTotal(double withdrawalChargesTotal) { this.withdrawalChargesTotal = withdrawalChargesTotal; }

    public double[] getTxEeValue() { return txEeValue; }
    public void setTxEeValue(double[] txEeValue) { this.txEeValue = txEeValue; }

    public double[] getTxVeeValue() { return txVeeValue; }
    public void setTxVeeValue(double[] txVeeValue) { this.txVeeValue = txVeeValue; }

    public double[] getTxErValue() { return txErValue; }
    public void setTxErValue(double[] txErValue) { this.txErValue = txErValue; }

    public double[] getTermErValue() { return termErValue; }
    public void setTermErValue(double[] termErValue) { this.termErValue = termErValue; }

    public double[] getTxEeUnits() { return txEeUnits; }
    public void setTxEeUnits(double[] txEeUnits) { this.txEeUnits = txEeUnits; }

    public double[] getTxVeeUnits() { return txVeeUnits; }
    public void setTxVeeUnits(double[] txVeeUnits) { this.txVeeUnits = txVeeUnits; }

    public double[] getTxErUnits() { return txErUnits; }
    public void setTxErUnits(double[] txErUnits) { this.txErUnits = txErUnits; }

    public double[] getTermErUnits() { return termErUnits; }
    public void setTermErUnits(double[] termErUnits) { this.termErUnits = termErUnits; }

    public double[] getTotEeUnits() { return totEeUnits; }
    public void setTotEeUnits(double[] totEeUnits) { this.totEeUnits = totEeUnits; }

    public double[] getTotVeeUnits() { return totVeeUnits; }
    public void setTotVeeUnits(double[] totVeeUnits) { this.totVeeUnits = totVeeUnits; }

    public double[] getTotErUnits() { return totErUnits; }
    public void setTotErUnits(double[] totErUnits) { this.totErUnits = totErUnits; }

    public double[] getTotUnits() { return totUnits; }
    public void setTotUnits(double[] totUnits) { this.totUnits = totUnits; }

    public double getTerminationEe() { return terminationEe; }
    public void setTerminationEe(double terminationEe) { this.terminationEe = terminationEe; }

    public double getTerminationVee() { return terminationVee; }
    public void setTerminationVee(double terminationVee) { this.terminationVee = terminationVee; }

    public double getTerminationEr() { return terminationEr; }
    public void setTerminationEr(double terminationEr) { this.terminationEr = terminationEr; }

    public double getTerminationTotal() { return terminationTotal; }
    public void setTerminationTotal(double terminationTotal) { this.terminationTotal = terminationTotal; }

    public double[] getTxTotUnits() { return txTotUnits; }
    public void setTxTotUnits(double[] txTotUnits) { this.txTotUnits = txTotUnits; }

    public double[] getTxTotValue() { return txTotValue; }
    public void setTxTotValue(double[] txTotValue) { this.txTotValue = txTotValue; }
}
