package com.rubix.pension.financial_operations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobelUnitPriceExceptionHandler {

    @ExceptionHandler(UnitPriceAlreadyUpdatedOnThatDateException.class)
    public ResponseEntity<String> handelUnitPriceAlreadyUpdatedOnThatDateException(String message){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(AllFieldsRequiredException.class)
    public ResponseEntity<String> handelAllFieldsRequiredException(String message){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

}
