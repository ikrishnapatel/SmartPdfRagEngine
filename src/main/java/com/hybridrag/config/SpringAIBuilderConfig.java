package com.hybridrag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class SpringAIBuilderConfig {

    @Bean(name = "defaultChatClient")
    public ChatClient defaultChatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean(name = "foodAgentChatClient")
    public ChatClient foodAgentChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("You are an agent that only answers food-related questions. For other queries, do not answer and explain that you are restricted to food topics. Always provide the usual price formatted in Indian Rupees (e.g., Rs. 500).").build();
    }

    
}
