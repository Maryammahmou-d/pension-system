package com.rubix.pension.employee_company.repository;

import com.rubix.pension.employee_company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    @Query(value = """
            SELECT DISTINCT ON ("Company_Number") *
            FROM "Companies"
            WHERE "Company_Number" IS NOT NULL
            ORDER BY "Company_Number",
                     "Serial" DESC NULLS LAST,
                     "ID" DESC
            """,
            nativeQuery = true)
    List<Company> findLatestCompanies();


    @Query(value = """
        SELECT "Company_Number"
        FROM "Companies"
        WHERE "Company_Number" IS NOT NULL
        ORDER BY CAST(SUBSTRING("Company_Number" FROM 2) AS INTEGER) DESC
        LIMIT 1
        """,
            nativeQuery = true)
    String findLastCompanyNumber();

    @Query(value = """
        SELECT *
        FROM "Companies"
        WHERE "Company_Number" = :companyNumber
        ORDER BY "Serial" DESC NULLS LAST,
                 "ID" DESC
        LIMIT 1
        """,
            nativeQuery = true)
    Optional<Company> findLatestByCompanyNumber(
            @Param("companyNumber") String companyNumber
    );


}
