package com.rubix.pension.financial_operations.repository;

import com.rubix.pension.financial_operations.entity.VestingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VestingRuleRepository extends JpaRepository<VestingRule, Integer> {

    @Query(value = """
            SELECT DISTINCT ON ("Company_Number") *
            FROM "Vesting"
            WHERE "Company_Number" IS NOT NULL
            ORDER BY "Company_Number", "ID" DESC
            """,
            nativeQuery = true)
    List<VestingRule> findAllLatest();

    @Query(value = """
            SELECT *
            FROM "Vesting"
            WHERE "Company_Number" = :companyNumber
            ORDER BY "ID" DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<VestingRule> findLatestByCompanyNumber(@Param("companyNumber") String companyNumber);
}
