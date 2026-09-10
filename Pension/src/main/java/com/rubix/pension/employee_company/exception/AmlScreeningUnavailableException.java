package com.rubix.pension.employee_company.exception;

public class AmlScreeningUnavailableException extends RuntimeException {
    public AmlScreeningUnavailableException(String message) {
        super(message);
    }

    public AmlScreeningUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
