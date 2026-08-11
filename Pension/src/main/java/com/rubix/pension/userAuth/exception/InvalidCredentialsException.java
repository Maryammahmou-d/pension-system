package com.rubix.pension.userAuth.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException(String message){
        super(message);
    }
}
