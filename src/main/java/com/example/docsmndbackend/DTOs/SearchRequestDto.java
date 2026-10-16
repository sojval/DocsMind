package com.example.docsmndbackend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequestDto {
    @NotBlank(message = "query cannot be null")
    private String query;
    private Long documentId;
    private Integer topK;
    private Double similaritySearch;
}
