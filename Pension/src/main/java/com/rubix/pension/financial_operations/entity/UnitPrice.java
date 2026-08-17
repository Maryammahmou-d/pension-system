package com.rubix.pension.financial_operations.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "\"UnitPrice\"")
public class UnitPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"EntryDate\"")
    private OffsetDateTime entryDate;

    @Column(name = "\"PriceDate\"")
    private OffsetDateTime priceDate;

    @Column(name = "\"Fund1\"")
    private Double fund1;

    @Column(name = "\"Fund2\"")
    private Double fund2;

    @Column(name = "\"Fund3\"")
    private Double fund3;

    @Column(name = "\"Fund4\"")
    private Double fund4;

    @Column(name = "\"Fund5\"")
    private Double fund5;

    @Column(name = "\"Fund6\"")
    private Double fund6;

    @Column(name = "\"Fund7\"")
    private Double fund7;

    @Column(name = "\"Fund8\"")
    private Double fund8;

    @Column(name = "\"Fund9\"")
    private Double fund9;

    @Column(name = "\"Fund10\"")
    private Double fund10;

    @Column(name = "\"UserName\"")
    private String userName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OffsetDateTime getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(OffsetDateTime entryDate) {
        this.entryDate = entryDate;
    }

    public OffsetDateTime getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(OffsetDateTime priceDate) {
        this.priceDate = priceDate;
    }

    public Double getFund1() {
        return fund1;
    }

    public void setFund1(Double fund1) {
        this.fund1 = fund1;
    }

    public Double getFund2() {
        return fund2;
    }

    public void setFund2(Double fund2) {
        this.fund2 = fund2;
    }

    public Double getFund3() {
        return fund3;
    }

    public void setFund3(Double fund3) {
        this.fund3 = fund3;
    }

    public Double getFund4() {
        return fund4;
    }

    public void setFund4(Double fund4) {
        this.fund4 = fund4;
    }

    public Double getFund5() {
        return fund5;
    }

    public void setFund5(Double fund5) {
        this.fund5 = fund5;
    }

    public Double getFund6() {
        return fund6;
    }

    public void setFund6(Double fund6) {
        this.fund6 = fund6;
    }

    public Double getFund7() {
        return fund7;
    }

    public void setFund7(Double fund7) {
        this.fund7 = fund7;
    }

    public Double getFund8() {
        return fund8;
    }

    public void setFund8(Double fund8) {
        this.fund8 = fund8;
    }

    public Double getFund9() {
        return fund9;
    }

    public void setFund9(Double fund9) {
        this.fund9 = fund9;
    }

    public Double getFund10() {
        return fund10;
    }

    public void setFund10(Double fund10) {
        this.fund10 = fund10;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}