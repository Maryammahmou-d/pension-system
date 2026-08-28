package com.rubix.pension.reports.dto;

public class CompanyFundsExtractResponse {

    private String filePath;
    private int recordCount;

    public CompanyFundsExtractResponse() {
    }

    public CompanyFundsExtractResponse(String filePath, int recordCount) {
        this.filePath = filePath;
        this.recordCount = recordCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
}
