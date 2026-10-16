package com.example.docsmndbackend.DTOs;

import lombok.*;

import java.util.Map;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitationDto {
    private Long documentId;
    private String fileName;
    private Integer chunkIndex;
    private Integer pageNumber;
    private String support;
    private double similarityScore;
    private Map<String,Object> metadata;
}
