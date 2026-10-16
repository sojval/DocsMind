package com.example.docsmndbackend.Config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private RagProperties rag = new RagProperties();
    private CorsProperties cors = new CorsProperties();


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagProperties {
        private int chunksize = 600;
        private int minChunkSizeChars = 300;
//        private int chunkOverlap = 100;
        private int minChunkLengthToEmbed = 5;
        private int maxNumChunks = 200;
        private double similarityThreshold = 0.0;
        private int topK = 5;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CorsProperties {
        private String allowedOrigins = "*";
        private String allowedHeaders = "*";
        private String getAllowedMethod = "GET,POST,PUT,DELETE,OPTIONS,HEAD,TRACE";
    }
}
