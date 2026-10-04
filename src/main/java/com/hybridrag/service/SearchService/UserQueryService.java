package com.hybridrag.service.SearchService;

import com.hybridrag.dto.UserQueryResponseDTO;
import com.hybridrag.exception.RagQueryProcessingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserQueryService {

    private static final Logger logger = LoggerFactory.getLogger(UserQueryService.class);

    private final ChromaVectorStore defaultVectorStore;
    private final ChromaVectorStore llmVectorStore;
    private final ChatClient chatClient;
    private final BM25SearchService bm25SearchService;

    public UserQueryService(
            @Qualifier("defaultVectorStore") ChromaVectorStore defaultVectorStore,
            @Qualifier("llmVectorStore") ChromaVectorStore llmVectorStore,
            ChatModel chatModel,
            BM25SearchService bm25SearchService) {
        this.defaultVectorStore = defaultVectorStore;
        this.llmVectorStore = llmVectorStore;
        this.chatClient = ChatClient.create(chatModel);
        this.bm25SearchService = bm25SearchService;
    }

    public UserQueryResponseDTO answerQuestion(String userQuery, boolean useLLMEmbedding, int topK) {
        
        if (userQuery == null || userQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("User query cannot be empty.");
        }

        try {

            ChromaVectorStore selectedStore = useLLMEmbedding ? llmVectorStore : defaultVectorStore;

            List<Document> documents = selectedStore.similaritySearch(
                    SearchRequest.query(userQuery)
                            .withTopK(topK)
                            .withFilterExpression(new FilterExpressionBuilder().ne("dummy", "dummy").build())
            );

            List<String> contextChunks = documents.stream()
                    .map(Document::getContent)
                    .filter(text -> text != null && !text.trim().isEmpty())
                    .collect(Collectors.toList());

            
            for (Document doc : documents) {
                // Spring AI automatically injects the mathematical distance score here!
                Object distanceScore = doc.getMetadata().get("distance"); 
                
                logger.info("Chunk text: {}", doc.getContent());
                logger.info("Accuracy Score (Distance): {}", distanceScore);
            }

            String combinedContext = String.join("\n\n--- Chunk ---\n\n", contextChunks);

            String prompt = String.format(
                    "Answer the question based ONLY on the following context: %s. Question: %s",
                    combinedContext,
                    userQuery
            );

            String answer = chatClient.prompt(new Prompt(prompt)).call().content();
            
            return new UserQueryResponseDTO(
                    200,
                    "Success",
                    answer,
                    userQuery,
                    contextChunks
            );
        } catch (Exception e) {
            throw new RagQueryProcessingException("Failed to process Question-Answering RAG query: " + e.getMessage());
        }
    }

    public UserQueryResponseDTO answerQuestionWithMultiQuery(String userQuery, boolean useLLMEmbedding, int topK) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("User query cannot be empty.");
        }

        try {
            // 1. Ask LLM to generate multiple queries
            String queryGenerationPrompt = String.format(
                    "You are an AI assistant tasked with generating search queries to find relevant information in a vector database. " +
                    "Generate 3 distinct search queries related to the following question. " +
                    "Return only the queries, one per line, without any numbering or extra text.\nQuestion: %s",
                    userQuery
            );
            
            String generatedQueriesStr = chatClient.prompt(new Prompt(queryGenerationPrompt)).call().content();
            List<String> queries = Arrays.stream(generatedQueriesStr.split("\n"))
                                         .map(String::trim)
                                         .filter(s -> !s.isEmpty())
                                         .collect(Collectors.toList());
            
            if (!queries.contains(userQuery.trim())) {
                queries.add(userQuery.trim());
            }

            ChromaVectorStore selectedStore = useLLMEmbedding ? llmVectorStore : defaultVectorStore;
            Set<String> uniqueContextChunks = new LinkedHashSet<>();

            for (String q : queries) {
                List<Document> docs = selectedStore.similaritySearch(
                        SearchRequest.query(q).withTopK(topK).withFilterExpression(new org.springframework.ai.vectorstore.filter.FilterExpressionBuilder().ne("dummy", "dummy").build())
                );

                for (Document doc : docs) {
                    if (doc.getContent() != null && !doc.getContent().trim().isEmpty()) {
                        uniqueContextChunks.add(doc.getContent());
                    }
                }
            }

            List<String> contextChunks = new ArrayList<>(uniqueContextChunks);
            String combinedContext = String.join("\n\n--- Chunk ---\n\n", contextChunks);


            String prompt = String.format(
                    "Answer the question based ONLY on the following context: %s. Question: %s",
                    combinedContext,
                    userQuery
            );

            String answer = chatClient.prompt(new Prompt(prompt)).call().content();
            
            return new UserQueryResponseDTO(
                    200,
                    "Success",
                    answer,
                    userQuery,
                    contextChunks
            );
        } catch (Exception e) {
            throw new RagQueryProcessingException("Failed to process multi-query RAG query: " + e.getMessage());
        }
    }

    public UserQueryResponseDTO answerQuestionWithHybridSearch(String userQuery, boolean useLLMEmbedding, int topK) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("User query cannot be empty.");
        }

        try {
            ChromaVectorStore selectedStore = useLLMEmbedding ? llmVectorStore : defaultVectorStore;

            // 1. Get Top K chunks from ChromaDB (Vector Search)
            List<Document> vectorDocs = selectedStore.similaritySearch(
                    SearchRequest.query(userQuery)
                            .withTopK(topK)
                            .withFilterExpression(new FilterExpressionBuilder().ne("dummy", "dummy").build())
            );

            // 2. Get Top K chunks from BM25 (Lexical Search)
            List<BM25SearchService.BM25Result> bm25Results = bm25SearchService.search(userQuery, topK);

            // 3. Combine both into a Set to automatically remove duplicates
            Set<String> uniqueContextChunks = new LinkedHashSet<>();
            
            for (Document doc : vectorDocs) {
                if (doc.getContent() != null && !doc.getContent().trim().isEmpty()) {
                    uniqueContextChunks.add(doc.getContent().trim());
                    logger.info("Hybrid RAG added ChromaDB Chunk");
                }
            }
            
            for (BM25SearchService.BM25Result res : bm25Results) {
                if (res.getText() != null && !res.getText().trim().isEmpty()) {
                    uniqueContextChunks.add(res.getText().trim());
                    logger.info("Hybrid RAG added BM25 Chunk");
                }
            }

            // 4. Feed all combined unique chunks to Gemini
            List<String> contextChunks = new ArrayList<>(uniqueContextChunks);
            String combinedContext = String.join("\n\n--- Chunk ---\n\n", contextChunks);

            String prompt = String.format(
                    "Answer the question based ONLY on the following context: %s. Question: %s",
                    combinedContext,
                    userQuery
            );

            String answer = chatClient.prompt(new Prompt(prompt)).call().content();
            
            return new UserQueryResponseDTO(
                    200,
                    "Success",
                    answer,
                    userQuery,
                    contextChunks
            );
        } catch (Exception e) {
            throw new RagQueryProcessingException("Failed to process true hybrid RAG query: " + e.getMessage());
        }
    }
}
