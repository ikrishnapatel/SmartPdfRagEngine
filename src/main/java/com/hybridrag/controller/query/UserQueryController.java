package com.hybridrag.controller.query;

import com.hybridrag.dto.UserQueryResponseDTO;
import com.hybridrag.service.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class UserQueryController {

    private final UserQueryService userQueryService;

    public UserQueryController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/ask-query")
    public ResponseEntity<UserQueryResponseDTO> askQuestion(
            @RequestParam("query") String query,
            @RequestParam(value = "useLLMEmbedding", defaultValue = "true") boolean useLLMEmbedding,
            @RequestParam(value = "topK", defaultValue = "2") int topK) {

        UserQueryResponseDTO response = userQueryService.answerQuestion(query, useLLMEmbedding, topK);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ask-query-multi")
    public ResponseEntity<UserQueryResponseDTO> askQuestionMulti(
            @RequestParam("query") String query,
            @RequestParam(value = "useLLMEmbedding", defaultValue = "true") boolean useLLMEmbedding,
            @RequestParam(value = "topK", defaultValue = "2") int topK) {

        UserQueryResponseDTO response = userQueryService.answerQuestionWithMultiQuery(query, useLLMEmbedding, topK);
        return ResponseEntity.ok(response);
    }
}
