package com.example.docsmndbackend.DTOs;

import com.example.docsmndbackend.Entity.DocumentStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponseDto {
    private Long id;
    private String fileName;
    private String fileSize;
    private DocumentStatus documentStatus;
    private Integer chunksCreated;
    private String message;
}
