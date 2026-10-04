package com.hybridrag.service.ChunksService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChunkManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ChunkManagementService.class);

    @Value("${chromadb.url}")
    private String chromaUrl;

    @Value("${chromadb.collection.default-name}")
    private String defaultCollectionName;

    @Value("${chromadb.collection.llm-name}")
    private String llmCollectionName;

    public void deleteChunkById(String id, boolean useLLMEmbedding) {
        deleteWorkaround(List.of(id), useLLMEmbedding);
    }

    public void deleteChunksBatch(List<String> ids, boolean useLLMEmbedding) {
        deleteWorkaround(ids, useLLMEmbedding);
    }

    private void deleteWorkaround(List<String> ids, boolean useLLMEmbedding) {
        String collectionName = useLLMEmbedding ? llmCollectionName : defaultCollectionName;
        RestTemplate restTemplate = new RestTemplate();
        Map<String,Object> response = restTemplate.getForObject(chromaUrl + "/api/v1/collections/" + collectionName, Map.class);
        if (response != null && response.get("id") != null) {
            String collectionId = (String) response.get("id");
            String deleteUrl = chromaUrl + "/api/v1/collections/" + collectionId + "/delete";
            Map<String, Object> body = new HashMap<>();
            body.put("ids", ids);
            restTemplate.postForObject(deleteUrl, body, String.class);
            logger.info("Successfully deleted via workaround!");
        }
    }

    public Object getAllCollections() {
        RestTemplate restTemplate = new RestTemplate();
        String url = chromaUrl + "/api/v1/collections";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getAllChunks(boolean useLLMEmbedding) {
        String collectionName = useLLMEmbedding ? llmCollectionName : defaultCollectionName;
        RestTemplate restTemplate = new RestTemplate();
        
        Map<String,Object> response = restTemplate.getForObject(chromaUrl + "/api/v1/collections/" + collectionName, Map.class);
        if (response != null && response.get("id") != null) {
            String collectionId = (String) response.get("id");
            String getUrl = chromaUrl + "/api/v1/collections/" + collectionId + "/get";
            Map<String, Object> body = new HashMap<>();
            return restTemplate.postForObject(getUrl, body, Object.class);
        }
        return null;
    }
}
