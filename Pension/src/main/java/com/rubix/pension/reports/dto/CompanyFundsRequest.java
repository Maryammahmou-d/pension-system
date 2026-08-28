package com.rubix.pension.reports.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CompanyFundsRequest {

    @JsonAlias({"valuation_date", "Valuation_Date", "ValuationDate", "date", "valDate", "ValDate"})
    private String valuationDate;

    @JsonAlias({"output_path", "OutputPath", "path", "url", "URL"})
    private String outputPath;

    public String getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(String valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
