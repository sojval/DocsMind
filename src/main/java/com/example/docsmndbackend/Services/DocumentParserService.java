package com.example.docsmndbackend.Services;

import com.example.docsmndbackend.Exception.DocumentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import java.util.List;

@Service
public class DocumentParserService {
    private final Logger logger = LoggerFactory.getLogger(DocumentParserService.class);

    public List<Document> parse(MultipartFile file)  {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename():"document";
        String contentType = file.getContentType() != null ? file.getContentType() : "appliaction/octat-stream";

        logger.info("Parsing document"+filename+file.getSize()+contentType);
        try {
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return super.getFilename();
                }

            };
            if(filename.toLowerCase().endsWith(".pdf") || contentType.contains("pdf")){
                return parsePDF(resource);
            }
            else{
                return parseGenericFile(resource);
            }
        }
        catch (IOException e) {
            throw new DocumentProcessingException(e.getMessage());
        }
    }

    private List<Document> parseGenericFile(Resource resource) {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageBottomMargin(0)
                .withPageTopMargin(0)
                .build();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource,config);
        return reader.read();
    }

    private List<Document> parsePDF(Resource resource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        return tikaDocumentReader.read();
    }
}