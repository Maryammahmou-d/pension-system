package com.rubix.pension.employee_company.service;

import com.rubix.pension.AML.Response.AmlCheckResponse;
import com.rubix.pension.AML.Service.AmlClient;
import com.rubix.pension.employee_company.dto.CreateEmployeeRequest;
import com.rubix.pension.employee_company.dto.CreateEmployeeResponse;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AmlClient amlClient;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void addNewEmployee_callsAmlScreeningAndSavesEmployee() {
        Company company = new Company();
        company.setCompanyNumber("C123");

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setCompanyId(1);
        request.setFullName("John Doe");
        request.setNationalId("123456789");

        Employee saved = new Employee();
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(employeeRepository.findMaxEmployeeIdByCompanyNumber("C123")).thenReturn(0);
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        AmlCheckResponse amlResponse = new AmlCheckResponse();
        amlResponse.setStatus("CLEAR");
        amlResponse.setTotalMatches(0);
        when(amlClient.screenEmployee("John Doe", "123456789")).thenReturn(amlResponse);

        CreateEmployeeResponse result = employeeService.addNewEmployee(request);

        assertNotNull(result);
        assertNotNull(result.getEmployee());
        assertNotNull(result.getAmlResult());
        assertEquals("CLEAR", result.getAmlResult().getStatus());
        verify(amlClient).screenEmployee("John Doe", "123456789");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void addNewEmployee_continuesToSaveWhenAmlFails() {
        Company company = new Company();
        company.setCompanyNumber("C123");

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setCompanyId(1);
        request.setFullName("Jane Doe");
        request.setNationalId("987654321");

        Employee saved = new Employee();
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(employeeRepository.findMaxEmployeeIdByCompanyNumber("C123")).thenReturn(0);
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);
        when(amlClient.screenEmployee("Jane Doe", "987654321"))
                .thenThrow(new RuntimeException("AML service unavailable"));

        CreateEmployeeResponse result = employeeService.addNewEmployee(request);

        assertNotNull(result);
        assertNotNull(result.getEmployee());
        assertNull(result.getAmlResult());
        verify(amlClient).screenEmployee("Jane Doe", "987654321");
        verify(employeeRepository).save(any(Employee.class));
    }
}
