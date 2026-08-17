package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.VestingRuleDto;
import com.rubix.pension.financial_operations.service.VestingRulesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vesting-rules")
public class VestingRulesController {

    private final VestingRulesService vestingRulesService;

    public VestingRulesController(VestingRulesService vestingRulesService) {
        this.vestingRulesService = vestingRulesService;
    }

    @GetMapping
    public List<VestingRuleDto> list() {
        return vestingRulesService.list();
    }

    @GetMapping("/{companyNumber}")
    public VestingRuleDto get(@PathVariable String companyNumber) {
        return vestingRulesService.get(companyNumber);
    }

    @PostMapping
    public VestingRuleDto create(@RequestBody VestingRuleDto request) {
        return vestingRulesService.create(request);
    }

    @PutMapping
    public VestingRuleDto update(@RequestBody VestingRuleDto request) {
        return vestingRulesService.update(request);
    }
}
