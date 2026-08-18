package com.rubix.pension.financial_operations.dto;

public class FundUnitRow {

    private int fund;
    private Double eeUnits;
    private Double veeUnits;
    private Double erUnits;
    private double totalUnits;

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
}
