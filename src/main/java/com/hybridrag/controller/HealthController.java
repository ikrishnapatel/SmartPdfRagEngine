package com.hybridrag.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
public class HealthController {

    @Value("${chromadb.url}")
    private String chromaUrl;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Check Spring Boot Server
        response.put("server", "UP");
        response.put("message", "Hybrid RAG Server is up and running.");

        // 2. Check ChromaDB vector database connection
        try {
            RestTemplate restTemplate = new RestTemplate();
            // ChromaDB exposes a heartbeat endpoint to verify it is alive
            Map chromaHeartbeat = restTemplate.getForObject(chromaUrl + "/api/v1/heartbeat", Map.class);
            if (chromaHeartbeat != null && chromaHeartbeat.containsKey("nanosecond heartbeat")) {
                response.put("chromadb", "UP");
            } else {
                response.put("chromadb", "UNKNOWN");
            }
        } catch (Exception e) {
            response.put("chromadb", "DOWN");
            response.put("chromadb_error", e.getMessage());
            
            // If ChromaDB is essential, you might want to return 503 Service Unavailable here, 
            // but returning 200 with the details allows the frontend to gracefully show what is broken.
        }

        return ResponseEntity.ok(response);
    }
}
