package com.rubix.pension.employee_company.dto;

public class CreateContributionRequest {

    private String companyNumber;
    private String category;
    private Double EE;
    private Double ER;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getEE() {
        return EE;
    }

    public void setEE(Double EE) {
        this.EE = EE;
    }

    public Double getER() {
        return ER;
    }

    public void setER(Double ER) {
        this.ER = ER;
    }
}
