package com.hybridrag.Test;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hybridrag.dto.Food;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/llm")
public class UserAskController {

    private final ChatClient defaultChatClient;
    private final ChatClient foodAgentChatClient;

    public UserAskController(
            @Qualifier("defaultChatClient") ChatClient defaultChatClient,
            @Qualifier("foodAgentChatClient") ChatClient foodAgentChatClient) {
        this.defaultChatClient = defaultChatClient;
        this.foodAgentChatClient = foodAgentChatClient;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String message, @RequestParam(defaultValue = "false") boolean useFoodAgent) {
        ChatClient client = useFoodAgent ? foodAgentChatClient : defaultChatClient;
        return client.prompt().user(message).call().content();
    }

    @GetMapping("/fooddetails")
    public List<Food> getBooks(@RequestParam String message) {
        return foodAgentChatClient.prompt()
                .user(message)
                .call()
                .entity(new ParameterizedTypeReference<List<Food>>() {});
    }
}
