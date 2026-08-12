package com.rubix.pension.employee_company.repository;

import com.rubix.pension.employee_company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    @Query(value = """
            SELECT DISTINCT ON ("Company_Number") *
            FROM "Companies"
            WHERE "Company_Number" IS NOT NULL
            ORDER BY "Company_Number",
                     "Modified_Date" DESC NULLS LAST,
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
}
