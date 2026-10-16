package com.example.docsmndbackend.Controller;

import com.example.docsmndbackend.DTOs.*;
import com.example.docsmndbackend.Services.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.naming.directory.SearchResult;
import java.time.LocalDateTime;

@RestController
@RequestMapping
@Tag(
        name="Chat Controlelr",
        description = "Endpoinst for uploading the documents"
)
@RequiredArgsConstructor
public class ChatController {
    private final RagService ragService;
    private final WebClient.Builder builder;

    @PostMapping("/query")
    @Operation(summary = "ask a question against all documents or a specific document with citation")
    public ResponseEntity<APIResponse<ChatResponseDTo>> askQuestion(
            @Valid @RequestBody ChatRequestDto requestDto) {

        ChatResponseDTo chat = ragService.askQuestion(requestDto);

        APIResponse<ChatResponseDTo> response = APIResponse.<ChatResponseDTo>builder()
                .success(true)
                .message("Question answered successfully")
                .data(chat)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/stream")
    @Operation(summary = "Stream real time Q&A answer tokens via Server-sent Evenets (SSE)")
    public Flux<String> stream(
            @Valid @RequestBody ChatRequestDto chat
    ){
        return ragService.streamQuestionAnswer(chat);
    }
    @PostMapping("/search/similarity")
    @Operation(summary = "Perform semantic similarity search on stored document vectors")
    public ResponseEntity<APIResponse<SearchResultDto>> searchSimilar(
            @Valid @RequestBody SearchRequestDto requestDto) {

        SearchResultDto resultDto = ragService.searchSimilarChunks(requestDto);

        APIResponse<SearchResultDto> response =
                APIResponse.<SearchResultDto>builder()
                        .success(true)
                        .message("Similarity search completed successfully")
                        .data(resultDto)
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(response);
    }



}
