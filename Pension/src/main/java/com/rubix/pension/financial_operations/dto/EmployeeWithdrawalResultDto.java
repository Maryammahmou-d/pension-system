package com.rubix.pension.financial_operations.dto;

public class EmployeeWithdrawalResultDto {

    private String reference;
    private String message;

    public EmployeeWithdrawalResultDto() {
    }

    public EmployeeWithdrawalResultDto(String reference, String message) {
        this.reference = reference;
        this.message = message;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
