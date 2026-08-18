package com.rubix.pension.financial_operations.repository;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitPriceRepository extends JpaRepository<UnitPrice,Integer> {

    List<UnitPrice> findAllByOrderByPriceDateDesc();
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

    @Query("""
        SELECT u
        FROM UnitPrice u
        WHERE u.priceDate >= :start
          AND u.priceDate < :end
        ORDER BY u.id DESC
        """)
    Optional<UnitPrice> findLatestByPriceDate(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM "UnitPrice"
        WHERE CAST("PriceDate" AS date) = :priceDate
           OR ("PriceDate" AT TIME ZONE 'UTC')::date = :priceDate
           OR ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = :priceDate
        """, nativeQuery = true)
    long countByCalendarDate(@Param("priceDate") LocalDate priceDate);

    @Query(value = """
        SELECT *
        FROM "UnitPrice"
        WHERE CAST("PriceDate" AS date) = :priceDate
           OR ("PriceDate" AT TIME ZONE 'UTC')::date = :priceDate
           OR ("PriceDate" AT TIME ZONE 'Asia/Kuwait')::date = :priceDate
        ORDER BY "ID" DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<UnitPrice> findLatestByCalendarDate(@Param("priceDate") LocalDate priceDate);
}
