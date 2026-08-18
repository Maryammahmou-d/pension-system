package com.rubix.pension.financial_operations.dto;

public class WithdrawalRowDto {

    private int fund;
    private double employeeFundUnits;
    private double voluntaryEmployeeFundUnits;
    private double employerFundUnits;
    private double unitPrice;
    private double employeeFundTotal;
    private double voluntaryEmployeeFundTotal;
    private double employerFundTotal;
    private double availableEmployeeFund;
    private double availableVoluntaryEmployeeFund;
    private double availableEmployerFund;

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public double getEmployeeFundUnits() {
        return employeeFundUnits;
    }

    public void setEmployeeFundUnits(double employeeFundUnits) {
        this.employeeFundUnits = employeeFundUnits;
    }

    public double getVoluntaryEmployeeFundUnits() {
        return voluntaryEmployeeFundUnits;
    }

    public void setVoluntaryEmployeeFundUnits(double voluntaryEmployeeFundUnits) {
        this.voluntaryEmployeeFundUnits = voluntaryEmployeeFundUnits;
    }

    public double getEmployerFundUnits() {
        return employerFundUnits;
    }

    public void setEmployerFundUnits(double employerFundUnits) {
        this.employerFundUnits = employerFundUnits;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getEmployeeFundTotal() {
        return employeeFundTotal;
    }

    public void setEmployeeFundTotal(double employeeFundTotal) {
        this.employeeFundTotal = employeeFundTotal;
    }

    public double getVoluntaryEmployeeFundTotal() {
        return voluntaryEmployeeFundTotal;
    }

    public void setVoluntaryEmployeeFundTotal(double voluntaryEmployeeFundTotal) {
        this.voluntaryEmployeeFundTotal = voluntaryEmployeeFundTotal;
    }

    public double getEmployerFundTotal() {
        return employerFundTotal;
    }

    public void setEmployerFundTotal(double employerFundTotal) {
        this.employerFundTotal = employerFundTotal;
    }

    public double getAvailableEmployeeFund() {
        return availableEmployeeFund;
    }

    public void setAvailableEmployeeFund(double availableEmployeeFund) {
        this.availableEmployeeFund = availableEmployeeFund;
    }

    public double getAvailableVoluntaryEmployeeFund() {
        return availableVoluntaryEmployeeFund;
    }

    public void setAvailableVoluntaryEmployeeFund(double availableVoluntaryEmployeeFund) {
        this.availableVoluntaryEmployeeFund = availableVoluntaryEmployeeFund;
    }

    public double getAvailableEmployerFund() {
        return availableEmployerFund;
    }

    public void setAvailableEmployerFund(double availableEmployerFund) {
        this.availableEmployerFund = availableEmployerFund;
    }
}
