package com.rubix.pension.employee_company.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="\"Contributions\"")
public class Contribution {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @SequenceGenerator(
            name = "contributions_id_seq",
            sequenceName = "contributions_id_seq",
            allocationSize = 1
    )

    @Column(name="\"ID\"")
    private Integer Id;

    @Column(name="\"Company_Number\"")
    private String companyNumber;

    @Column(name="\"Modified_Date\"")
    private OffsetDateTime modifiedDate;

    @Column(name="\"Category\"")
    private String category;

    @Column(name="\"EE\"")
    private Double EE;

    @Column(name="\"ER\"")
    private Double ER;

    @Column(name="\"UserName\"")
    private String userName;


    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
