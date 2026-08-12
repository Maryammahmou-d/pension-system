package com.rubix.pension.employee_company.controller;
import com.rubix.pension.employee_company.dto.CreateEmployeeRequest;
import com.rubix.pension.employee_company.dto.EmployeeNumberResponse;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.service.EmployeeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(@RequestBody EmployeeService employeeService){
        this.employeeService=employeeService;
    }

    @PostMapping
    public Employee addNewEmployee(@RequestBody CreateEmployeeRequest request){
        return employeeService.addNewEmployee(request);
    }

    @GetMapping("/next-number/{companyNumber}")
    public EmployeeNumberResponse getNextEmployeeNumber(
            @PathVariable String companyNumber) {

        return employeeService.getNextEmployeeNumber(companyNumber);
    }
}
