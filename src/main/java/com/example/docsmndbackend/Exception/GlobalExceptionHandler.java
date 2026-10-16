package com.example.docsmndbackend.Exception;

import com.example.docsmndbackend.DTOs.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex){
        logger.warn("Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.builder()
                .success(false)
                .data(null)
                .message(String.valueOf(ex))
                .timestamp(LocalDateTime.now()
        ).build());
    }
    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<APIResponse<Object>> handleDocumentProcessingException(DocumentProcessingException ex){
        logger.error("Document processing failed");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(APIResponse.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now()).build());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<APIResponse<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex){
        logger.error("Max upload size exceeded");
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(APIResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handleException(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();
        for(FieldError fieldError : ex.getBindingResult().getFieldErrors()){
            errors.put(fieldError.getField(),fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.builder()
                        .success(false)
                        .data(errors)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex){
        logger.error("Illegal argument");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleException(Exception ex){
        logger.error("Internal server error");
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }
}
