package com.rubix.pension.reports.dto;

public class MovementBetweenDatesBalanceResponse {
    private String startDate;
    private String endDate;
    private DateBalanceBlockDto startDateBalance;
    private DateBalanceBlockDto endDateBalance;

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public DateBalanceBlockDto getStartDateBalance() { return startDateBalance; }
    public void setStartDateBalance(DateBalanceBlockDto startDateBalance) { this.startDateBalance = startDateBalance; }

    public DateBalanceBlockDto getEndDateBalance() { return endDateBalance; }
    public void setEndDateBalance(DateBalanceBlockDto endDateBalance) { this.endDateBalance = endDateBalance; }
}
