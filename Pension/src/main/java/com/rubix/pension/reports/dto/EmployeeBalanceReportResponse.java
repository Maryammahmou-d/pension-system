package com.rubix.pension.reports.dto;

import java.util.ArrayList;
import java.util.List;

public class EmployeeBalanceReportResponse {

    private String companyNumber;
    private String employeeNumber;
    private Integer employeeId;
    private String fullName;
    private String currency;
    private String valuationDate;
    private List<EmployeeBalanceRowDto> rows = new ArrayList<>();
    private EmployeeBalanceTotalsDto totals = new EmployeeBalanceTotalsDto();

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(String valuationDate) {
        this.valuationDate = valuationDate;
    }

    public List<EmployeeBalanceRowDto> getRows() {
        return rows;
    }

    public void setRows(List<EmployeeBalanceRowDto> rows) {
        this.rows = rows;
    }

    public EmployeeBalanceTotalsDto getTotals() {
        return totals;
    }

    public void setTotals(EmployeeBalanceTotalsDto totals) {
        this.totals = totals;
    }
}
