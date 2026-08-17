package com.rubix.pension.financial_operations.dto;

public class WithdrawalAmountDto {

    private int fund;
    private double employeeFund;
    private double voluntaryEmployeeFund;
    private double employerFund;

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public double getEmployeeFund() {
        return employeeFund;
    }

    public void setEmployeeFund(double employeeFund) {
        this.employeeFund = employeeFund;
    }

    public double getVoluntaryEmployeeFund() {
        return voluntaryEmployeeFund;
    }

    public void setVoluntaryEmployeeFund(double voluntaryEmployeeFund) {
        this.voluntaryEmployeeFund = voluntaryEmployeeFund;
    }

    public double getEmployerFund() {
        return employerFund;
    }

    public void setEmployerFund(double employerFund) {
        this.employerFund = employerFund;
    }
}
