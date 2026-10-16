package com.example.docsmndbackend.DTOs;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponseDTo {
    private String answer;
    private String conversationId;
    private List<CitationDto> citations;
    private Long responseTimeMs;
}
