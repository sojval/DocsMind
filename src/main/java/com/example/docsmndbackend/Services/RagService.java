
package com.example.docsmndbackend.Services;

import com.example.docsmndbackend.Config.AppProperties;
import com.example.docsmndbackend.DTOs.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private static final Logger logger =
            LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final AppProperties appProperties;
    private final ChatClient chatClient;

    public Flux<String> streamQuestionAnswer(ChatRequestDto chatRequestDto) {
        logger.info("Received request to stream question answer");
        List<Document> relevantDocuments = retrieveRelevantDocuments(
                chatRequestDto.getQuestion(),
                chatRequestDto.getDocumentId(),
                chatRequestDto.getTopK(),
                chatRequestDto.getSimilarity()
        );
        String contextText = buildContextString(relevantDocuments);
        String userPrompt = buildPrompt(chatRequestDto.getQuestion(), contextText);
        return chatClient.prompt()
                .user(userPrompt)
                .stream()
                .content();
    }

    public SearchResultDto searchSimilarChunks(
            SearchRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Search request cannot be null"
            );
        }

        if (!StringUtils.hasText(request.getQuery())) {
            throw new IllegalArgumentException(
                    "Query cannot be blank"
            );
        }

        List<Document> documents = retrieveRelevantDocuments(
                request.getQuery(),
                request.getDocumentId(),
                request.getTopK(),
                request.getSimilaritySearch()
        );

        List<CitationDto> citations = documents.stream()
                .map(this::mapToCitation)
                .toList();

        return SearchResultDto.builder()
                .query(request.getQuery())
                .totalMatches(citations.size())
                .matches(citations)
                .build();
    }

    private CitationDto mapToCitation(Document document) {

        Map<String, Object> metadata = document.getMetadata();

        Object documentId = metadata.get("documentId");
        Object fileName = metadata.get("documentName");
        Object chunkIndex = metadata.get("chunkIndex");
        Object pageNumber = metadata.get("pageNumber");

        return CitationDto.builder()
                .documentId(documentId != null
                        ? Long.valueOf(documentId.toString())
                        : null)
                .fileName(fileName != null
                        ? fileName.toString()
                        : null)
                .chunkIndex(chunkIndex != null
                        ? Integer.valueOf(chunkIndex.toString())
                        : null)
                .pageNumber(pageNumber != null
                        ? Integer.valueOf(pageNumber.toString())
                        : null)
                .support(document.getText())
                .similarityScore(0.0)
                .metadata(metadata)
                .build();
    }

    private List<Document> retrieveRelevantDocuments(
            @NotBlank(message = "Query cannot be blank")
            String query,
            Long documentId,
            Integer topK,
            Double similaritySearch) {

        int effectiveTopK = topK != null
                ? topK
                : appProperties.getRag().getTopK();

        double effectiveSimilarity = similaritySearch != null
                ? similaritySearch
                : appProperties.getRag().getSimilarityThreshold();

        if (effectiveTopK < 1) {
            throw new IllegalArgumentException(
                    "topK must be greater than zero"
            );
        }

        if (effectiveSimilarity < 0.0
                || effectiveSimilarity > 1.0) {
            throw new IllegalArgumentException(
                    "Similarity threshold must be between 0 and 1"
            );
        }

        SearchRequest.Builder searchBuilder =
                SearchRequest.builder()
                        .query(query)
                        .topK(effectiveTopK);

        if (effectiveSimilarity > 0.0) {
            searchBuilder.similarityThreshold(
                    effectiveSimilarity
            );
        }

        if (documentId != null) {
            FilterExpressionBuilder builder =
                    new FilterExpressionBuilder();

            searchBuilder.filterExpression(
                    builder.eq(
                            "documentId",
                            documentId.toString()
                    ).build()
            );
        }

        try {
            List<Document> results = vectorStore.similaritySearch(
                    searchBuilder.build()
            );

            logger.info(
                    "RAG search completed: documentId={}, results={}",
                    documentId,
                    results.size()
            );

            return results;

        } catch (Exception e) {
            logger.error(
                    "Failed to retrieve relevant documents. "
                            + "documentId={}, query={}",
                    documentId,
                    query,
                    e
            );

            throw new RuntimeException(
                    "Failed to retrieve relevant documents",
                    e
            );
        }
    }
    public ChatResponseDTo askQuestion(ChatRequestDto chatRequestDto){
        long startTime = System.currentTimeMillis();
        logger.info("Processing query",chatRequestDto.getQuestion());
        List<Document> similarityDocuments = this.retrieveRelevantDocuments(
                chatRequestDto.getQuestion(),
                chatRequestDto.getDocumentId(),
                chatRequestDto.getTopK(),
                chatRequestDto.getSimilarity()
        );
        List<CitationDto> citationDtos = similarityDocuments.stream()
                .map(this::mapToCitation)
                .toList();
        String contextText = buildContextString(similarityDocuments);
        String prompt = buildPrompt(chatRequestDto.getQuestion(),contextText);
        String ans = this.chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        long responseTime = System.currentTimeMillis() - startTime;
        logger.info("Completed Qand A ");
        return ChatResponseDTo.builder()
                .answer(ans)
                .conversationId(chatRequestDto.getConversationId())
                .citations(citationDtos)
                .responseTimeMs(responseTime)
                .build();
    }

    private String buildPrompt(String question, String contextText) {
        if(contextText != null &&  !contextText.isEmpty()) {
            return String.format("""
                       Document Context:
                       ---------------------
                       %s
                       ---------------------                                       \s
                       User Message / Question: %s            \s
                       Instructions:
                       - If the user's question relates to the document context above, prioritize answering using that context and reference key sections.
                       - If the user is asking a general question, greeting, or discussing topics beyond the document context, respond helpfully and conversationally using your general knowledge while weaving in relevant document context if applicable.
                    """,contextText,question);
        }
        else{
            return String.format(
                    """
                             User Message / Question:
                             %s                        \s
                             Instructions:
                                - Respond helpfully, accurately, and conversationally to the user's message using your broad knowledge base.
                            """,question);
        }
    }

    private String buildContextString(List<Document> similarityDocuments) {
        if(similarityDocuments.isEmpty()) {
            return "";
        }
        return similarityDocuments.stream()
                .map(doc ->{
                    String fileName = doc.getMetadata().getOrDefault("documentName", "").toString();
                    Object page =  doc.getMetadata().getOrDefault("pageNumber", "").toString();
                    return String.format("%s (%s)", fileName, page);
                }).collect(Collectors.joining("\n\n---\n\n"));
    }
}
