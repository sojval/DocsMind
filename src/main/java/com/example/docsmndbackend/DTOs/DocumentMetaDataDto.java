package com.example.docsmndbackend.DTOs;

import com.example.docsmndbackend.Entity.DocumentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentMetaDataDto {
    private Long id;
    private  String filename;
    private String contentType;
    private Long filesize;
    private Integer totalPages;
    private Integer totalChunks;
    private DocumentStatus status;

    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
