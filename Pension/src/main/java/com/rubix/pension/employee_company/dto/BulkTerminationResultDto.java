package com.rubix.pension.employee_company.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkTerminationResultDto {

    private String companyNumber;
    private String companyName;
    private String summaryFileName;
    private String message;
    private int totalRows;
    private int terminatedCount;
    private List<TerminationReportDto> reports = new ArrayList<>();
    private List<TerminationSummaryRowDto> summaryRows = new ArrayList<>();
    private List<TerminationFundReportDto> fundReports = new ArrayList<>();
    private double grandTotalTransactionalEeValue;
    private double grandTotalTransactionalVeeValue;
    private double grandTotalTransactionalErValue;
    private double grandTotalTerminatedErValue;
    private double grandTotalTransactionalValue;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSummaryFileName() {
        return summaryFileName;
    }

    public void setSummaryFileName(String summaryFileName) {
        this.summaryFileName = summaryFileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTerminatedCount() {
        return terminatedCount;
    }

    public void setTerminatedCount(int terminatedCount) {
        this.terminatedCount = terminatedCount;
    }

    public List<TerminationReportDto> getReports() {
        return reports;
    }

    public void setReports(List<TerminationReportDto> reports) {
        this.reports = reports == null ? new ArrayList<>() : reports;
    }

    public List<TerminationSummaryRowDto> getSummaryRows() {
        return summaryRows;
    }

    public void setSummaryRows(List<TerminationSummaryRowDto> summaryRows) {
        this.summaryRows = summaryRows == null ? new ArrayList<>() : summaryRows;
    }

    public List<TerminationFundReportDto> getFundReports() {
        return fundReports;
    }

    public void setFundReports(List<TerminationFundReportDto> fundReports) {
        this.fundReports = fundReports == null ? new ArrayList<>() : fundReports;
    }

    public double getGrandTotalTransactionalEeValue() {
        return grandTotalTransactionalEeValue;
    }

    public void setGrandTotalTransactionalEeValue(double grandTotalTransactionalEeValue) {
        this.grandTotalTransactionalEeValue = grandTotalTransactionalEeValue;
    }

    public double getGrandTotalTransactionalVeeValue() {
        return grandTotalTransactionalVeeValue;
    }

    public void setGrandTotalTransactionalVeeValue(double grandTotalTransactionalVeeValue) {
        this.grandTotalTransactionalVeeValue = grandTotalTransactionalVeeValue;
    }

    public double getGrandTotalTransactionalErValue() {
        return grandTotalTransactionalErValue;
    }

    public void setGrandTotalTransactionalErValue(double grandTotalTransactionalErValue) {
        this.grandTotalTransactionalErValue = grandTotalTransactionalErValue;
    }

    public double getGrandTotalTerminatedErValue() {
        return grandTotalTerminatedErValue;
    }

    public void setGrandTotalTerminatedErValue(double grandTotalTerminatedErValue) {
        this.grandTotalTerminatedErValue = grandTotalTerminatedErValue;
    }

    public double getGrandTotalTransactionalValue() {
        return grandTotalTransactionalValue;
    }

    public void setGrandTotalTransactionalValue(double grandTotalTransactionalValue) {
        this.grandTotalTransactionalValue = grandTotalTransactionalValue;
    }
}
