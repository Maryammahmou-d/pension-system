package com.rubix.pension.userAuth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "\"UserTable\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="\"UserID\"")
    private Integer userId;

    @Column(name="\"Full_Name\"")
    private String fullName;

    @NotNull(message = "User Login can not be empty")
    @Column(name="\"UserLogin\"")
    private String userLogin;

    @Column(name="\"Password\"")
    private String password;

    @Column(name="\"UserSecurity\"")
    private Integer userSecurity;


    public Integer getUserId(){
        return userId;
    }
    public void setUserId(Integer userId){
        this.userId=userId;
    }


    public String getFullName(){
        return fullName;
    }
    public void setFullName(String fullName){
        this.fullName=fullName;
    }


    public String getUserLogin(){
        return userLogin;
    }
    public void setUserLogin(String userLogin){
        this.userLogin=userLogin;
    }


    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }


    public Integer getUserSecurity(){
        return userSecurity;
    }
    public void setUserSecurity(Integer userSecurity){
        this.userSecurity=userSecurity;
    }




}
