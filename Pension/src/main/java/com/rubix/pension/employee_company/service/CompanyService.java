package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.CreateCompanyRequest;
import com.rubix.pension.employee_company.entity.Company;
import com.rubix.pension.employee_company.exception.CompanyNotFound;
import com.rubix.pension.employee_company.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CompanyService{

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository){
        this.companyRepository=companyRepository;
    }

    public Company addNewCompany(CreateCompanyRequest request) {

        Company company = new Company();

        String newCompanyNumber = findLastCompanyNumber();

        company.setCompanyNumber(newCompanyNumber);

        company.setModifiedDate(OffsetDateTime.now());
        company.setSerial(1);

        company.setKafsCompanyNumber(request.getKafsCompanyNumber());
        company.setCompanyName(request.getCompanyName());
        company.setIssueDate(request.getIssueDate());
        company.setFrequency(request.getFrequency());
        company.setAddress(request.getAddress());
        company.setContactPerson(request.getContactPerson());
        company.setMobileNumber(request.getMobileNumber());
        company.setEmail(request.getEmail());

        company.setStartingNumberOfEmployees(
                request.getStartingNumberOfEmployees());

        company.setStartingAverageSalary(
                request.getStartingAverageSalary());

        company.setStartingFundValue(
                request.getStartingFundValue());

        company.setContributionCharges(
                request.getContributionCharges());

        company.setContributionChargesVee(
                request.getContributionChargesVee());

        company.setImc(
                request.getImc());

        company.setWithdrawalChargesEe(
                request.getWithdrawalChargesEe());

        company.setWithdrawalChargesVee(
                request.getWithdrawalChargesVee());

        company.setWithdrawalChargesEr(
                request.getWithdrawalChargesEr());

        company.setEmployeeSurrenderCharge(
                request.getEmployeeSurrenderCharge());

        company.setTopUpCharges(
                request.getTopUpCharges());

        company.setAdminCharges(
                request.getAdminCharges());

        company.setPortfolioSwitchingCharges(
                request.getPortfolioSwitchingCharges());

        company.setAllocationRedirectionCharges(
                request.getAllocationRedirectionCharges());

        company.setTerminationDate(
                request.getTerminationDate());

        company.setNewOrAcquired(
                request.getNewOrAcquired());

        company.setVestingOnHire(
                request.getVestingOnHire());

        company.setMaxWithdrawalPercentage(
                request.getMaxWithdrawalPercentage());

        company.setMaxWithdrawalCount(
                request.getMaxWithdrawalCount());

        company.setSalaryOrContribution(
                request.getSalaryOrContribution());

        company.setShowAvailableWithdrawal(
                request.getShowAvailableWithdrawal());

        return companyRepository.save(company);
    }

    public List<Company> getLatestCompanies(){
        return companyRepository.findLatestCompanies();
    }

    public List<Company> getActiveLatestCompanies(){
        return companyRepository.findLatestCompanies().stream()
                .filter(c -> c.getTerminationDate() == null)
                .toList();
    }

    public Company updateCompany(Integer id, CreateCompanyRequest requestCompany) {

        Company oldCompany = companyRepository.findById(id)
                .orElseThrow(() ->
                        new CompanyNotFound("Company Not Found"));

        Company newCompany = new Company();

        // First copy all old values
        newCompany.setCompanyNumber(oldCompany.getCompanyNumber());
        newCompany.setKafsCompanyNumber(oldCompany.getKafsCompanyNumber());
        newCompany.setCompanyName(oldCompany.getCompanyName());
        newCompany.setIssueDate(oldCompany.getIssueDate());
        newCompany.setFrequency(oldCompany.getFrequency());
        newCompany.setAddress(oldCompany.getAddress());
        newCompany.setContactPerson(oldCompany.getContactPerson());
        newCompany.setMobileNumber(oldCompany.getMobileNumber());
        newCompany.setEmail(oldCompany.getEmail());
        newCompany.setSerial(oldCompany.getSerial() == null ? 1 : oldCompany.getSerial()+1);

        newCompany.setStartingNumberOfEmployees(
                oldCompany.getStartingNumberOfEmployees());

        newCompany.setStartingAverageSalary(
                oldCompany.getStartingAverageSalary());

        newCompany.setStartingFundValue(
                oldCompany.getStartingFundValue());

        newCompany.setContributionCharges(
                oldCompany.getContributionCharges());

        newCompany.setContributionChargesVee(
                oldCompany.getContributionChargesVee());

        newCompany.setImc(oldCompany.getImc());

        newCompany.setWithdrawalChargesEe(
                oldCompany.getWithdrawalChargesEe());

        newCompany.setWithdrawalChargesVee(
                oldCompany.getWithdrawalChargesVee());

        newCompany.setWithdrawalChargesEr(
                oldCompany.getWithdrawalChargesEr());

        newCompany.setEmployeeSurrenderCharge(
                oldCompany.getEmployeeSurrenderCharge());

        newCompany.setTopUpCharges(oldCompany.getTopUpCharges());
        newCompany.setAdminCharges(oldCompany.getAdminCharges());

        newCompany.setPortfolioSwitchingCharges(
                oldCompany.getPortfolioSwitchingCharges());

        newCompany.setAllocationRedirectionCharges(
                oldCompany.getAllocationRedirectionCharges());

        newCompany.setTerminationDate(oldCompany.getTerminationDate());

        newCompany.setNewOrAcquired(oldCompany.getNewOrAcquired());

        newCompany.setVestingOnHire(oldCompany.getVestingOnHire());

        newCompany.setMaxWithdrawalPercentage(
                oldCompany.getMaxWithdrawalPercentage());

        newCompany.setMaxWithdrawalCount(
                oldCompany.getMaxWithdrawalCount());

        newCompany.setSalaryOrContribution(
                oldCompany.getSalaryOrContribution());

        newCompany.setShowAvailableWithdrawal(
                oldCompany.getShowAvailableWithdrawal());


        // Override only values that were provided
        if (requestCompany.getKafsCompanyNumber() != null
                && !requestCompany.getKafsCompanyNumber().trim().isEmpty()) {

            newCompany.setKafsCompanyNumber(
                    requestCompany.getKafsCompanyNumber());
        }

        if (requestCompany.getCompanyName() != null
                && !requestCompany.getCompanyName().trim().isEmpty()) {

            newCompany.setCompanyName(
                    requestCompany.getCompanyName());
        }


        if (requestCompany.getFrequency() != null
                && !requestCompany.getFrequency().trim().isEmpty()) {

            newCompany.setFrequency(
                    requestCompany.getFrequency());
        }

        if (requestCompany.getAddress() != null
                && !requestCompany.getAddress().trim().isEmpty()) {

            newCompany.setAddress(
                    requestCompany.getAddress());
        }

        if (requestCompany.getContactPerson() != null
                && !requestCompany.getContactPerson().trim().isEmpty()) {

            newCompany.setContactPerson(
                    requestCompany.getContactPerson());
        }

        if (requestCompany.getMobileNumber() != null
                && !requestCompany.getMobileNumber().trim().isEmpty()) {

            newCompany.setMobileNumber(
                    requestCompany.getMobileNumber());
        }

        if (requestCompany.getEmail() != null
                && !requestCompany.getEmail().trim().isEmpty()) {

            newCompany.setEmail(
                    requestCompany.getEmail());
        }


        if (requestCompany.getStartingNumberOfEmployees() != null) {
            newCompany.setStartingNumberOfEmployees(
                    requestCompany.getStartingNumberOfEmployees());
        }

        if (requestCompany.getStartingAverageSalary() != null) {
            newCompany.setStartingAverageSalary(
                    requestCompany.getStartingAverageSalary());
        }

        if (requestCompany.getStartingFundValue() != null) {
            newCompany.setStartingFundValue(
                    requestCompany.getStartingFundValue());
        }

        if (requestCompany.getContributionCharges() != null) {
            newCompany.setContributionCharges(
                    requestCompany.getContributionCharges());
        }

        if (requestCompany.getContributionChargesVee() != null) {
            newCompany.setContributionChargesVee(
                    requestCompany.getContributionChargesVee());
        }

        if (requestCompany.getImc() != null) {
            newCompany.setImc(requestCompany.getImc());
        }

        if (requestCompany.getWithdrawalChargesEe() != null) {
            newCompany.setWithdrawalChargesEe(
                    requestCompany.getWithdrawalChargesEe());
        }

        if (requestCompany.getWithdrawalChargesVee() != null) {
            newCompany.setWithdrawalChargesVee(
                    requestCompany.getWithdrawalChargesVee());
        }

        if (requestCompany.getWithdrawalChargesEr() != null) {
            newCompany.setWithdrawalChargesEr(
                    requestCompany.getWithdrawalChargesEr());
        }

        if (requestCompany.getEmployeeSurrenderCharge() != null) {
            newCompany.setEmployeeSurrenderCharge(
                    requestCompany.getEmployeeSurrenderCharge());
        }

        if (requestCompany.getTopUpCharges() != null) {
            newCompany.setTopUpCharges(
                    requestCompany.getTopUpCharges());
        }

        if (requestCompany.getAdminCharges() != null) {
            newCompany.setAdminCharges(
                    requestCompany.getAdminCharges());
        }

        if (requestCompany.getPortfolioSwitchingCharges() != null) {
            newCompany.setPortfolioSwitchingCharges(
                    requestCompany.getPortfolioSwitchingCharges());
        }

        if (requestCompany.getAllocationRedirectionCharges() != null) {
            newCompany.setAllocationRedirectionCharges(
                    requestCompany.getAllocationRedirectionCharges());
        }

        if (requestCompany.getTerminationDate() != null) {
            newCompany.setTerminationDate(
                    requestCompany.getTerminationDate());
        }

        if (requestCompany.getNewOrAcquired() != null) {
            newCompany.setNewOrAcquired(
                    requestCompany.getNewOrAcquired());
        }

        if (requestCompany.getVestingOnHire() != null) {
            newCompany.setVestingOnHire(
                    requestCompany.getVestingOnHire());
        }

        if (requestCompany.getMaxWithdrawalPercentage() != null) {
            newCompany.setMaxWithdrawalPercentage(
                    requestCompany.getMaxWithdrawalPercentage());
        }

        if (requestCompany.getMaxWithdrawalCount() != null) {
            newCompany.setMaxWithdrawalCount(
                    requestCompany.getMaxWithdrawalCount());
        }

        if (requestCompany.getSalaryOrContribution() != null) {
            newCompany.setSalaryOrContribution(
                    requestCompany.getSalaryOrContribution());
        }

        if (requestCompany.getShowAvailableWithdrawal() != null) {
            newCompany.setShowAvailableWithdrawal(
                    requestCompany.getShowAvailableWithdrawal());
        }


        newCompany.setModifiedDate(OffsetDateTime.now());

        // This will create a NEW row.
        return companyRepository.save(newCompany);
    }

    public String findLastCompanyNumber(){
        String lastCompanyNumber = companyRepository.findLastCompanyNumber();

        int nextNumber;

        if (lastCompanyNumber == null) {
            nextNumber = 1;
        } else {
            String numberPart = lastCompanyNumber.substring(1);
            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        String newCompanyNumber = String.format("C%06d", nextNumber);

        return newCompanyNumber;
    }



}
