package com.hybridrag.exception;

import com.hybridrag.dto.UserQueryResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice
public class RagExceptionHandler {

    @ExceptionHandler(RagQueryProcessingException.class)
    public ResponseEntity<UserQueryResponseDTO> handleRagQueryProcessingException(RagQueryProcessingException ex) {
        UserQueryResponseDTO response = new UserQueryResponseDTO(
                ex.getStatusCode(),
                "RAG Processing Error: " + ex.getMessage(),
                null,
                null,
                Collections.emptyList()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UserQueryResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        UserQueryResponseDTO response = new UserQueryResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request: " + ex.getMessage(),
                null,
                null,
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
