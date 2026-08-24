package com.rubix.pension.reports.dto;

import java.util.List;

public class AggregatedBalanceReportResponse {

    private String companyNumber;
    private String valuationDate;
    private String currency;
    private List<AggregatedBalanceRowDto> rows;
    private AggregatedBalanceTotalsDto totals;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(String valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<AggregatedBalanceRowDto> getRows() {
        return rows;
    }

    public void setRows(List<AggregatedBalanceRowDto> rows) {
        this.rows = rows;
    }

    public AggregatedBalanceTotalsDto getTotals() {
        return totals;
    }

    public void setTotals(AggregatedBalanceTotalsDto totals) {
        this.totals = totals;
    }
}
