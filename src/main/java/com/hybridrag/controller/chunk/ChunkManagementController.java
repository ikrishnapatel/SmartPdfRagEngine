package com.hybridrag.controller.chunk;

import com.hybridrag.service.ChunkManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/documents/chunks")
public class ChunkManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ChunkManagementController.class);

    private final ChunkManagementService chunkManagementService;

    public ChunkManagementController(ChunkManagementService chunkManagementService) {
        this.chunkManagementService = chunkManagementService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChunkById(
            @PathVariable("id") String id,
            @RequestParam(value = "useLLMEmbedding", defaultValue = "false") boolean useLLMEmbedding) {
        try {
            logger.info("Deleting chunk with ID: {}", id);
            chunkManagementService.deleteChunkById(id, useLLMEmbedding);
            return ResponseEntity.ok("Chunk with ID '" + id + "' deleted successfully from ChromaDB.");
        } catch (Exception e) {
            logger.error("Failed to delete chunk with ID: {}", id, e);
            return ResponseEntity.internalServerError().body("Failed to delete chunk: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<String> deleteChunksBatch(
            @RequestBody List<String> ids,
            @RequestParam(value = "useLLMEmbedding", defaultValue = "true") boolean useLLMEmbedding) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("List of IDs to delete cannot be empty.");
        }
        try {
            logger.info("Deleting {} chunks in batch", ids.size());
            chunkManagementService.deleteChunksBatch(ids, useLLMEmbedding);
            return ResponseEntity.ok("Successfully deleted " + ids.size() + " chunks from ChromaDB.");
        } catch (Exception e) {
            logger.error("Failed to delete batch chunks", e);
            return ResponseEntity.internalServerError().body("Failed to delete batch chunks: " + e.getMessage());
        }
    }

    @GetMapping("/collections")
    public ResponseEntity<Object> getAllCollections() {
        try {
            logger.info("Fetching all ChromaDB collections");
            Object collections = chunkManagementService.getAllCollections();
            return ResponseEntity.ok(collections);
        } catch (Exception e) {
            logger.error("Failed to fetch collections", e);
            return ResponseEntity.internalServerError().body("Failed to fetch collections: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllChunks(
            @RequestParam(value = "useLLMEmbedding", defaultValue = "true") boolean useLLMEmbedding) {
        try {
            logger.info("Fetching chunks for collection (useLLMEmbedding={})", useLLMEmbedding);
            Object chunks = chunkManagementService.getAllChunks(useLLMEmbedding);
            return ResponseEntity.ok(chunks);
        } catch (Exception e) {
            logger.error("Failed to fetch chunks", e);
            return ResponseEntity.internalServerError().body("Failed to fetch chunks: " + e.getMessage());
        }
    }
}
