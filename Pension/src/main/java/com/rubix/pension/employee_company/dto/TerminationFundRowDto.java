package com.rubix.pension.employee_company.dto;

public class TerminationFundRowDto {

    private int fund;
    private double eeUnits;
    private double veeUnits;
    private double erUnits;
    private double terminatedErUnits;
    private double totalUnits;
    private double unitPrice;

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public double getEeUnits() {
        return eeUnits;
    }

    public void setEeUnits(double eeUnits) {
        this.eeUnits = eeUnits;
    }

    public double getVeeUnits() {
        return veeUnits;
    }

    public void setVeeUnits(double veeUnits) {
        this.veeUnits = veeUnits;
    }

    public double getErUnits() {
        return erUnits;
    }

    public void setErUnits(double erUnits) {
        this.erUnits = erUnits;
    }

    public double getTerminatedErUnits() {
        return terminatedErUnits;
    }

    public void setTerminatedErUnits(double terminatedErUnits) {
        this.terminatedErUnits = terminatedErUnits;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(double totalUnits) {
        this.totalUnits = totalUnits;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
