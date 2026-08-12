package com.rubix.pension.employee_company.dto;
import java.time.OffsetDateTime;

public class CreateCompanyRequest {

    private String companyNumber;

    private String kafsCompanyNumber;

    private String companyName;

    private OffsetDateTime issueDate;

    private String frequency;

    private String address;

    private String contactPerson;

    private String mobileNumber;

    private String email;

    private Integer startingNumberOfEmployees;

    private Double startingAverageSalary;

    private Double startingFundValue;

    private Double contributionCharges;

    private Double contributionChargesVee;

    private Double imc;

    private Double withdrawalChargesEe;

    private Double withdrawalChargesVee;

    private Double withdrawalChargesEr;

    private Double employeeSurrenderCharge;

    private Integer topUpCharges;

    private Integer adminCharges;

    private Integer portfolioSwitchingCharges;

    private Integer allocationRedirectionCharges;

    private OffsetDateTime terminationDate;

    private Integer newOrAcquired;

    private Boolean vestingOnHire;

    private Double maxWithdrawalPercentage;

    private Double maxWithdrawalCount;

    private Boolean salaryOrContribution;

    private Boolean showAvailableWithdrawal;


    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getKafsCompanyNumber() {
        return kafsCompanyNumber;
    }

    public void setKafsCompanyNumber(String kafsCompanyNumber) {
        this.kafsCompanyNumber = kafsCompanyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public OffsetDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(OffsetDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStartingNumberOfEmployees() {
        return startingNumberOfEmployees;
    }

    public void setStartingNumberOfEmployees(Integer startingNumberOfEmployees) {
        this.startingNumberOfEmployees = startingNumberOfEmployees;
    }

    public Double getStartingAverageSalary() {
        return startingAverageSalary;
    }

    public void setStartingAverageSalary(Double startingAverageSalary) {
        this.startingAverageSalary = startingAverageSalary;
    }

    public Double getStartingFundValue() {
        return startingFundValue;
    }

    public void setStartingFundValue(Double startingFundValue) {
        this.startingFundValue = startingFundValue;
    }

    public Double getContributionCharges() {
        return contributionCharges;
    }

    public void setContributionCharges(Double contributionCharges) {
        this.contributionCharges = contributionCharges;
    }

    public Double getContributionChargesVee() {
        return contributionChargesVee;
    }

    public void setContributionChargesVee(Double contributionChargesVee) {
        this.contributionChargesVee = contributionChargesVee;
    }

    public Double getImc() {
        return imc;
    }

    public void setImc(Double imc) {
        this.imc = imc;
    }

    public Double getWithdrawalChargesEe() {
        return withdrawalChargesEe;
    }

    public void setWithdrawalChargesEe(Double withdrawalChargesEe) {
        this.withdrawalChargesEe = withdrawalChargesEe;
    }

    public Double getWithdrawalChargesVee() {
        return withdrawalChargesVee;
    }

    public void setWithdrawalChargesVee(Double withdrawalChargesVee) {
        this.withdrawalChargesVee = withdrawalChargesVee;
    }

    public Double getWithdrawalChargesEr() {
        return withdrawalChargesEr;
    }

    public void setWithdrawalChargesEr(Double withdrawalChargesEr) {
        this.withdrawalChargesEr = withdrawalChargesEr;
    }

    public Double getEmployeeSurrenderCharge() {
        return employeeSurrenderCharge;
    }

    public void setEmployeeSurrenderCharge(Double employeeSurrenderCharge) {
        this.employeeSurrenderCharge = employeeSurrenderCharge;
    }

    public Integer getTopUpCharges() {
        return topUpCharges;
    }

    public void setTopUpCharges(Integer topUpCharges) {
        this.topUpCharges = topUpCharges;
    }

    public Integer getAdminCharges() {
        return adminCharges;
    }

    public void setAdminCharges(Integer adminCharges) {
        this.adminCharges = adminCharges;
    }

    public Integer getPortfolioSwitchingCharges() {
        return portfolioSwitchingCharges;
    }

    public void setPortfolioSwitchingCharges(Integer portfolioSwitchingCharges) {
        this.portfolioSwitchingCharges = portfolioSwitchingCharges;
    }

    public Integer getAllocationRedirectionCharges() {
        return allocationRedirectionCharges;
    }

    public void setAllocationRedirectionCharges(Integer allocationRedirectionCharges) {
        this.allocationRedirectionCharges = allocationRedirectionCharges;
    }

    public OffsetDateTime getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(OffsetDateTime terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Integer getNewOrAcquired() {
        return newOrAcquired;
    }

    public void setNewOrAcquired(Integer newOrAcquired) {
        this.newOrAcquired = newOrAcquired;
    }

    public Boolean getVestingOnHire() {
        return vestingOnHire;
    }

    public void setVestingOnHire(Boolean vestingOnHire) {
        this.vestingOnHire = vestingOnHire;
    }

    public Double getMaxWithdrawalPercentage() {
        return maxWithdrawalPercentage;
    }

    public void setMaxWithdrawalPercentage(Double maxWithdrawalPercentage) {
        this.maxWithdrawalPercentage = maxWithdrawalPercentage;
    }

    public Double getMaxWithdrawalCount() {
        return maxWithdrawalCount;
    }

    public void setMaxWithdrawalCount(Double maxWithdrawalCount) {
        this.maxWithdrawalCount = maxWithdrawalCount;
    }

    public Boolean getSalaryOrContribution() {
        return salaryOrContribution;
    }

    public void setSalaryOrContribution(Boolean salaryOrContribution) {
        this.salaryOrContribution = salaryOrContribution;
    }

    public Boolean getShowAvailableWithdrawal() {
        return showAvailableWithdrawal;
    }

    public void setShowAvailableWithdrawal(Boolean showAvailableWithdrawal) {
        this.showAvailableWithdrawal = showAvailableWithdrawal;
    }
}

