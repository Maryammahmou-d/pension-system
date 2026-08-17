package com.rubix.pension.financial_operations.support;

import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.financial_operations.entity.VestingRule;
import com.rubix.pension.financial_operations.repository.VestingRuleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class VestingSupport {

    private final VestingRuleRepository vestingRuleRepository;

    public VestingSupport(VestingRuleRepository vestingRuleRepository) {
        this.vestingRuleRepository = vestingRuleRepository;
    }

    public double vestingFraction(String companyNumber, Employee employee, LocalDate asOf) {
        VestingRule rule = vestingRuleRepository.findLatestByCompanyNumber(companyNumber).orElse(null);
        if (rule == null) {
            return 0;
        }
        int years = yearsOfService(employee, asOf);
        Double percent = rule.yearPercent(years);
        return percent == null ? 0 : percent / 100.0;
    }

    public double vestingPercent(String companyNumber, Employee employee, LocalDate asOf) {
        return vestingFraction(companyNumber, employee, asOf) * 100.0;
    }

    public int yearsOfService(Employee employee, LocalDate asOf) {
        LocalDate start = firstDate(employee.getPensionStartDate(), employee.getKafJoiningDate(), employee.getHireDate());
        if (start == null) {
            return 0;
        }
        long years = ChronoUnit.YEARS.between(start, asOf);
        return (int) Math.max(years, 0);
    }

    private LocalDate firstDate(OffsetDateTime... values) {
        for (OffsetDateTime value : values) {
            if (value != null) {
                return value.toLocalDate();
            }
        }
        return null;
    }
}
