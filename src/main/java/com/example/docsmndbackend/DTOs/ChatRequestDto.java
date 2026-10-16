package com.example.docsmndbackend.DTOs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRequestDto {
    private String question;
    private Integer topK;
    private Long documentId;
    private Double similarity;;
    private String conversationId;

}
