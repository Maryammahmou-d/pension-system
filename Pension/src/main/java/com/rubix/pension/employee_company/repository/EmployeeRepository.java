package com.rubix.pension.employee_company.repository;
import com.rubix.pension.employee_company.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}
