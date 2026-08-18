package com.rubix.pension.financial_operations.service;

import com.rubix.pension.financial_operations.dto.NetUnitsResult;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.FundUnitsBuildResult;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.UnitSums;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class NetUnitsService {

    private final FundHoldingsSupport fundHoldingsSupport;

    public NetUnitsService(FundHoldingsSupport fundHoldingsSupport) {
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    public NetUnitsResult calculate(String valuationDate) {
        fundHoldingsSupport.requireNonBlank(valuationDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(valuationDate);

        fundHoldingsSupport.requireUnitPrice(targetDate);
        List<Map<String, Object>> transactionRows = fundHoldingsSupport.fetchLatestHoldings(targetDate);

        UnitSums sums = fundHoldingsSupport.aggregateUnits(transactionRows, row -> true);
        FundUnitsBuildResult build = fundHoldingsSupport.buildFundUnitRows(sums);

        NetUnitsResult result = new NetUnitsResult();
        result.setValuationDate(targetDate.toString());
        result.setDateFinal(sums.hasTransactions() ? targetDate.toString() : null);
        result.setRows(build.rows());
        result.setTotalEEUnits(build.totalEEUnits());
        result.setTotalVEEUnits(build.totalVEEUnits());
        result.setTotalERUnits(build.totalERUnits());
        result.setTotalUnits(build.totalUnits());
        return result;
    }
}
