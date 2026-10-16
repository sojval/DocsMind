package com.example.docsmndbackend.Controller;

import com.example.docsmndbackend.DTOs.APIResponse;
import com.example.docsmndbackend.DTOs.DocumentMetaDataDto;
import com.example.docsmndbackend.DTOs.DocumentResponseDto;
import com.example.docsmndbackend.Services.DocumentMetaDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(
        name="Document Management",
        description = "Endpoints for uploading ,listing and managing documents and their vectors embeddings"
)
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentMetaDataService documentservice;

    @PostMapping(value="/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload and index a document(PDF,DOCX,TEXT,MD,CSV)",
            description = "This api is used to upload and index documents files"
    )
    public ResponseEntity<APIResponse<?>> uploadDocument(
            @RequestParam("file")MultipartFile file
    ) {
       //process the file
        DocumentResponseDto documentResponseDto =this.documentservice.uploadAndProcess(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<DocumentResponseDto>builder()
                .success(true)
                .data(documentResponseDto)
                        .timestamp(LocalDateTime.now())
                        .message("file created SuccessFully")
                        .build());

    }
    // upload multiple documents
    @PostMapping(value = "/puload-multiple",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload and index multiple documents simultaneously",
            description = "This api is used to upload and index multiple documents"
    )
    public ResponseEntity<APIResponse<?>> uploadDocument(@RequestParam("files") List<MultipartFile> files) {
        List<DocumentResponseDto> responseDtos = documentservice.uploadMultipleDocuments(files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        APIResponse.<List<DocumentResponseDto>>
                                builder()
                                .message("files uploaded successfully")
                                .success(true)
                                .timestamp(LocalDateTime.now())
                                .data(responseDtos)
                                .build()
                );
    }
    //list all uploaded documents
    @GetMapping
    public ResponseEntity<APIResponse<List<DocumentMetaDataDto>>> uploadMultipleDocuments(){
        List<DocumentMetaDataDto> documents =
                documentservice.getAllDocuments();
        return ResponseEntity.ok(
                APIResponse.<List<DocumentMetaDataDto>>
                                builder()
                        .message("files loaded successfully")
                        .success(true)
                        .timestamp(LocalDateTime.now())
                        .data(documents)
                        .build()
        );
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get metadata of a specific document by ID")
    public ResponseEntity<APIResponse<DocumentMetaDataDto>> getDocumentById(
            @PathVariable Long id) {

        DocumentMetaDataDto documentResponseDto =
                documentservice.getDocumentByID(id);

        APIResponse<DocumentMetaDataDto> response =
                APIResponse.<DocumentMetaDataDto>builder()
                        .message("Document loaded successfully")
                        .success(true)
                        .timestamp(LocalDateTime.now())
                        .data(documentResponseDto)
                        .build();

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document and purge its vector embeddings from vector store")
    public ResponseEntity<APIResponse<Boolean>> deleteDocument(@PathVariable Long id) {
        documentservice.deleteDocument(id);
        return ResponseEntity.ok(APIResponse.<Boolean>builder().message("Document deleted successfully").build());
    }

}
