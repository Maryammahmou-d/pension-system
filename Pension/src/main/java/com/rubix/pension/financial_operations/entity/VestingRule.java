package com.rubix.pension.financial_operations.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "\"Vesting\"")
public class VestingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vesting_id_seq")
    @SequenceGenerator(name = "vesting_id_seq", sequenceName = "vesting_id_seq", allocationSize = 1)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"Company_Number\"")
    private String companyNumber;

    @Column(name = "\"Modified_Date\"")
    private OffsetDateTime modifiedDate;

    @Column(name = "\"Year1\"")
    private Double year1;

    @Column(name = "\"Year2\"")
    private Double year2;

    @Column(name = "\"Year3\"")
    private Double year3;

    @Column(name = "\"Year4\"")
    private Double year4;

    @Column(name = "\"Year5\"")
    private Double year5;

    @Column(name = "\"Year6\"")
    private Double year6;

    @Column(name = "\"Year7\"")
    private Double year7;

    @Column(name = "\"Year8\"")
    private Double year8;

    @Column(name = "\"Year9\"")
    private Double year9;

    @Column(name = "\"Year10\"")
    private Double year10;

    @Column(name = "\"UserName\"")
    private String userName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public OffsetDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(OffsetDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Double getYear1() {
        return year1;
    }

    public void setYear1(Double year1) {
        this.year1 = year1;
    }

    public Double getYear2() {
        return year2;
    }

    public void setYear2(Double year2) {
        this.year2 = year2;
    }

    public Double getYear3() {
        return year3;
    }

    public void setYear3(Double year3) {
        this.year3 = year3;
    }

    public Double getYear4() {
        return year4;
    }

    public void setYear4(Double year4) {
        this.year4 = year4;
    }

    public Double getYear5() {
        return year5;
    }

    public void setYear5(Double year5) {
        this.year5 = year5;
    }

    public Double getYear6() {
        return year6;
    }

    public void setYear6(Double year6) {
        this.year6 = year6;
    }

    public Double getYear7() {
        return year7;
    }

    public void setYear7(Double year7) {
        this.year7 = year7;
    }

    public Double getYear8() {
        return year8;
    }

    public void setYear8(Double year8) {
        this.year8 = year8;
    }

    public Double getYear9() {
        return year9;
    }

    public void setYear9(Double year9) {
        this.year9 = year9;
    }

    public Double getYear10() {
        return year10;
    }

    public void setYear10(Double year10) {
        this.year10 = year10;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double yearPercent(int yearsOfService) {
        if (yearsOfService >= 10) {
            return year10;
        }
        return switch (yearsOfService) {
            case 1 -> year1;
            case 2 -> year2;
            case 3 -> year3;
            case 4 -> year4;
            case 5 -> year5;
            case 6 -> year6;
            case 7 -> year7;
            case 8 -> year8;
            case 9 -> year9;
            default -> 0.0;
        };
    }
}
