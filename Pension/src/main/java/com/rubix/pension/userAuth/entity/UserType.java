package com.rubix.pension.userAuth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="\"UserType\"")
public class UserType {

    @Id
    @Column(name = "\"UserTypeID\"")
    private Integer userTypeId;

    @Column(name = "\"UserSecurity\"")
    private String userSecurity;

    public Integer getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Integer userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getUserSecurity(){
        return userSecurity;
    }


    public void setUserSecurity(String userSecurity) {
        this.userSecurity = userSecurity;
    }

}
