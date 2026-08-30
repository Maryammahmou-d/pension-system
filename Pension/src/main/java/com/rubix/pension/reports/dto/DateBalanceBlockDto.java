package com.rubix.pension.reports.dto;

import java.util.List;

public class DateBalanceBlockDto {
    private String date;
    private List<MovementBetweenDatesBalanceRowDto> rows;
    private MovementBetweenDatesBalanceRowDto totals;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<MovementBetweenDatesBalanceRowDto> getRows() { return rows; }
    public void setRows(List<MovementBetweenDatesBalanceRowDto> rows) { this.rows = rows; }

    public MovementBetweenDatesBalanceRowDto getTotals() { return totals; }
    public void setTotals(MovementBetweenDatesBalanceRowDto totals) { this.totals = totals; }
}
