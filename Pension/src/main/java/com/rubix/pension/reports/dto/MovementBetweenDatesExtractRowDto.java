package com.rubix.pension.reports.dto;

public class MovementBetweenDatesExtractRowDto {
    private String modifiedDate;
    private String paymentDate;
    private double[] up = new double[10];
    private double[] txEeUnits = new double[10];
    private double[] txVeeUnits = new double[10];
    private double[] txErUnits = new double[10];
    private double[] termErUnits = new double[10];
    private double[] txEeValue = new double[10];
    private double[] txVeeValue = new double[10];
    private double[] txErValue = new double[10];
    private double[] termErValue = new double[10];
    private double[] txTotUnits = new double[10];

    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public double[] getUp() { return up; }
    public void setUp(double[] up) { this.up = up; }

    public double[] getTxEeUnits() { return txEeUnits; }
    public void setTxEeUnits(double[] txEeUnits) { this.txEeUnits = txEeUnits; }

    public double[] getTxVeeUnits() { return txVeeUnits; }
    public void setTxVeeUnits(double[] txVeeUnits) { this.txVeeUnits = txVeeUnits; }

    public double[] getTxErUnits() { return txErUnits; }
    public void setTxErUnits(double[] txErUnits) { this.txErUnits = txErUnits; }

    public double[] getTermErUnits() { return termErUnits; }
    public void setTermErUnits(double[] termErUnits) { this.termErUnits = termErUnits; }

    public double[] getTxEeValue() { return txEeValue; }
    public void setTxEeValue(double[] txEeValue) { this.txEeValue = txEeValue; }

    public double[] getTxVeeValue() { return txVeeValue; }
    public void setTxVeeValue(double[] txVeeValue) { this.txVeeValue = txVeeValue; }

    public double[] getTxErValue() { return txErValue; }
    public void setTxErValue(double[] txErValue) { this.txErValue = txErValue; }

    public double[] getTermErValue() { return termErValue; }
    public void setTermErValue(double[] termErValue) { this.termErValue = termErValue; }

    public double[] getTxTotUnits() { return txTotUnits; }
    public void setTxTotUnits(double[] txTotUnits) { this.txTotUnits = txTotUnits; }
}
