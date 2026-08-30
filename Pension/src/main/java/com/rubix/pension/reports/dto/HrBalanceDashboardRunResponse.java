package com.rubix.pension.reports.dto;

import java.util.List;

public class HrBalanceDashboardRunResponse {

    private String valuationDate;
    private int hrDashboardRows;
    private int hrDashboardCountsRows;
    private int hrDashboardMonthlyRows;
    private int unitPriceAnnualizedGainRows;
    private List<String> previousRuns;

    public String getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(String valuationDate) {
        this.valuationDate = valuationDate;
    }

    public int getHrDashboardRows() {
        return hrDashboardRows;
    }

    public void setHrDashboardRows(int hrDashboardRows) {
        this.hrDashboardRows = hrDashboardRows;
    }

    public int getHrDashboardCountsRows() {
        return hrDashboardCountsRows;
    }

    public void setHrDashboardCountsRows(int hrDashboardCountsRows) {
        this.hrDashboardCountsRows = hrDashboardCountsRows;
    }

    public int getHrDashboardMonthlyRows() {
        return hrDashboardMonthlyRows;
    }

    public void setHrDashboardMonthlyRows(int hrDashboardMonthlyRows) {
        this.hrDashboardMonthlyRows = hrDashboardMonthlyRows;
    }

    public int getUnitPriceAnnualizedGainRows() {
        return unitPriceAnnualizedGainRows;
    }

    public void setUnitPriceAnnualizedGainRows(int unitPriceAnnualizedGainRows) {
        this.unitPriceAnnualizedGainRows = unitPriceAnnualizedGainRows;
    }

    public List<String> getPreviousRuns() {
        return previousRuns;
    }

    public void setPreviousRuns(List<String> previousRuns) {
        this.previousRuns = previousRuns;
    }
}
