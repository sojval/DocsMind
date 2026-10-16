package com.example.docsmndbackend.Exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }
    public ResourceNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
    public ResourceNotFoundException(){
        super("Error in processing document");
    }
}
