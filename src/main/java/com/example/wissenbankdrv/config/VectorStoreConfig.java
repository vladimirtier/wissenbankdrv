//package com.example.wissenbankdrv.config;
//
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//@Configuration
//public class VectorStoreConfig {
//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
//                .dimensions(768)                   // nomic-embed-text
//                .initializeSchema(true)
//                .schemaName("public")
//                .vectorTableName("document_embeddings")
//                .maxDocumentBatchSize(10000)
//                .build();
//    }
//}
