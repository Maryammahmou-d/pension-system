package com.rubix.pension.employee_company.exception;

public class CompanyNotFound extends RuntimeException {

    public CompanyNotFound(String message){
        super(message);
    }
}
