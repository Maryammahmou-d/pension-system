package com.rubix.pension.employee_company.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.ArrayList;
import java.util.List;

public class BulkTerminationRequest {

    private String companyNumber;
    private String userName;
    @JsonAlias("path")
    private String savePath;
    private List<BulkTerminationRow> rows = new ArrayList<>();

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** Optional server-side folder to also save the generated PDFs/Excel files into. May be null/blank. */
    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public List<BulkTerminationRow> getRows() {
        return rows;
    }

    @JsonAlias("employees")
    public void setRows(List<BulkTerminationRow> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }
}
