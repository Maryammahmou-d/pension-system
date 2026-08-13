package com.rubix.pension.employee_company.controller;

import com.rubix.pension.employee_company.dto.CreateContributionRequest;
import com.rubix.pension.employee_company.entity.Contribution;
import com.rubix.pension.employee_company.service.ContributionsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contribution")
public class ContributionController {

    private final ContributionsService contributionsService;

    public ContributionController(
            ContributionsService contributionsService) {

        this.contributionsService = contributionsService;
    }


    @PostMapping
    public Contribution addNewContribution(
            @RequestBody CreateContributionRequest request) {

        return contributionsService.addNewContribution(request);
    }


    @GetMapping("/{companyNumber}")
    public List<Contribution> getContributions(
            @PathVariable String companyNumber) {

        return contributionsService.getContributions(companyNumber);
    }


    @PutMapping("/update")
    public Contribution updateContribution(
            @RequestBody CreateContributionRequest request) {

        return contributionsService.updateContribution(request);
    }
}