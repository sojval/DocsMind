package com.example.docsmndbackend.Exception;

public class DocumentProcessingException extends RuntimeException{
    public DocumentProcessingException(String message){
        super(message);
    }
    public DocumentProcessingException(String message, Throwable cause){
        super(message,cause);
    }
    public DocumentProcessingException(){
        super("Error in processing document");
    }

}
