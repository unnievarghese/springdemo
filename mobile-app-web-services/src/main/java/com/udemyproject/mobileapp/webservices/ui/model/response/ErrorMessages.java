package com.udemyproject.mobileapp.webservices.ui.model.response;

public enum ErrorMessages {

    MISSING_REQUIRED_FIELD("Missing required field, Please check documentation for required field"),
    RECORD_ALREADY_EXISTS("Record already exists"),
    INTERNAL_SERVER_ERROR("Internal Server error"),
    NO_RECORD_FOUND("Record with provided id not found"),
    AUTHENTICATION_FAILED("Authentication is failed"),
    COULD_NOT_UPDATE_RECORD("Could not update record"),
    COULD_NOT_DELETE_RECORD("Could not delete record"),
    EMAIL_ADDRESS_NOT_VERIFIED("Email address could not be verified");

    private String errorMessage;

    ErrorMessages(String errorMessage){
        this.errorMessage = errorMessage;
    }
    public String getErrorMessage(){
        System.out.println("1");
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
