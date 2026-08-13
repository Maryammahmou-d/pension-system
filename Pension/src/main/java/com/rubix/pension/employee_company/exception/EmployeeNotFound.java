package com.rubix.pension.employee_company.exception;

public class EmployeeNotFound extends RuntimeException{
    public EmployeeNotFound(String message){
        super(message);
    }
}
