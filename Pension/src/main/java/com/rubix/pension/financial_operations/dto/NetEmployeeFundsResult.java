package com.rubix.pension.financial_operations.dto;

import java.util.List;

public class NetEmployeeFundsResult {

    private String companyNumber;
    private String companyName;
    private String employeeNumber;
    private String employeeName;
    private String valuationDate;
    private String dateFinal;
    private List<FundNetSummary> rows;
    private double totalEEFunds;
    private double totalVEEFunds;
    private double totalERFunds;
    private double totalFunds;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(String valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getDateFinal() {
        return dateFinal;
    }

    public void setDateFinal(String dateFinal) {
        this.dateFinal = dateFinal;
    }

    public List<FundNetSummary> getRows() {
        return rows;
    }

    public void setRows(List<FundNetSummary> rows) {
        this.rows = rows;
    }

    public double getTotalEEFunds() {
        return totalEEFunds;
    }

    public void setTotalEEFunds(double totalEEFunds) {
        this.totalEEFunds = totalEEFunds;
    }

    public double getTotalVEEFunds() {
        return totalVEEFunds;
    }

    public void setTotalVEEFunds(double totalVEEFunds) {
        this.totalVEEFunds = totalVEEFunds;
    }

    public double getTotalERFunds() {
        return totalERFunds;
    }

    public void setTotalERFunds(double totalERFunds) {
        this.totalERFunds = totalERFunds;
    }

    public double getTotalFunds() {
        return totalFunds;
    }

    public void setTotalFunds(double totalFunds) {
        this.totalFunds = totalFunds;
    }
}
