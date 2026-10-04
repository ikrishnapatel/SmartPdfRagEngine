package com.hybridrag.service.UploadService;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hybridrag.service.SearchService.BM25SearchService;
import com.hybridrag.service.helper.CustomOverlappingSplitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PdfDocumentSplitterService {

    private static final Logger logger = LoggerFactory.getLogger(PdfDocumentSplitterService.class);

    private final ChromaVectorStore defaultVectorStore;
    private final ChromaVectorStore llmVectorStore;
    private final BM25SearchService bm25SearchService;

    @org.springframework.beans.factory.annotation.Value("${chromadb.url}")
    private String chromaUrl;

    @org.springframework.beans.factory.annotation.Value("${chromadb.collection.default-name}")
    private String defaultCollectionName;

    @org.springframework.beans.factory.annotation.Value("${chromadb.collection.llm-name}")
    private String llmCollectionName;

    public PdfDocumentSplitterService(
            @Qualifier("defaultVectorStore") ChromaVectorStore defaultVectorStore,
            @Qualifier("llmVectorStore") ChromaVectorStore llmVectorStore,
            BM25SearchService bm25SearchService) {
        this.defaultVectorStore = defaultVectorStore;
        this.llmVectorStore = llmVectorStore;
        this.bm25SearchService = bm25SearchService;
    }

    public Map<String, Object> processAndIngestDocument(MultipartFile file, boolean useLLMEmbedding) throws Exception {

        List<Document> documents = parseDocument(file);
        CustomOverlappingSplitter splitter = new CustomOverlappingSplitter(1500, 250);
        List<Document> segments = splitter.apply(documents);

        logger.info("Document split into {} chunks.", segments.size());
        
        ChromaVectorStore selectedStore = useLLMEmbedding ? llmVectorStore : defaultVectorStore;

        logger.info("Generating embeddings using Spring AI and adding to ChromaDB");
        selectedStore.add(segments);
        bm25SearchService.indexSegments(segments);
        logger.info("Embeddings saved to ChromaDB and segments indexed into BM25!");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Document processed successfully! Embeddings generated.");
        return response;
        
    }

    private List<Document> parseDocument(MultipartFile file) throws Exception {
        try {
            Resource resource = file.getResource();
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource,
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                    .withNumberOfBottomTextLinesToDelete(0)
                                    .withNumberOfTopPagesToSkipBeforeDelete(0)
                                    .build())
                            .withPagesPerDocument(1)
                            .build());
            return pdfReader.get();
        } catch (Exception e) {
            logger.error("Error occurred while parsing the document: {}", file.getOriginalFilename(), e);
            throw new Exception("Failed to parse document: " + file.getOriginalFilename(), e);
        }
    }
}
