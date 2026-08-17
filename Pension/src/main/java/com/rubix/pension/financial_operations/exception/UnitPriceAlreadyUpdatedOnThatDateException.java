package com.rubix.pension.financial_operations.exception;

public class UnitPriceAlreadyUpdatedOnThatDateException extends RuntimeException{

    public UnitPriceAlreadyUpdatedOnThatDateException(String message){
        super(message);
    }
}
