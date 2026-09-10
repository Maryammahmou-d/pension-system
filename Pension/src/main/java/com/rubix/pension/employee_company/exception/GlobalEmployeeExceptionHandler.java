package com.rubix.pension.employee_company.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalEmployeeExceptionHandler {

    @ExceptionHandler(EmployeeNotFound.class)
    public ResponseEntity<String> handelEmployeeNotFound(EmployeeNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(EmployeeFlaggedException.class)
    public ResponseEntity<Map<String, String>> handleEmployeeFlagged(EmployeeFlaggedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage(), "amlStatus", "FLAGGED"));
    }

    @ExceptionHandler(AmlScreeningUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleAmlScreeningUnavailable(AmlScreeningUnavailableException ex) {
        String details = ex.getCause() != null ? ex.getCause().getMessage() : null;
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "details", details != null ? details : "unknown"
                ));
    }
}
