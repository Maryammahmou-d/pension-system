package com.rubix.pension.employee_company.repository;
import com.rubix.pension.employee_company.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    @Query("""
            SELECT COALESCE(MAX(e.employeeId), 0)
            FROM Employee e
            WHERE e.companyNumber = :companyNumber
            """)
    Integer findMaxEmployeeIdByCompanyNumber(
            @Param("companyNumber") String companyNumber);
}
