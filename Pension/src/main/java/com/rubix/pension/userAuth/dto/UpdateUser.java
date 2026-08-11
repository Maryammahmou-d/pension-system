package com.rubix.pension.userAuth.dto;

public class UpdateUser {

    private String fullName;
    private String userLogin;
    private Integer userSecurity;

    public void setFullName(String fullName){
        this.fullName=fullName;
    }
    public String getFullName(){
        return fullName;
    }

    public void setUserLogin(String userLogin){
        this.userLogin=userLogin;
    }
    public String getUserLogin(){
        return userLogin;
    }


    public void setUserSecurity(Integer userSecurity){
        this.userSecurity=userSecurity;
    }
    public Integer getUserSecurity(){
        return userSecurity;
    }


}
