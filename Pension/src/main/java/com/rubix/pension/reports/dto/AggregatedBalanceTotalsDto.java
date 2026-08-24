package com.rubix.pension.reports.dto;

/** PDF header totals — sums of Step_1 fields used on the Access report. */
public class AggregatedBalanceTotalsDto {

    private double grossContributionEe;
    private double grossContributionVee;
    private double grossContributionEr;
    private double investmentReturnEe;
    private double investmentReturnVee;
    private double investmentReturnEr;
    private double totalInvestmentReturn;
    private double accumulatedValueEe;
    private double accumulatedValueVee;
    private double accumulatedValueEr;
    private double accumulatedValueTotal;

    public double getGrossContributionEe() {
        return grossContributionEe;
    }

    public void setGrossContributionEe(double grossContributionEe) {
        this.grossContributionEe = grossContributionEe;
    }

    public double getGrossContributionVee() {
        return grossContributionVee;
    }

    public void setGrossContributionVee(double grossContributionVee) {
        this.grossContributionVee = grossContributionVee;
    }

    public double getGrossContributionEr() {
        return grossContributionEr;
    }

    public void setGrossContributionEr(double grossContributionEr) {
        this.grossContributionEr = grossContributionEr;
    }

    public double getInvestmentReturnEe() {
        return investmentReturnEe;
    }

    public void setInvestmentReturnEe(double investmentReturnEe) {
        this.investmentReturnEe = investmentReturnEe;
    }

    public double getInvestmentReturnVee() {
        return investmentReturnVee;
    }

    public void setInvestmentReturnVee(double investmentReturnVee) {
        this.investmentReturnVee = investmentReturnVee;
    }

    public double getInvestmentReturnEr() {
        return investmentReturnEr;
    }

    public void setInvestmentReturnEr(double investmentReturnEr) {
        this.investmentReturnEr = investmentReturnEr;
    }

    public double getTotalInvestmentReturn() {
        return totalInvestmentReturn;
    }

    public void setTotalInvestmentReturn(double totalInvestmentReturn) {
        this.totalInvestmentReturn = totalInvestmentReturn;
    }

    public double getAccumulatedValueEe() {
        return accumulatedValueEe;
    }

    public void setAccumulatedValueEe(double accumulatedValueEe) {
        this.accumulatedValueEe = accumulatedValueEe;
    }

    public double getAccumulatedValueVee() {
        return accumulatedValueVee;
    }

    public void setAccumulatedValueVee(double accumulatedValueVee) {
        this.accumulatedValueVee = accumulatedValueVee;
    }

    public double getAccumulatedValueEr() {
        return accumulatedValueEr;
    }

    public void setAccumulatedValueEr(double accumulatedValueEr) {
        this.accumulatedValueEr = accumulatedValueEr;
    }

    public double getAccumulatedValueTotal() {
        return accumulatedValueTotal;
    }

    public void setAccumulatedValueTotal(double accumulatedValueTotal) {
        this.accumulatedValueTotal = accumulatedValueTotal;
    }
}
