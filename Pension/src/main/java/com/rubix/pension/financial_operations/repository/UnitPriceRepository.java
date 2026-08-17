package com.rubix.pension.financial_operations.repository;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface UnitPriceRepository extends JpaRepository<UnitPrice,Integer> {
    @Query("""
        SELECT COUNT(u)
        FROM UnitPrice u
        WHERE u.priceDate >= :start
          AND u.priceDate < :end
        """)
    long countByPriceDate(
            @Param("start")OffsetDateTime start,
            @Param("end") OffsetDateTime end
            );
}
