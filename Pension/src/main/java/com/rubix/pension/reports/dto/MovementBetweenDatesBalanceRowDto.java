package com.rubix.pension.reports.dto;

public class MovementBetweenDatesBalanceRowDto {
    private int fund;
    private double unitsEe;
    private double unitsVee;
    private double unitsEr;
    private double unitsTotal;
    private double unitPrice;
    private double fundsEe;
    private double fundsVee;
    private double fundsEr;
    private double fundsTotal;

    public int getFund() { return fund; }
    public void setFund(int fund) { this.fund = fund; }

    public double getUnitsEe() { return unitsEe; }
    public void setUnitsEe(double unitsEe) { this.unitsEe = unitsEe; }

    public double getUnitsVee() { return unitsVee; }
    public void setUnitsVee(double unitsVee) { this.unitsVee = unitsVee; }

    public double getUnitsEr() { return unitsEr; }
    public void setUnitsEr(double unitsEr) { this.unitsEr = unitsEr; }

    public double getUnitsTotal() { return unitsTotal; }
    public void setUnitsTotal(double unitsTotal) { this.unitsTotal = unitsTotal; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getFundsEe() { return fundsEe; }
    public void setFundsEe(double fundsEe) { this.fundsEe = fundsEe; }

    public double getFundsVee() { return fundsVee; }
    public void setFundsVee(double fundsVee) { this.fundsVee = fundsVee; }

    public double getFundsEr() { return fundsEr; }
    public void setFundsEr(double fundsEr) { this.fundsEr = fundsEr; }

    public double getFundsTotal() { return fundsTotal; }
    public void setFundsTotal(double fundsTotal) { this.fundsTotal = fundsTotal; }
}
