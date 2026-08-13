package com.rubix.pension.employee_company.repository;

import com.rubix.pension.employee_company.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository
        extends JpaRepository<Contribution, Integer> {

    List<Contribution> findByCompanyNumber(String companyNumber);

    Optional<Contribution> findByCompanyNumberAndCategory(
            String companyNumber,
            String category
    );

    boolean existsByCompanyNumberAndCategory( String companyNumber,String category);
}