package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.CreateContributionRequest;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Contribution;
import com.rubix.pension.employee_company.exception.CompanyNotFound;
import com.rubix.pension.employee_company.exception.ContributionNotFound;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.ContributionRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ContributionsService {

    private final ContributionRepository contributionRepository;
    private final CompanyRepository companyRepository;

    public ContributionsService(
            ContributionRepository contributionRepository,
            CompanyRepository companyRepository) {

        this.contributionRepository = contributionRepository;
        this.companyRepository = companyRepository;
    }


    public Contribution addNewContribution(
            CreateContributionRequest request) {

        Company company = companyRepository
                .findLatestByCompanyNumber(request.getCompanyNumber())
                .orElseThrow(() ->
                        new CompanyNotFound("Company Not Found")
                );

        if (contributionRepository
                .existsByCompanyNumberAndCategory(
                        request.getCompanyNumber(),
                        request.getCategory())) {

            throw new IllegalArgumentException(
                    "This category already exists for this company"
            );
        }

        Contribution contribution = new Contribution();

        contribution.setCompanyNumber(
                company.getCompanyNumber()
        );

        contribution.setModifiedDate(
                OffsetDateTime.now()
        );

        contribution.setCategory(
                request.getCategory()
        );

        contribution.setEE(
                request.getEE()
        );

        contribution.setER(
                request.getER()
        );

        return contributionRepository.save(contribution);
    }


    public List<Contribution> getContributions(
            String companyNumber) {

        return contributionRepository
                .findByCompanyNumber(companyNumber);
    }


    public Contribution updateContribution(
            CreateContributionRequest request) {

        Contribution contribution =
                contributionRepository
                        .findByCompanyNumberAndCategory(
                                request.getCompanyNumber(),
                                request.getCategory()
                        )
                        .orElseThrow(() ->
                                new ContributionNotFound(
                                        "Contribution Not Found"
                                )
                        );

        if (request.getEE() != null) {
            contribution.setEE(request.getEE());
        }

        if (request.getER() != null) {
            contribution.setER(request.getER());
        }

        contribution.setModifiedDate(
                OffsetDateTime.now()
        );

        return contributionRepository.save(contribution);
    }
}