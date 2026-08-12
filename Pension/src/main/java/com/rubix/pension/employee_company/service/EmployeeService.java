package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.CreateEmployeeRequest;
import com.rubix.pension.employee_company.dto.EmployeeNumberResponse;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.exception.CompanyNotFound;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            CompanyRepository companyRepository) {

        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }


    public Employee addNewEmployee(CreateEmployeeRequest request) {

        Company company = companyRepository
                .findById(request.getCompanyId())
                .orElseThrow(() ->
                        new CompanyNotFound("Company Not Found")
                );


        String companyNumber = company.getCompanyNumber();


        Integer maxEmployeeId =
                employeeRepository.findMaxEmployeeIdByCompanyNumber(
                        companyNumber
                );

        Integer newEmployeeId = maxEmployeeId + 1;


        Employee employee = new Employee();


        employee.setCompanyNumber(companyNumber);

        employee.setEmployeeId(newEmployeeId);

        employee.setEmployeeNumber(
                companyNumber + "_" + newEmployeeId
        );


        employee.setNationalId(
                request.getNationalId()
        );

        employee.setFullName(
                request.getFullName()
        );

        employee.setDob(
                request.getDob()
        );

        employee.setGender(
                request.getGender()
        );

        employee.setOccupation(
                request.getOccupation()
        );

        employee.setHireDate(
                request.getHireDate()
        );

        employee.setAgeAtHire(
                request.getAgeAtHire()
        );

        employee.setPensionStartDate(
                request.getPensionStartDate()
        );

        employee.setKafJoiningDate(
                request.getKafJoiningDate()
        );

        employee.setCategory(
                request.getCategory()
        );



        employee.setGrossSalary(
                request.getGrossSalary()
        );

        employee.setSalaryCurrency(
                request.getSalaryCurrency()
        );

        employee.setContributionEe(
                request.getContributionEe()
        );

        employee.setContributionEr(
                request.getContributionEr()
        );

        employee.setEmail(
                request.getEmail()
        );


        employee.setStartingEeValue(
                request.getStartingEeValue()
        );

        employee.setStartingErValue(
                request.getStartingErValue()
        );

        employee.setStartingFundValue(
                request.getStartingFundValue()
        );


        employee.setTerminationDate(
                request.getTerminationDate()
        );

        employee.setResignationDate(
                request.getResignationDate()
        );

        employee.setVee(
                request.getVee()
        );



        employee.setWeightF1Ee(request.getWeightF1Ee());
        employee.setWeightF2Ee(request.getWeightF2Ee());
        employee.setWeightF3Ee(request.getWeightF3Ee());
        employee.setWeightF4Ee(request.getWeightF4Ee());
        employee.setWeightF5Ee(request.getWeightF5Ee());
        employee.setWeightF6Ee(request.getWeightF6Ee());
        employee.setWeightF7Ee(request.getWeightF7Ee());
        employee.setWeightF8Ee(request.getWeightF8Ee());
        employee.setWeightF9Ee(request.getWeightF9Ee());
        employee.setWeightF10Ee(request.getWeightF10Ee());


        employee.setWeightF1Er(request.getWeightF1Er());
        employee.setWeightF2Er(request.getWeightF2Er());
        employee.setWeightF3Er(request.getWeightF3Er());
        employee.setWeightF4Er(request.getWeightF4Er());
        employee.setWeightF5Er(request.getWeightF5Er());
        employee.setWeightF6Er(request.getWeightF6Er());
        employee.setWeightF7Er(request.getWeightF7Er());
        employee.setWeightF8Er(request.getWeightF8Er());
        employee.setWeightF9Er(request.getWeightF9Er());
        employee.setWeightF10Er(request.getWeightF10Er());


        employee.setModifiedDate(
                OffsetDateTime.now()
        );

        return employeeRepository.save(employee);
    }

    public EmployeeNumberResponse getNextEmployeeNumber(
            String companyNumber) {

        Integer maxEmployeeId =
                employeeRepository.findMaxEmployeeIdByCompanyNumber(
                        companyNumber
                );

        Integer nextEmployeeId = maxEmployeeId + 1;

        String employeeNumber =
                companyNumber + "_" + nextEmployeeId;

        return new EmployeeNumberResponse(
                nextEmployeeId,
                employeeNumber
        );
    }


}