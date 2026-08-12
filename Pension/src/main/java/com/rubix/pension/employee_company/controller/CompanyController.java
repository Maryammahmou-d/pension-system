package com.rubix.pension.employee_company.controller;

import com.rubix.pension.employee_company.dto.CreateCompanyRequest;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService=companyService;
    }

    @PostMapping
    public Company addNewCompany(@RequestBody CreateCompanyRequest request){
        return companyService.addNewCompany(request);
    }

    @GetMapping("/latest")
    public List<Company> getLatestCompanies(){
        return companyService.getLatestCompanies();
    }

    @PutMapping("/{id}")
    public Company updateCompany(@PathVariable Integer id, @RequestBody CreateCompanyRequest request){
        return companyService.updateCompany(id, request);
    }


}
