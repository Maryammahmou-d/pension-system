package com.rubix.pension.employee_company.dto;

import java.util.ArrayList;
import java.util.List;

public class TerminationFundReportDto {

    private int fund;
    private double unitPrice;
    private String companyNumber;
    private String companyName;
    private String terminationDate;
    private int employeeCount;
    private List<TerminationFundEmployeeRow> employees = new ArrayList<>();
    private double totalStartingEeUnits;
    private double totalStartingVeeUnits;
    private double totalStartingErUnits;
    private double totalTransactionalEeUnits;
    private double totalTransactionalVeeUnits;
    private double totalTransactionalErUnits;
    private double totalTerminatedErUnits;
    private double totalTransactionalUnits;
    private double totalTransactionalEeValue;
    private double totalTransactionalVeeValue;
    private double totalTransactionalErValue;
    private double totalTerminatedErValue;
    private double totalTransactionalValue;

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

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

    public String getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(String terminationDate) {
        this.terminationDate = terminationDate;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public List<TerminationFundEmployeeRow> getEmployees() {
        return employees;
    }

    public void setEmployees(List<TerminationFundEmployeeRow> employees) {
        this.employees = employees == null ? new ArrayList<>() : employees;
    }

    public double getTotalStartingEeUnits() {
        return totalStartingEeUnits;
    }

    public void setTotalStartingEeUnits(double totalStartingEeUnits) {
        this.totalStartingEeUnits = totalStartingEeUnits;
    }

    public double getTotalStartingVeeUnits() {
        return totalStartingVeeUnits;
    }

    public void setTotalStartingVeeUnits(double totalStartingVeeUnits) {
        this.totalStartingVeeUnits = totalStartingVeeUnits;
    }

    public double getTotalStartingErUnits() {
        return totalStartingErUnits;
    }

    public void setTotalStartingErUnits(double totalStartingErUnits) {
        this.totalStartingErUnits = totalStartingErUnits;
    }

    public double getTotalTransactionalEeUnits() {
        return totalTransactionalEeUnits;
    }

    public void setTotalTransactionalEeUnits(double totalTransactionalEeUnits) {
        this.totalTransactionalEeUnits = totalTransactionalEeUnits;
    }

    public double getTotalTransactionalVeeUnits() {
        return totalTransactionalVeeUnits;
    }

    public void setTotalTransactionalVeeUnits(double totalTransactionalVeeUnits) {
        this.totalTransactionalVeeUnits = totalTransactionalVeeUnits;
    }

    public double getTotalTransactionalErUnits() {
        return totalTransactionalErUnits;
    }

    public void setTotalTransactionalErUnits(double totalTransactionalErUnits) {
        this.totalTransactionalErUnits = totalTransactionalErUnits;
    }

    public double getTotalTerminatedErUnits() {
        return totalTerminatedErUnits;
    }

    public void setTotalTerminatedErUnits(double totalTerminatedErUnits) {
        this.totalTerminatedErUnits = totalTerminatedErUnits;
    }

    public double getTotalTransactionalUnits() {
        return totalTransactionalUnits;
    }

    public void setTotalTransactionalUnits(double totalTransactionalUnits) {
        this.totalTransactionalUnits = totalTransactionalUnits;
    }

    public double getTotalTransactionalEeValue() {
        return totalTransactionalEeValue;
    }

    public void setTotalTransactionalEeValue(double totalTransactionalEeValue) {
        this.totalTransactionalEeValue = totalTransactionalEeValue;
    }

    public double getTotalTransactionalVeeValue() {
        return totalTransactionalVeeValue;
    }

    public void setTotalTransactionalVeeValue(double totalTransactionalVeeValue) {
        this.totalTransactionalVeeValue = totalTransactionalVeeValue;
    }

    public double getTotalTransactionalErValue() {
        return totalTransactionalErValue;
    }

    public void setTotalTransactionalErValue(double totalTransactionalErValue) {
        this.totalTransactionalErValue = totalTransactionalErValue;
    }

    public double getTotalTerminatedErValue() {
        return totalTerminatedErValue;
    }

    public void setTotalTerminatedErValue(double totalTerminatedErValue) {
        this.totalTerminatedErValue = totalTerminatedErValue;
    }

    public double getTotalTransactionalValue() {
        return totalTransactionalValue;
    }

    public void setTotalTransactionalValue(double totalTransactionalValue) {
        this.totalTransactionalValue = totalTransactionalValue;
    }
}
