package com.rubix.pension.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseIndexInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseIndexInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexesIfNotExist() {
        log.info("Checking performance indexes for dashboard and charge previous runs...");
        try {
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_hrdashboard_counts_valdate 
                ON "HRDashboard Counts" ("Valuation_Date");
            """);

            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_hrdashboard_counts_valdate_date 
                ON "HRDashboard Counts" ((("Valuation_Date" AT TIME ZONE 'Asia/Kuwait')::date));
            """);

            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_transactions_monthly_charges 
                ON "Transactions" (("Payment_Date"::date)) 
                WHERE "Description" = 'Monthly Charges';
            """);

            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_unitprice_pricedate 
                ON "UnitPrice" ("PriceDate");
            """);

            log.info("Performance indexes initialized successfully.");
        } catch (Exception e) {
            log.warn("Failed to create one or more performance indexes: {}", e.getMessage());
        }
    }
}
