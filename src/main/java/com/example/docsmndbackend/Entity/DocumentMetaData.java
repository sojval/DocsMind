package com.example.docsmndbackend.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import jakarta.persistence.*;
import lombok.*;

import javax.swing.text.Document;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentMetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private  String filename;
    private String contentType;
    private Long filesize;
    private Integer totalPages;
    private Integer totalChunks;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
