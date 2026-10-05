package com.hybridrag.service.helper;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;

import java.util.ArrayList;
import java.util.List;

public class CustomOverlappingSplitter implements DocumentTransformer {

    private final int chunkSize;
    private final int overlapSize;

    public CustomOverlappingSplitter() {
        this.chunkSize = 1500;
        this.overlapSize = 250;
    }

    public CustomOverlappingSplitter(int chunkSize, int overlapSize) {
        if (overlapSize >= chunkSize) {
            throw new IllegalArgumentException("Overlap size must be less than chunk size");
        }
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }

    @Override
    public List<Document> apply(List<Document> documents) {
        List<Document> splitDocuments = new ArrayList<>();

        for (Document document : documents) {
            String text = document.getContent();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }
            
            // Sanitize text: replace all multiple whitespaces/newlines with a single space
            text = text.replaceAll("\\s+", " ").trim();

            int length = text.length();
            int start = 0;

            while (start < length) {
                int end = Math.min(start + chunkSize, length);
                String chunkContent = text.substring(start, end);
                Document chunkDoc = new Document(chunkContent, document.getMetadata());
                splitDocuments.add(chunkDoc);
                start += (chunkSize - overlapSize);
            }
        }

        return splitDocuments;
    }
}
