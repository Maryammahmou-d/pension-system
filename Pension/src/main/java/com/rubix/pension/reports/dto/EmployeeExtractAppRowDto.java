package com.rubix.pension.reports.dto;

import java.time.LocalDate;

/** One Access TempAppData row for Employee Extract for App. */
public class EmployeeExtractAppRowDto {

    private String companyNumber;
    private String companyName;
    private String employeeNumber;
    private String fullName;
    private String nationalId;
    private LocalDate dob;
    private String gender;
    private String occupation;
    private LocalDate pensionStartDate;
    private String email;
    private LocalDate terminationDate;
    private Double availableWithdrawal;
    private double totalFundValue;
    private double grossEeContribution;
    private double grossVeeContribution;
    private double grossErContribution;
    private double netEeContribution;
    private double netVeeContribution;
    private double netErContribution;
    private double gainValue;
    private double percentageGainUnitPrice;
    private Double percentageGainPaid;
    private LocalDate priceDate;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public LocalDate getPensionStartDate() {
        return pensionStartDate;
    }

    public void setPensionStartDate(LocalDate pensionStartDate) {
        this.pensionStartDate = pensionStartDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Double getAvailableWithdrawal() {
        return availableWithdrawal;
    }

    public void setAvailableWithdrawal(Double availableWithdrawal) {
        this.availableWithdrawal = availableWithdrawal;
    }

    public double getTotalFundValue() {
        return totalFundValue;
    }

    public void setTotalFundValue(double totalFundValue) {
        this.totalFundValue = totalFundValue;
    }

    public double getGrossEeContribution() {
        return grossEeContribution;
    }

    public void setGrossEeContribution(double grossEeContribution) {
        this.grossEeContribution = grossEeContribution;
    }

    public double getGrossVeeContribution() {
        return grossVeeContribution;
    }

    public void setGrossVeeContribution(double grossVeeContribution) {
        this.grossVeeContribution = grossVeeContribution;
    }

    public double getGrossErContribution() {
        return grossErContribution;
    }

    public void setGrossErContribution(double grossErContribution) {
        this.grossErContribution = grossErContribution;
    }

    public double getNetEeContribution() {
        return netEeContribution;
    }

    public void setNetEeContribution(double netEeContribution) {
        this.netEeContribution = netEeContribution;
    }

    public double getNetVeeContribution() {
        return netVeeContribution;
    }

    public void setNetVeeContribution(double netVeeContribution) {
        this.netVeeContribution = netVeeContribution;
    }

    public double getNetErContribution() {
        return netErContribution;
    }

    public void setNetErContribution(double netErContribution) {
        this.netErContribution = netErContribution;
    }

    public double getGainValue() {
        return gainValue;
    }

    public void setGainValue(double gainValue) {
        this.gainValue = gainValue;
    }

    public double getPercentageGainUnitPrice() {
        return percentageGainUnitPrice;
    }

    public void setPercentageGainUnitPrice(double percentageGainUnitPrice) {
        this.percentageGainUnitPrice = percentageGainUnitPrice;
    }

    public Double getPercentageGainPaid() {
        return percentageGainPaid;
    }

    public void setPercentageGainPaid(Double percentageGainPaid) {
        this.percentageGainPaid = percentageGainPaid;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }
}
