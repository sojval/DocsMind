package com.example.docsmndbackend.DTOs;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

}
