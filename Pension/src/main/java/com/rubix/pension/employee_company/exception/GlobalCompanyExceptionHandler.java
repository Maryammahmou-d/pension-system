package com.rubix.pension.employee_company.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalCompanyExceptionHandler {

    @ExceptionHandler(CompanyNotFound.class)
    public ResponseEntity<String> handelComapnyNotFound(String message){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
}
