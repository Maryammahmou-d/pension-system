package com.rubix.pension.employee_company.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalEmployeeExceptionHandler {

    @ExceptionHandler(EmployeeNotFound.class)
    public ResponseEntity<String> handelEmployeeNotFound(String message){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
}
