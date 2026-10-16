package com.example.docsmndbackend.Repository;

import com.example.docsmndbackend.DTOs.DocumentMetaDataDto;
import com.example.docsmndbackend.DTOs.DocumentResponseDto;
import com.example.docsmndbackend.Entity.DocumentMetaData;
import com.example.docsmndbackend.Entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMetaDataRepo  extends JpaRepository<DocumentMetaData,Long> {
   List<DocumentMetaData> findAllByOrderByCreatedAtDesc();
   List<DocumentMetaData> findAllByStatus(DocumentStatus status);
}
