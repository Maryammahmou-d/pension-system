package com.rubix.pension.employee_company.dto;

import com.rubix.pension.AML.Response.AmlCheckResponse;
import com.rubix.pension.employee_company.entity.Employee;

public class CreateEmployeeResponse {

    private Employee employee;
    private AmlCheckResponse amlResult;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public AmlCheckResponse getAmlResult() {
        return amlResult;
    }

    public void setAmlResult(AmlCheckResponse amlResult) {
        this.amlResult = amlResult;
    }
}
