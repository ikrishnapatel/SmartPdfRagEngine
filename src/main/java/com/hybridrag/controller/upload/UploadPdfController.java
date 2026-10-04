package com.hybridrag.controller.upload;

import com.hybridrag.service.PdfDocumentSplitterService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*") 
@RequestMapping("/api/v1/documents")
public class UploadPdfController {

    private static final Logger logger = LoggerFactory.getLogger(UploadPdfController.class);

    private final PdfDocumentSplitterService ingestionService;

    public UploadPdfController(PdfDocumentSplitterService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload-doc")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "useLLMEmbedding", defaultValue = "true") boolean useLLMEmbedding) {
        
        try {
            logger.info("Uploading document: {}", file.getOriginalFilename());
            Map<String, Object> response = ingestionService.processAndIngestDocument(file, useLLMEmbedding);
            
            if (response.containsKey("status") && (Integer) response.get("status") == 200) {
                logger.info("Document processed successfully: {}", file.getOriginalFilename());
                return ResponseEntity.ok(response);

            } else {
                int status = response.containsKey("status") ? (Integer) response.get("status") : 500;
                return ResponseEntity.status(status).body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing document: {}", file.getOriginalFilename(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Failed to process document: " + e.toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}
