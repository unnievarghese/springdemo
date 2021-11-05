package com.udemyproject.mobileapp.webservices.exceptions;

public class UserServiceException extends RuntimeException{

    public static final long serialVersionUID = -3941833315619492413L;

    public UserServiceException(String message){
        super(message);
    }
}
