package com.example.docsmndbackend.Services;

import com.example.docsmndbackend.DTOs.DocumentMetaDataDto;
import com.example.docsmndbackend.DTOs.DocumentResponseDto;
import com.example.docsmndbackend.Entity.DocumentMetaData;
import com.example.docsmndbackend.Entity.DocumentStatus;
import com.example.docsmndbackend.Exception.ResourceNotFoundException;
import com.example.docsmndbackend.Repository.DocumentMetaDataRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentMetaDataService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentMetaDataService.class.getName());
    private final DocumentMetaDataRepo documentMetaDataRepo;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentParserService parseService;
    private final DocumentIngestionService ingestionService;
    private final ModelMapper modelMapper;
    private final VectorStore vectorStore;

    //method to upload and parse document
    public DocumentResponseDto uploadAndProcess(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename():"document";
        String contentType = file.getContentType() != null ? file.getContentType() : "appliaction/octat-stream";

        DocumentMetaData document = DocumentMetaData.builder()
                .filename(filename).contentType(contentType).filesize(file.getSize()).status(DocumentStatus.UPLOADING).build();

        documentMetaDataRepo.save(document);
        //parse the file
        List<Document> parseDocs = parseService.parse(file);


        //ingest service
        int chunksCreated = ingestionService.ingest(document,parseDocs);

        return DocumentResponseDto.builder()
                .id(document.getId())
                .fileName(document.getFilename())
                .fileSize(String.valueOf(document.getFilesize()))
                .chunksCreated(chunksCreated)
                .documentStatus(document.getStatus())
                .message("Document processed successfully")
                .build();
    }

    public List<DocumentResponseDto> uploadMultipleDocuments(List<MultipartFile> files) {
        List<DocumentResponseDto> responseDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            DocumentResponseDto response = this.uploadAndProcess(file);
            responseDtos.add(response);
        }
        return responseDtos;
    }

    public List<DocumentMetaDataDto> getAllDocuments() {
        List<DocumentMetaData> documents =
                documentMetaDataRepo.findAllByOrderByCreatedAtDesc();

        return documents.stream()
                .map(document -> modelMapper.map(document, DocumentMetaDataDto.class))
                .toList();
    }


    public DocumentMetaDataDto getDocumentByID(Long id) {
        DocumentMetaData document = documentMetaDataRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id)
                );

        return modelMapper.map(document, DocumentMetaDataDto.class);
    }

    public void deleteDocument(Long id) {
        DocumentMetaData document = documentMetaDataRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id)
                );
        //delete the vector and entries
        try {
            vectorStore.delete("documentId == " + id);

            logger.info("Deleted Pinecone vectors for document ID: {}", id);

        } catch (Exception e) {
            logger.error(
                    "Failed to delete Pinecone vectors for document ID: {}", id, e
            );

            throw new IllegalStateException(
                    "Could not delete document vectors from Pinecone", e
            );
        }
        documentMetaDataRepo.delete(document);

        logger.info("Deleted document metadata for ID: {}", id);
    }
}
