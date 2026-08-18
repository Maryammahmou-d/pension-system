package com.rubix.pension.financial_operations.dto;

import java.util.List;

public class EmployeeWithdrawalEstimateDto {

    private String companyNumber;
    private String companyName;
    private String employeeNumber;
    private String employeeName;
    private Integer employeeId;
    private String currency;
    private String withdrawalDate;
    private String pensionStartDate;
    private String terminationDate;
    private String nationalId;
    private String category;
    private String companyAddress;
    private String companyPhone;
    private List<WithdrawalRowDto> rows;
    private Charges charges = new Charges();
    private double maximumWithdrawalPercentage;
    private double maximumWithdrawalCount;
    private int withdrawalCountInPast365Days;
    private double vestingRulePercentage;
    private double totalEmployeeFund;
    private double totalVoluntaryEmployeeFund;
    private double totalEmployerFund;
    private double availableEmployeeFund;
    private double availableVoluntaryEmployeeFund;
    private double availableEmployerFund;

    public static class Charges {
        private double ee;
        private double voluntaryEE;
        private double er;

        public double getEe() {
            return ee;
        }

        public void setEe(double ee) {
            this.ee = ee;
        }

        public double getVoluntaryEE() {
            return voluntaryEE;
        }

        public void setVoluntaryEE(double voluntaryEE) {
            this.voluntaryEE = voluntaryEE;
        }

        public double getEr() {
            return er;
        }

        public void setEr(double er) {
            this.er = er;
        }
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

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(String withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getPensionStartDate() {
        return pensionStartDate;
    }

    public void setPensionStartDate(String pensionStartDate) {
        this.pensionStartDate = pensionStartDate;
    }

    public String getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(String terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public List<WithdrawalRowDto> getRows() {
        return rows;
    }

    public void setRows(List<WithdrawalRowDto> rows) {
        this.rows = rows;
    }

    public Charges getCharges() {
        return charges;
    }

    public void setCharges(Charges charges) {
        this.charges = charges;
    }

    public double getMaximumWithdrawalPercentage() {
        return maximumWithdrawalPercentage;
    }

    public void setMaximumWithdrawalPercentage(double maximumWithdrawalPercentage) {
        this.maximumWithdrawalPercentage = maximumWithdrawalPercentage;
    }

    public double getMaximumWithdrawalCount() {
        return maximumWithdrawalCount;
    }

    public void setMaximumWithdrawalCount(double maximumWithdrawalCount) {
        this.maximumWithdrawalCount = maximumWithdrawalCount;
    }

    public int getWithdrawalCountInPast365Days() {
        return withdrawalCountInPast365Days;
    }

    public void setWithdrawalCountInPast365Days(int withdrawalCountInPast365Days) {
        this.withdrawalCountInPast365Days = withdrawalCountInPast365Days;
    }

    public double getVestingRulePercentage() {
        return vestingRulePercentage;
    }

    public void setVestingRulePercentage(double vestingRulePercentage) {
        this.vestingRulePercentage = vestingRulePercentage;
    }

    public double getTotalEmployeeFund() {
        return totalEmployeeFund;
    }

    public void setTotalEmployeeFund(double totalEmployeeFund) {
        this.totalEmployeeFund = totalEmployeeFund;
    }

    public double getTotalVoluntaryEmployeeFund() {
        return totalVoluntaryEmployeeFund;
    }

    public void setTotalVoluntaryEmployeeFund(double totalVoluntaryEmployeeFund) {
        this.totalVoluntaryEmployeeFund = totalVoluntaryEmployeeFund;
    }

    public double getTotalEmployerFund() {
        return totalEmployerFund;
    }

    public void setTotalEmployerFund(double totalEmployerFund) {
        this.totalEmployerFund = totalEmployerFund;
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
