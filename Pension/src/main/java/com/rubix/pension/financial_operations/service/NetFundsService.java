package com.rubix.pension.financial_operations.service;

import com.rubix.pension.financial_operations.dto.NetFundsResult;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.FundNetBuildResult;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport.UnitSums;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class NetFundsService {

    private final FundHoldingsSupport fundHoldingsSupport;

    public NetFundsService(FundHoldingsSupport fundHoldingsSupport) {
        this.fundHoldingsSupport = fundHoldingsSupport;
    }

    public NetFundsResult calculate(String valuationDate) {
        fundHoldingsSupport.requireNonBlank(valuationDate);
        LocalDate targetDate = fundHoldingsSupport.parseRequiredDate(valuationDate);

        UnitPrice unitPrice = fundHoldingsSupport.requireUnitPrice(targetDate);
        List<Map<String, Object>> transactionRows = fundHoldingsSupport.fetchLatestHoldings(targetDate);

        UnitSums sums = fundHoldingsSupport.aggregateUnits(transactionRows, row -> true);
        FundNetBuildResult build = fundHoldingsSupport.buildFundNetSummaryRows(sums, unitPrice);

        NetFundsResult result = new NetFundsResult();
        result.setValuationDate(targetDate.toString());
        result.setDateFinal(sums.hasTransactions() ? targetDate.toString() : null);
        result.setRows(build.rows());
        result.setTotalEEFunds(build.totalEEFunds());
        result.setTotalVEEFunds(build.totalVEEFunds());
        result.setTotalERFunds(build.totalERFunds());
        result.setTotalFunds(build.totalFunds());
        return result;
    }
}
