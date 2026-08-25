package com.rubix.pension.employee_company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "Payment_Date",
        "Company_Number",
        "Employee_ID",
        "Employee_Number",
        "National_ID",
        "Full_Name",
        "DOB",
        "Gender",
        "Currency",
        "Total_EE_Value",
        "Total_VEE_Value",
        "Total_ER_Value",
        "Description",
        "Total_Terminated_ER_Value",
        "Total_Value"
})
public class TerminationSummaryRowDto {

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Payment_Date")
    private String paymentDate;

    @JsonProperty("Company_Number")
    private String companyNumber;

    @JsonProperty("Employee_ID")
    private Integer employeeId;

    @JsonProperty("Employee_Number")
    private String employeeNumber;

    @JsonProperty("National_ID")
    private String nationalId;

    @JsonProperty("Full_Name")
    private String fullName;

    @JsonProperty("DOB")
    private String dob;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Total_EE_Value")
    private double totalEeValue;

    @JsonProperty("Total_VEE_Value")
    private double totalVeeValue;

    @JsonProperty("Total_ER_Value")
    private double totalErValue;

    @JsonProperty("Total_Terminated_ER_Value")
    private double totalTerminatedErValue;

    @JsonProperty("Total_Value")
    private double totalValue;

    @JsonProperty("Description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("Payment_Date")
    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    @JsonProperty("Company_Number")
    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @JsonProperty("Employee_ID")
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    @JsonProperty("Employee_Number")
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    @JsonProperty("National_ID")
    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    @JsonProperty("Full_Name")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @JsonProperty("DOB")
    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    @JsonProperty("Gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @JsonProperty("Currency")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("Total_EE_Value")
    public double getTotalEeValue() {
        return totalEeValue;
    }

    public void setTotalEeValue(double totalEeValue) {
        this.totalEeValue = totalEeValue;
    }

    @JsonProperty("Total_VEE_Value")
    public double getTotalVeeValue() {
        return totalVeeValue;
    }

    public void setTotalVeeValue(double totalVeeValue) {
        this.totalVeeValue = totalVeeValue;
    }

    @JsonProperty("Total_ER_Value")
    public double getTotalErValue() {
        return totalErValue;
    }

    public void setTotalErValue(double totalErValue) {
        this.totalErValue = totalErValue;
    }

    @JsonProperty("Total_Terminated_ER_Value")
    public double getTotalTerminatedErValue() {
        return totalTerminatedErValue;
    }

    public void setTotalTerminatedErValue(double totalTerminatedErValue) {
        this.totalTerminatedErValue = totalTerminatedErValue;
    }

    @JsonProperty("Total_Value")
    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
}
