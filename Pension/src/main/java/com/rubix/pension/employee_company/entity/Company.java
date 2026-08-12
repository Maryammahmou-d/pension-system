package com.rubix.pension.employee_company.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "\"Companies\"")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "companies_id_seq")
    @SequenceGenerator(
            name = "companies_id_seq",
            sequenceName = "companies_id_seq",
            allocationSize = 1
    )
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"Serial\"")
    private Integer serial;

    @Column(name = "\"Modified_Date\"")
    private OffsetDateTime modifiedDate;

    @Column(name = "\"Company_Number\"")
    private String companyNumber;

    @Column(name = "\"Kafs_Company_Number\"")
    private String kafsCompanyNumber;

    @Column(name = "\"Company_Name\"")
    private String companyName;

    @Column(name = "\"Issue_Date\"")
    private OffsetDateTime issueDate;

    @Column(name = "\"Frequency\"")
    private String frequency;

    @Column(name = "\"Address\"")
    private String address;

    @Column(name = "\"Contact_Person\"")
    private String contactPerson;

    @Column(name = "\"Mobile_Number\"")
    private String mobileNumber;

    @Column(name = "\"Email\"")
    private String email;

    @Column(name = "\"Starting_Number_of_Employees\"")
    private Integer startingNumberOfEmployees;

    @Column(name = "\"Starting_Average_Salary\"")
    private Double startingAverageSalary;

    @Column(name = "\"Starting_Fund_Value\"")
    private Double startingFundValue;

    @Column(name = "\"Contribution_Charges\"")
    private Double contributionCharges;

    @Column(name = "\"Contribution_Charges_VEE\"")
    private Double contributionChargesVee;

    @Column(name = "\"IMC\"")
    private Double imc;

    @Column(name = "\"Withdrawal_Charges_EE\"")
    private Double withdrawalChargesEe;

    @Column(name = "\"Withdrawal_Charges_VEE\"")
    private Double withdrawalChargesVee;

    @Column(name = "\"Withdrawal_Charges_ER\"")
    private Double withdrawalChargesEr;

    @Column(name = "\"Employee_Surrender_Charge\"")
    private Double employeeSurrenderCharge;

    @Column(name = "\"Top_Up_Charges\"")
    private Integer topUpCharges;

    @Column(name = "\"Admin_Charges\"")
    private Integer adminCharges;

    @Column(name = "\"Portfolio_Switching_Charges\"")
    private Integer portfolioSwitchingCharges;

    @Column(name = "\"Allocation_Redirection_Charges\"")
    private Integer allocationRedirectionCharges;

    @Column(name = "\"Termination_Date\"")
    private OffsetDateTime terminationDate;

    @Column(name = "\"New (0) /Acquired (1)\"")
    private Integer newOrAcquired;

    @Column(name = "\"Vesting_On_Hire\"")
    private Boolean vestingOnHire;

    @Column(name = "\"Max_Withdrawal_Percentage\"")
    private Double maxWithdrawalPercentage;

    @Column(name = "\"Max_Withdrawal_Count\"")
    private Double maxWithdrawalCount;

    @Column(name = "\"Salary (Yes) / Contribution (No)\"")
    private Boolean salaryOrContribution;

    @Column(name = "\"ShowAvailableWithdrawal (Yes) / HideVesting (No)\"")
    private Boolean showAvailableWithdrawal;

    @Column(name = "\"UserName\"")
    private String userName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public OffsetDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(OffsetDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}