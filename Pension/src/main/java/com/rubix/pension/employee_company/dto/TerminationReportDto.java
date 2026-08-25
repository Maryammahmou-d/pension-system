package com.rubix.pension.employee_company.dto;

import java.util.List;

public class TerminationReportDto {

    private Integer id;
    private String modifiedDate;
    private Integer serial;
    private Integer employeeSerial;
    private String reference;
    private String companyNumber;
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private Integer employeeId;
    private String employeeNumber;
    private String employeeName;
    private String nationalId;
    private String dob;
    private String gender;
    private String category;
    private String currency;
    private String pensionStartDate;
    private String terminationDate;
    private String resignationDate;
    private String paymentDate;
    private double vestingPercentage;
    private double surrenderChargesEe;
    private double surrenderChargesVee;
    private double surrenderChargesEr;
    private List<TerminationReportFundRow> rows;
    private double totalTransactionalEeValue;
    private double totalTransactionalVeeValue;
    private double totalTransactionalErValue;
    private double totalTerminatedErValue;
    private double totalTransactionalValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public Integer getEmployeeSerial() {
        return employeeSerial;
    }

    public void setEmployeeSerial(Integer employeeSerial) {
        this.employeeSerial = employeeSerial;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
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

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public String getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(String resignationDate) {
        this.resignationDate = resignationDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getVestingPercentage() {
        return vestingPercentage;
    }

    public void setVestingPercentage(double vestingPercentage) {
        this.vestingPercentage = vestingPercentage;
    }

    public double getSurrenderChargesEe() {
        return surrenderChargesEe;
    }

    public void setSurrenderChargesEe(double surrenderChargesEe) {
        this.surrenderChargesEe = surrenderChargesEe;
    }

    public double getSurrenderChargesVee() {
        return surrenderChargesVee;
    }

    public void setSurrenderChargesVee(double surrenderChargesVee) {
        this.surrenderChargesVee = surrenderChargesVee;
    }

    public double getSurrenderChargesEr() {
        return surrenderChargesEr;
    }

    public void setSurrenderChargesEr(double surrenderChargesEr) {
        this.surrenderChargesEr = surrenderChargesEr;
    }

    public List<TerminationReportFundRow> getRows() {
        return rows;
    }

    public void setRows(List<TerminationReportFundRow> rows) {
        this.rows = rows;
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
