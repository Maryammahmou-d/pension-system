package com.rubix.pension.employee_company.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class TempEmpTerminationsRepository {

    private static final String INSERT_SQL = """
            INSERT INTO "TempEmpTerminations" ("Employee_Number", "Termination_Date", "Resignation_Date")
            VALUES (:employeeNumber, :terminationDate, :resignationDate)
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TempEmpTerminationsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String employeeNumber, OffsetDateTime terminationDate, OffsetDateTime resignationDate) {
        jdbcTemplate.update(
                INSERT_SQL,
                new MapSqlParameterSource()
                        .addValue("employeeNumber", employeeNumber)
                        .addValue("terminationDate", terminationDate)
                        .addValue("resignationDate", resignationDate)
        );
    }
}
