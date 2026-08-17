package com.rubix.pension.financial_operations.dto;

import java.util.ArrayList;
import java.util.List;

public class EmployeeWithdrawalRequest {

    private String companyNumber;
    private String employeeNumber;
    private String withdrawalDate;
    private List<WithdrawalAmountDto> amounts = new ArrayList<>();

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(String withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public List<WithdrawalAmountDto> getAmounts() {
        return amounts;
    }

    public void setAmounts(List<WithdrawalAmountDto> amounts) {
        this.amounts = amounts == null ? new ArrayList<>() : amounts;
    }
}
