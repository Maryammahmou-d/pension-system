package com.rubix.pension.financial_operations.service;

import com.rubix.pension.financial_operations.dto.VestingRuleDto;
import com.rubix.pension.financial_operations.entity.VestingRule;
import com.rubix.pension.financial_operations.repository.VestingRuleRepository;
import com.rubix.pension.financial_operations.support.FundHoldingsSupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class VestingRulesService {

    private final VestingRuleRepository vestingRuleRepository;

    public VestingRulesService(VestingRuleRepository vestingRuleRepository) {
        this.vestingRuleRepository = vestingRuleRepository;
    }

    public List<VestingRuleDto> list() {
        return vestingRuleRepository.findAllLatest().stream().map(this::toDto).toList();
    }

    public VestingRuleDto get(String companyNumber) {
        if (companyNumber == null || companyNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        return vestingRuleRepository.findLatestByCompanyNumber(companyNumber.trim())
                .map(this::toDto)
                .orElse(null);
    }

    public VestingRuleDto create(VestingRuleDto request) {
        VestingRuleDto validated = validate(request);
        String companyNumber = validated.getCompanyNumber();
        if (vestingRuleRepository.findLatestByCompanyNumber(companyNumber).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vesting rules already exist for this company.");
        }
        return toDto(vestingRuleRepository.save(fromDto(validated)));
    }

    public VestingRuleDto update(VestingRuleDto request) {
        VestingRuleDto validated = validate(request);
        String companyNumber = validated.getCompanyNumber();
        if (vestingRuleRepository.findLatestByCompanyNumber(companyNumber).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No vesting rules found for this company.");
        }
        return toDto(vestingRuleRepository.save(fromDto(validated)));
    }

    private VestingRuleDto validate(VestingRuleDto request) {
        if (request == null || request.getCompanyNumber() == null || request.getCompanyNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FundHoldingsSupport.REQUIRED_FIELDS_MESSAGE);
        }
        Double[] years = {
                request.getYear1(), request.getYear2(), request.getYear3(), request.getYear4(), request.getYear5(),
                request.getYear6(), request.getYear7(), request.getYear8(), request.getYear9(), request.getYear10()
        };
        for (int i = 0; i < 9; i += 1) {
            requirePercent(years[i], i + 1, true);
        }
        if (years[9] == null) {
            request.setYear10(100.0);
        } else {
            requirePercent(years[9], 10, false);
        }
        request.setCompanyNumber(request.getCompanyNumber().trim());
        return request;
    }

    private void requirePercent(Double value, int year, boolean required) {
        if (value == null) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year " + year + " is required.");
            }
            return;
        }
        if (value < 0 || value > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year " + year + " must be a percentage between 0 and 100.");
        }
    }

    private VestingRule fromDto(VestingRuleDto dto) {
        VestingRule rule = new VestingRule();
        rule.setCompanyNumber(dto.getCompanyNumber());
        rule.setModifiedDate(OffsetDateTime.now());
        rule.setYear1(dto.getYear1());
        rule.setYear2(dto.getYear2());
        rule.setYear3(dto.getYear3());
        rule.setYear4(dto.getYear4());
        rule.setYear5(dto.getYear5());
        rule.setYear6(dto.getYear6());
        rule.setYear7(dto.getYear7());
        rule.setYear8(dto.getYear8());
        rule.setYear9(dto.getYear9());
        rule.setYear10(dto.getYear10());
        return rule;
    }

    private VestingRuleDto toDto(VestingRule rule) {
        VestingRuleDto dto = new VestingRuleDto();
        dto.setCompanyNumber(rule.getCompanyNumber());
        dto.setYear1(nz(rule.getYear1()));
        dto.setYear2(nz(rule.getYear2()));
        dto.setYear3(nz(rule.getYear3()));
        dto.setYear4(nz(rule.getYear4()));
        dto.setYear5(nz(rule.getYear5()));
        dto.setYear6(nz(rule.getYear6()));
        dto.setYear7(nz(rule.getYear7()));
        dto.setYear8(nz(rule.getYear8()));
        dto.setYear9(nz(rule.getYear9()));
        dto.setYear10(nz(rule.getYear10()));
        return dto;
    }

    private double nz(Double value) {
        return value == null ? 0 : value;
    }
}
