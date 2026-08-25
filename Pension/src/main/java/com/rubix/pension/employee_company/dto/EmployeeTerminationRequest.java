package com.rubix.pension.employee_company.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class EmployeeTerminationRequest {

    private String companyNumber;
    private String employeeNumber;
    private String terminationDate;
    private String resignationDate;
    private String userName;
    @JsonAlias("path")
    private String savePath;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** Optional server-side folder to also save the generated PDF into. May be null/blank. */
    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}
