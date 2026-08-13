package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.CreateEmployeeRequest;
import com.rubix.pension.employee_company.dto.EmployeeNumberResponse;
import com.rubix.pension.employee_company.entity.Employee;
import com.rubix.pension.employee_company.exception.EmployeeNotFound;
import com.rubix.pension.employee_company.repository.EmployeeRepository;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import com.rubix.pension.employee_company.exception.CompanyNotFound;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

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

    public List<Employee> getEmployeesByCompanyNumber(String companyNumber) {

        return employeeRepository
                .findLatestEmployeesByCompanyNumber(companyNumber);
    }

    public Employee getEmployee(Integer id) {

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFound("Employee Not Found"));
    }

    public Employee updateEmployee(Integer id, CreateEmployeeRequest request) {

        Employee oldEmployee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFound("Employee Not Found")
                );

        Employee newEmployee = new Employee();

        // Keep employee identity information
        newEmployee.setCompanyNumber(oldEmployee.getCompanyNumber());
        newEmployee.setEmployeeId(oldEmployee.getEmployeeId());
        newEmployee.setEmployeeNumber(oldEmployee.getEmployeeNumber());
        newEmployee.setKafJoiningDate(oldEmployee.getKafJoiningDate());


        if (request.getNationalId() != null &&
                !request.getNationalId().trim().isEmpty()) {
            newEmployee.setNationalId(request.getNationalId());
        } else {
            newEmployee.setNationalId(oldEmployee.getNationalId());
        }


        if (request.getFullName() != null &&
                !request.getFullName().trim().isEmpty()) {
            newEmployee.setFullName(request.getFullName());
        } else {
            newEmployee.setFullName(oldEmployee.getFullName());
        }


        if (request.getDob() != null) {
            newEmployee.setDob(request.getDob());
        } else {
            newEmployee.setDob(oldEmployee.getDob());
        }


        if (request.getGender() != null &&
                !request.getGender().trim().isEmpty()) {
            newEmployee.setGender(request.getGender());
        } else {
            newEmployee.setGender(oldEmployee.getGender());
        }


        if (request.getOccupation() != null &&
                !request.getOccupation().trim().isEmpty()) {
            newEmployee.setOccupation(request.getOccupation());
        } else {
            newEmployee.setOccupation(oldEmployee.getOccupation());
        }


        if (request.getHireDate() != null) {
            newEmployee.setHireDate(request.getHireDate());
        } else {
            newEmployee.setHireDate(oldEmployee.getHireDate());
        }


        if (request.getAgeAtHire() != null) {
            newEmployee.setAgeAtHire(request.getAgeAtHire());
        } else {
            newEmployee.setAgeAtHire(oldEmployee.getAgeAtHire());
        }


        if (request.getPensionStartDate() != null) {
            newEmployee.setPensionStartDate(request.getPensionStartDate());
        } else {
            newEmployee.setPensionStartDate(
                    oldEmployee.getPensionStartDate());
        }


        if (request.getCategory() != null &&
                !request.getCategory().trim().isEmpty()) {
            newEmployee.setCategory(request.getCategory());
        } else {
            newEmployee.setCategory(oldEmployee.getCategory());
        }


        if (request.getGrossSalary() != null) {
            newEmployee.setGrossSalary(request.getGrossSalary());
        } else {
            newEmployee.setGrossSalary(oldEmployee.getGrossSalary());
        }


        if (request.getSalaryCurrency() != null &&
                !request.getSalaryCurrency().trim().isEmpty()) {
            newEmployee.setSalaryCurrency(request.getSalaryCurrency());
        } else {
            newEmployee.setSalaryCurrency(
                    oldEmployee.getSalaryCurrency());
        }


        if (request.getContributionEe() != null) {
            newEmployee.setContributionEe(request.getContributionEe());
        } else {
            newEmployee.setContributionEe(
                    oldEmployee.getContributionEe());
        }


        if (request.getContributionEr() != null) {
            newEmployee.setContributionEr(request.getContributionEr());
        } else {
            newEmployee.setContributionEr(
                    oldEmployee.getContributionEr());
        }


        if (request.getEmail() != null &&
                !request.getEmail().trim().isEmpty()) {
            newEmployee.setEmail(request.getEmail());
        } else {
            newEmployee.setEmail(oldEmployee.getEmail());
        }


        if (request.getStartingEeValue() != null) {
            newEmployee.setStartingEeValue(
                    request.getStartingEeValue());
        } else {
            newEmployee.setStartingEeValue(
                    oldEmployee.getStartingEeValue());
        }


        if (request.getStartingErValue() != null) {
            newEmployee.setStartingErValue(
                    request.getStartingErValue());
        } else {
            newEmployee.setStartingErValue(
                    oldEmployee.getStartingErValue());
        }


        if (request.getStartingFundValue() != null) {
            newEmployee.setStartingFundValue(
                    request.getStartingFundValue());
        } else {
            newEmployee.setStartingFundValue(
                    oldEmployee.getStartingFundValue());
        }


        if (request.getTerminationDate() != null) {
            newEmployee.setTerminationDate(
                    request.getTerminationDate());
        } else {
            newEmployee.setTerminationDate(
                    oldEmployee.getTerminationDate());
        }


        if (request.getResignationDate() != null) {
            newEmployee.setResignationDate(
                    request.getResignationDate());
        } else {
            newEmployee.setResignationDate(
                    oldEmployee.getResignationDate());
        }


        if (request.getVee() != null) {
            newEmployee.setVee(request.getVee());
        } else {
            newEmployee.setVee(oldEmployee.getVee());
        }


        if (request.getWeightF1Ee() != null)
            newEmployee.setWeightF1Ee(request.getWeightF1Ee());
        else
            newEmployee.setWeightF1Ee(oldEmployee.getWeightF1Ee());

        if (request.getWeightF2Ee() != null)
            newEmployee.setWeightF2Ee(request.getWeightF2Ee());
        else
            newEmployee.setWeightF2Ee(oldEmployee.getWeightF2Ee());

        if (request.getWeightF3Ee() != null)
            newEmployee.setWeightF3Ee(request.getWeightF3Ee());
        else
            newEmployee.setWeightF3Ee(oldEmployee.getWeightF3Ee());

        if (request.getWeightF4Ee() != null)
            newEmployee.setWeightF4Ee(request.getWeightF4Ee());
        else
            newEmployee.setWeightF4Ee(oldEmployee.getWeightF4Ee());

        if (request.getWeightF5Ee() != null)
            newEmployee.setWeightF5Ee(request.getWeightF5Ee());
        else
            newEmployee.setWeightF5Ee(oldEmployee.getWeightF5Ee());

        if (request.getWeightF6Ee() != null)
            newEmployee.setWeightF6Ee(request.getWeightF6Ee());
        else
            newEmployee.setWeightF6Ee(oldEmployee.getWeightF6Ee());

        if (request.getWeightF7Ee() != null)
            newEmployee.setWeightF7Ee(request.getWeightF7Ee());
        else
            newEmployee.setWeightF7Ee(oldEmployee.getWeightF7Ee());

        if (request.getWeightF8Ee() != null)
            newEmployee.setWeightF8Ee(request.getWeightF8Ee());
        else
            newEmployee.setWeightF8Ee(oldEmployee.getWeightF8Ee());

        if (request.getWeightF9Ee() != null)
            newEmployee.setWeightF9Ee(request.getWeightF9Ee());
        else
            newEmployee.setWeightF9Ee(oldEmployee.getWeightF9Ee());

        if (request.getWeightF10Ee() != null)
            newEmployee.setWeightF10Ee(request.getWeightF10Ee());
        else
            newEmployee.setWeightF10Ee(oldEmployee.getWeightF10Ee());


        if (request.getWeightF1Er() != null)
            newEmployee.setWeightF1Er(request.getWeightF1Er());
        else
            newEmployee.setWeightF1Er(oldEmployee.getWeightF1Er());

        if (request.getWeightF2Er() != null)
            newEmployee.setWeightF2Er(request.getWeightF2Er());
        else
            newEmployee.setWeightF2Er(oldEmployee.getWeightF2Er());

        if (request.getWeightF3Er() != null)
            newEmployee.setWeightF3Er(request.getWeightF3Er());
        else
            newEmployee.setWeightF3Er(oldEmployee.getWeightF3Er());

        if (request.getWeightF4Er() != null)
            newEmployee.setWeightF4Er(request.getWeightF4Er());
        else
            newEmployee.setWeightF4Er(oldEmployee.getWeightF4Er());

        if (request.getWeightF5Er() != null)
            newEmployee.setWeightF5Er(request.getWeightF5Er());
        else
            newEmployee.setWeightF5Er(oldEmployee.getWeightF5Er());

        if (request.getWeightF6Er() != null)
            newEmployee.setWeightF6Er(request.getWeightF6Er());
        else
            newEmployee.setWeightF6Er(oldEmployee.getWeightF6Er());

        if (request.getWeightF7Er() != null)
            newEmployee.setWeightF7Er(request.getWeightF7Er());
        else
            newEmployee.setWeightF7Er(oldEmployee.getWeightF7Er());

        if (request.getWeightF8Er() != null)
            newEmployee.setWeightF8Er(request.getWeightF8Er());
        else
            newEmployee.setWeightF8Er(oldEmployee.getWeightF8Er());

        if (request.getWeightF9Er() != null)
            newEmployee.setWeightF9Er(request.getWeightF9Er());
        else
            newEmployee.setWeightF9Er(oldEmployee.getWeightF9Er());

        if (request.getWeightF10Er() != null)
            newEmployee.setWeightF10Er(request.getWeightF10Er());
        else
            newEmployee.setWeightF10Er(oldEmployee.getWeightF10Er());


        newEmployee.setModifiedDate(OffsetDateTime.now());

        return employeeRepository.save(newEmployee);
    }

}