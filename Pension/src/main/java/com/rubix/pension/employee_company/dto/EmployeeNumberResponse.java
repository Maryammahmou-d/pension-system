package com.rubix.pension.employee_company.dto;

public class EmployeeNumberResponse {

    private Integer employeeId;
    private String employeeNumber;

    public EmployeeNumberResponse(
            Integer employeeId,
            String employeeNumber) {

        this.employeeId = employeeId;
        this.employeeNumber = employeeNumber;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }
}