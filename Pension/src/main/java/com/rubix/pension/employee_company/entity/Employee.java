package com.rubix.pension.employee_company.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "\"Employees\"")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employees_id_seq")
    @SequenceGenerator(
            name = "employees_id_seq",
            sequenceName = "employees_id_seq",
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

    @Column(name = "\"Employee_ID\"")
    private Integer employeeId;

    @Column(name = "\"Employee_Number\"")
    private String employeeNumber;

    @Column(name = "\"National_ID\"")
    private String nationalId;

    @Column(name = "\"Full_Name\"")
    private String fullName;

    @Column(name = "\"DOB\"")
    private OffsetDateTime dob;

    @Column(name = "\"Gender\"")
    private String gender;

    @Column(name = "\"Occupation\"")
    private String occupation;

    @Column(name = "\"Hire_Date\"")
    private OffsetDateTime hireDate;

    @Column(name = "\"Age_At_Hire\"")
    private Double ageAtHire;

    @Column(name = "\"Pension_Start_Date\"")
    private OffsetDateTime pensionStartDate;

    @Column(name = "\"Kaf_Joining_Date\"")
    private OffsetDateTime kafJoiningDate;

    @Column(name = "\"Category\"")
    private String category;

    @Column(name = "\"Gross_Salary\"")
    private Double grossSalary;

    @Column(name = "\"Salary_Currency\"")
    private String salaryCurrency;

    @Column(name = "\"Contribution_EE\"")
    private Double contributionEe;

    @Column(name = "\"Contribution_ER\"")
    private Double contributionEr;

    @Column(name = "\"E-mail\"")
    private String email;

    @Column(name = "\"Starting_EE_Value\"")
    private Double startingEeValue;

    @Column(name = "\"Starting_ER_Value\"")
    private Double startingErValue;

    @Column(name = "\"Starting_Fund_Value\"")
    private Double startingFundValue;

    @Column(name = "\"Termination_Date\"")
    private OffsetDateTime terminationDate;

    @Column(name = "\"Resignation_Date\"")
    private OffsetDateTime resignationDate;

    @Column(name = "\"VEE\"")
    private Double vee;

    @Column(name = "\"Weight_F1_EE\"")
    private Double weightF1Ee;

    @Column(name = "\"Weight_F2_EE\"")
    private Double weightF2Ee;

    @Column(name = "\"Weight_F3_EE\"")
    private Double weightF3Ee;

    @Column(name = "\"Weight_F4_EE\"")
    private Double weightF4Ee;

    @Column(name = "\"Weight_F5_EE\"")
    private Double weightF5Ee;

    @Column(name = "\"Weight_F6_EE\"")
    private Double weightF6Ee;

    @Column(name = "\"Weight_F7_EE\"")
    private Double weightF7Ee;

    @Column(name = "\"Weight_F8_EE\"")
    private Double weightF8Ee;

    @Column(name = "\"Weight_F9_EE\"")
    private Double weightF9Ee;

    @Column(name = "\"Weight_F10_EE\"")
    private Double weightF10Ee;

    @Column(name = "\"Weight_F1_ER\"")
    private Double weightF1Er;

    @Column(name = "\"Weight_F2_ER\"")
    private Double weightF2Er;

    @Column(name = "\"Weight_F3_ER\"")
    private Double weightF3Er;

    @Column(name = "\"Weight_F4_ER\"")
    private Double weightF4Er;

    @Column(name = "\"Weight_F5_ER\"")
    private Double weightF5Er;

    @Column(name = "\"Weight_F6_ER\"")
    private Double weightF6Er;

    @Column(name = "\"Weight_F7_ER\"")
    private Double weightF7Er;

    @Column(name = "\"Weight_F8_ER\"")
    private Double weightF8Er;

    @Column(name = "\"Weight_F9_ER\"")
    private Double weightF9Er;

    @Column(name = "\"Weight_F10_ER\"")
    private Double weightF10Er;

    @Column(name = "\"Username\"")
    private String username;

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

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public OffsetDateTime getDob() {
        return dob;
    }

    public void setDob(OffsetDateTime dob) {
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

    public OffsetDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(OffsetDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public Double getAgeAtHire() {
        return ageAtHire;
    }

    public void setAgeAtHire(Double ageAtHire) {
        this.ageAtHire = ageAtHire;
    }

    public OffsetDateTime getPensionStartDate() {
        return pensionStartDate;
    }

    public void setPensionStartDate(OffsetDateTime pensionStartDate) {
        this.pensionStartDate = pensionStartDate;
    }

    public OffsetDateTime getKafJoiningDate() {
        return kafJoiningDate;
    }

    public void setKafJoiningDate(OffsetDateTime kafJoiningDate) {
        this.kafJoiningDate = kafJoiningDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(Double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public Double getContributionEe() {
        return contributionEe;
    }

    public void setContributionEe(Double contributionEe) {
        this.contributionEe = contributionEe;
    }

    public Double getContributionEr() {
        return contributionEr;
    }

    public void setContributionEr(Double contributionEr) {
        this.contributionEr = contributionEr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getStartingEeValue() {
        return startingEeValue;
    }

    public void setStartingEeValue(Double startingEeValue) {
        this.startingEeValue = startingEeValue;
    }

    public Double getStartingErValue() {
        return startingErValue;
    }

    public void setStartingErValue(Double startingErValue) {
        this.startingErValue = startingErValue;
    }

    public Double getStartingFundValue() {
        return startingFundValue;
    }

    public void setStartingFundValue(Double startingFundValue) {
        this.startingFundValue = startingFundValue;
    }

    public OffsetDateTime getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(OffsetDateTime terminationDate) {
        this.terminationDate = terminationDate;
    }

    public OffsetDateTime getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(OffsetDateTime resignationDate) {
        this.resignationDate = resignationDate;
    }

    public Double getVee() {
        return vee;
    }

    public void setVee(Double vee) {
        this.vee = vee;
    }

    public Double getWeightF1Ee() {
        return weightF1Ee;
    }

    public void setWeightF1Ee(Double weightF1Ee) {
        this.weightF1Ee = weightF1Ee;
    }

    public Double getWeightF2Ee() {
        return weightF2Ee;
    }

    public void setWeightF2Ee(Double weightF2Ee) {
        this.weightF2Ee = weightF2Ee;
    }

    public Double getWeightF3Ee() {
        return weightF3Ee;
    }

    public void setWeightF3Ee(Double weightF3Ee) {
        this.weightF3Ee = weightF3Ee;
    }

    public Double getWeightF4Ee() {
        return weightF4Ee;
    }

    public void setWeightF4Ee(Double weightF4Ee) {
        this.weightF4Ee = weightF4Ee;
    }

    public Double getWeightF5Ee() {
        return weightF5Ee;
    }

    public void setWeightF5Ee(Double weightF5Ee) {
        this.weightF5Ee = weightF5Ee;
    }

    public Double getWeightF6Ee() {
        return weightF6Ee;
    }

    public void setWeightF6Ee(Double weightF6Ee) {
        this.weightF6Ee = weightF6Ee;
    }

    public Double getWeightF7Ee() {
        return weightF7Ee;
    }

    public void setWeightF7Ee(Double weightF7Ee) {
        this.weightF7Ee = weightF7Ee;
    }

    public Double getWeightF8Ee() {
        return weightF8Ee;
    }

    public void setWeightF8Ee(Double weightF8Ee) {
        this.weightF8Ee = weightF8Ee;
    }

    public Double getWeightF9Ee() {
        return weightF9Ee;
    }

    public void setWeightF9Ee(Double weightF9Ee) {
        this.weightF9Ee = weightF9Ee;
    }

    public Double getWeightF10Ee() {
        return weightF10Ee;
    }

    public void setWeightF10Ee(Double weightF10Ee) {
        this.weightF10Ee = weightF10Ee;
    }

    public Double getWeightF1Er() {
        return weightF1Er;
    }

    public void setWeightF1Er(Double weightF1Er) {
        this.weightF1Er = weightF1Er;
    }

    public Double getWeightF2Er() {
        return weightF2Er;
    }

    public void setWeightF2Er(Double weightF2Er) {
        this.weightF2Er = weightF2Er;
    }

    public Double getWeightF3Er() {
        return weightF3Er;
    }

    public void setWeightF3Er(Double weightF3Er) {
        this.weightF3Er = weightF3Er;
    }

    public Double getWeightF4Er() {
        return weightF4Er;
    }

    public void setWeightF4Er(Double weightF4Er) {
        this.weightF4Er = weightF4Er;
    }

    public Double getWeightF5Er() {
        return weightF5Er;
    }

    public void setWeightF5Er(Double weightF5Er) {
        this.weightF5Er = weightF5Er;
    }

    public Double getWeightF6Er() {
        return weightF6Er;
    }

    public void setWeightF6Er(Double weightF6Er) {
        this.weightF6Er = weightF6Er;
    }

    public Double getWeightF7Er() {
        return weightF7Er;
    }

    public void setWeightF7Er(Double weightF7Er) {
        this.weightF7Er = weightF7Er;
    }

    public Double getWeightF8Er() {
        return weightF8Er;
    }

    public void setWeightF8Er(Double weightF8Er) {
        this.weightF8Er = weightF8Er;
    }

    public Double getWeightF9Er() {
        return weightF9Er;
    }

    public void setWeightF9Er(Double weightF9Er) {
        this.weightF9Er = weightF9Er;
    }

    public Double getWeightF10Er() {
        return weightF10Er;
    }

    public void setWeightF10Er(Double weightF10Er) {
        this.weightF10Er = weightF10Er;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}