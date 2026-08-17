package com.rubix.pension.financial_operations.dto;

import java.util.List;

public class NetUnitsResult {

    private String valuationDate;
    private String dateFinal;
    private List<FundUnitRow> rows;
    private double totalEEUnits;
    private double totalVEEUnits;
    private double totalERUnits;
    private double totalUnits;

    public String getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(String valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getDateFinal() {
        return dateFinal;
    }

    public void setDateFinal(String dateFinal) {
        this.dateFinal = dateFinal;
    }

    public List<FundUnitRow> getRows() {
        return rows;
    }

    public void setRows(List<FundUnitRow> rows) {
        this.rows = rows;
    }

    public double getTotalEEUnits() {
        return totalEEUnits;
    }

    public void setTotalEEUnits(double totalEEUnits) {
        this.totalEEUnits = totalEEUnits;
    }

    public double getTotalVEEUnits() {
        return totalVEEUnits;
    }

    public void setTotalVEEUnits(double totalVEEUnits) {
        this.totalVEEUnits = totalVEEUnits;
    }

    public double getTotalERUnits() {
        return totalERUnits;
    }

    public void setTotalERUnits(double totalERUnits) {
        this.totalERUnits = totalERUnits;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(double totalUnits) {
        this.totalUnits = totalUnits;
    }
}
