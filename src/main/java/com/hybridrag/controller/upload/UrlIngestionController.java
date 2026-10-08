package com.hybridrag.controller.upload;

import com.hybridrag.service.UploadService.UrlIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") 
@RequestMapping("/api/v1/documents")
public class UrlIngestionController {

    private static final Logger logger = LoggerFactory.getLogger(UrlIngestionController.class);
    
    private final UrlIngestionService urlIngestionService;

    public UrlIngestionController(UrlIngestionService urlIngestionService) {
        this.urlIngestionService = urlIngestionService;
    }

    @PostMapping("/test-url")
    public ResponseEntity<Map<String, Object>> testUrlScraping(
            @RequestParam("url") String url,
            @RequestParam(value = "selector", defaultValue = "body") String selector) {
        try {
            logger.info("Received request to test URL scraping: {} with selector: {}", url, selector);
            Map<String, Object> response = urlIngestionService.testUrlScraping(url, selector);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error scraping URL: {}", url, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Failed to scrape URL: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
