package com.rubix.pension.employee_company.repository;
import com.rubix.pension.employee_company.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    @Query("""
            SELECT COALESCE(MAX(e.employeeId), 0)
            FROM Employee e
            WHERE e.companyNumber = :companyNumber
            """)

    Integer findMaxEmployeeIdByCompanyNumber(
            @Param("companyNumber") String companyNumber);

    @Query(value = """
        SELECT DISTINCT ON ("Employee_ID") *
        FROM "Employees"
        WHERE "Company_Number" = :companyNumber
        ORDER BY "Employee_ID",
                 "Serial" DESC NULLS LAST,
                 "ID" DESC
        """,
            nativeQuery = true)
    List<Employee> findLatestEmployeesByCompanyNumber(
            @Param("companyNumber") String companyNumber);

    @Query(value = """
            SELECT DISTINCT ON ("Employee_Number") *
            FROM "Employees"
            WHERE "Company_Number" = :companyNumber
              AND "Employee_Number" = :employeeNumber
            ORDER BY "Employee_Number",
                     "Serial" DESC NULLS LAST,
                     "ID" DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<Employee> findLatestByCompanyAndEmployeeNumber(
            @Param("companyNumber") String companyNumber,
            @Param("employeeNumber") String employeeNumber);

    @Query(value = """
            SELECT DISTINCT ON ("Employee_Number") *
            FROM "Employees"
            WHERE "Employee_Number" = :employeeNumber
            ORDER BY "Employee_Number",
                     "Serial" DESC NULLS LAST,
                     "ID" DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<Employee> findLatestByEmployeeNumber(
            @Param("employeeNumber") String employeeNumber);

    @Query(value = """
            SELECT DISTINCT ON ("Employee_ID") *
            FROM "Employees"
            WHERE "Company_Number" = :companyNumber
              AND "Employee_ID" = :employeeId
            ORDER BY "Employee_ID",
                     "Serial" DESC NULLS LAST,
                     "ID" DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<Employee> findLatestByCompanyAndEmployeeId(
            @Param("companyNumber") String companyNumber,
            @Param("employeeId") Integer employeeId);
}
