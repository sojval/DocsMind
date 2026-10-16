package com.example.docsmndbackend.Services;

import com.example.docsmndbackend.Config.AppProperties;
import com.example.docsmndbackend.Entity.DocumentMetaData;
import com.example.docsmndbackend.Entity.DocumentStatus;
import com.example.docsmndbackend.Repository.DocumentMetaDataRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {
    private final Logger logger = LoggerFactory.getLogger(DocumentIngestionService.class);
    private final VectorStore vectorStore;
    private final DocumentMetaDataRepo documentMetaDataRepo;
    private final AppProperties appProperties;
    public int ingest(DocumentMetaData document, List<Document> parseDocs) {
        logger.info("In DocumentIngestionService.ingest",documentMetaDataRepo,document,parseDocs);

        try{
            document.setStatus(DocumentStatus.PROCESSING);
            document.setTotalPages(document.getTotalPages());

            //1.text chunking using TokenSpillter
            TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                    .withChunkSize(appProperties.getRag().getChunksize())
                    .withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars())
                    .withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed())
                    .withMaxNumChunks(appProperties.getRag().getMaxNumChunks())
                    .withKeepSeparator(true)
                    .build();

            List<Document> chunks = tokenTextSplitter.apply(parseDocs);
            if(chunks.isEmpty()){
                document.setStatus(DocumentStatus.FAILED);
                document.setError("No chunks found");
                documentMetaDataRepo.save(document);
                return 0;
            }

            // 2. Metadata enrichment on ech chunk
            // 2. Metadata enrichment on each chunk
            List<Document> enrichChunks = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);

                Map<String, Object> enrichMetaData =
                        new HashMap<>(chunk.getMetadata());

                // Remove any existing null keys or values
                enrichMetaData.entrySet().removeIf(entry ->
                        entry.getKey() == null || entry.getValue() == null
                );

                // Add only valid metadata
                enrichMetaData.put("documentId", document.getId());
                enrichMetaData.put("chunkSize", i);

                Object documentType = chunk.getMetadata().get("documentType");
                Object documentName = chunk.getMetadata().get("documentName");

                if (documentType != null) {
                    enrichMetaData.put("documentType", documentType);
                }

                if (documentName != null) {
                    enrichMetaData.put("documentName", documentName);
                }

                Object pageNumber = chunk.getMetadata().get("Page number");

                if (pageNumber != null) {
                    enrichMetaData.put("pageNumber", pageNumber);
                }

                Document enrichDocs = new Document(
                        chunk.getText(),
                        enrichMetaData
                );

                enrichChunks.add(enrichDocs);
            }

            //3 . write chunks embedding to pinecone
            logger.info("In DocumentIngestionService.ingest",document,enrichChunks);
            vectorStore.add(enrichChunks);


            //4. Update document status
            document.setStatus(DocumentStatus.INDEXED);
            document.setTotalPages(document.getTotalPages());
            document.setError("Document IngestionService is done");
            documentMetaDataRepo.save(document);

            return enrichChunks.size();
        }
        catch(Exception e){
            logger.error("In DocumentIngestionService.ingest",document,e);
            document.setStatus(DocumentStatus.FAILED);
            document.setError("Error while processing the document");
            documentMetaDataRepo.save(document);
            throw new RuntimeException(e);
        }
    }
}
