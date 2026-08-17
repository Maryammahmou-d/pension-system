package com.rubix.pension.financial_operations.dto;

public class FundNetSummary {

    private int fund;
    private Double eeUnits;
    private Double veeUnits;
    private Double erUnits;
    private double totalUnits;
    private Double unitPrice;
    private Double eeFunds;
    private Double veeFunds;
    private Double erFunds;
    private double totalFunds;

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public Double getEeUnits() {
        return eeUnits;
    }

    public void setEeUnits(Double eeUnits) {
        this.eeUnits = eeUnits;
    }

    public Double getVeeUnits() {
        return veeUnits;
    }

    public void setVeeUnits(Double veeUnits) {
        this.veeUnits = veeUnits;
    }

    public Double getErUnits() {
        return erUnits;
    }

    public void setErUnits(Double erUnits) {
        this.erUnits = erUnits;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(double totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getEeFunds() {
        return eeFunds;
    }

    public void setEeFunds(Double eeFunds) {
        this.eeFunds = eeFunds;
    }

    public Double getVeeFunds() {
        return veeFunds;
    }

    public void setVeeFunds(Double veeFunds) {
        this.veeFunds = veeFunds;
    }

    public Double getErFunds() {
        return erFunds;
    }

    public void setErFunds(Double erFunds) {
        this.erFunds = erFunds;
    }

    public double getTotalFunds() {
        return totalFunds;
    }

    public void setTotalFunds(double totalFunds) {
        this.totalFunds = totalFunds;
    }
}
