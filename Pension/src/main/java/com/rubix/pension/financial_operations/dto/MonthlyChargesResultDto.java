package com.rubix.pension.financial_operations.dto;

import java.util.List;

public class MonthlyChargesResultDto {

    private MonthlyChargeRunDto run;
    private List<MonthlyChargeRunDto> previousRuns;

    public MonthlyChargeRunDto getRun() {
        return run;
    }

    public void setRun(MonthlyChargeRunDto run) {
        this.run = run;
    }

    public List<MonthlyChargeRunDto> getPreviousRuns() {
        return previousRuns;
    }

    public void setPreviousRuns(List<MonthlyChargeRunDto> previousRuns) {
        this.previousRuns = previousRuns;
    }
}
