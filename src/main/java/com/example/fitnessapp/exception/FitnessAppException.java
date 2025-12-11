package com.example.fitnessapp.exception;

public class FitnessAppException extends RuntimeException {

    public FitnessAppException(String message) {
        super(message);
    }

    public FitnessAppException(String message, Throwable cause) {
        super(message, cause);
    }
}

