package com.rubix.pension.employee_company.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalContributionsExceptionHandler {

    @ExceptionHandler(ContributionNotFound.class)
    public ResponseEntity<String> handelContributionNotFound(String message){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
}
