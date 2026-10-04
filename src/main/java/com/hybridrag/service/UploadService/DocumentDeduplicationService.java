package com.hybridrag.service.UploadService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentDeduplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDeduplicationService.class);

    private final ChromaVectorStore defaultVectorStore;
    private final ChromaVectorStore llmVectorStore;

    public DocumentDeduplicationService(
            @Qualifier("defaultVectorStore") ChromaVectorStore defaultVectorStore,
            @Qualifier("llmVectorStore") ChromaVectorStore llmVectorStore) {
        this.defaultVectorStore = defaultVectorStore;
        this.llmVectorStore = llmVectorStore;
    }

    public String calculateHash(MultipartFile file) throws IOException {
        logger.info("Calculating MD5 hash for file: {}", file.getOriginalFilename());
        return DigestUtils.md5DigestAsHex(file.getBytes());
    }

    public boolean isDuplicate(String fileHash, boolean useLLMEmbedding) {
        ChromaVectorStore selectedStore = useLLMEmbedding ? llmVectorStore : defaultVectorStore;

        try {
            // We search the vector store specifically for chunks that contain this file_hash in their metadata
            List<Document> existingDocs = selectedStore.similaritySearch(
                    SearchRequest.defaults()
                            .withQuery("duplicate check") // The text query doesn't matter much because we are filtering by metadata
                            .withTopK(1)
                            .withFilterExpression(new FilterExpressionBuilder().eq("file_hash", fileHash).build())
            );
            
            boolean isDup = !existingDocs.isEmpty();
            if (isDup) {
                logger.info("Found duplicate document with hash: {}", fileHash);
            }
            return isDup;
        } catch (Exception e) {
            logger.warn("Could not perform duplication check, assuming not duplicate. Error: {}", e.getMessage());
            return false;
        }
    }
}
