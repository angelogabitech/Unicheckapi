package com.unicheck.Unicheckapi.Exception;


public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}