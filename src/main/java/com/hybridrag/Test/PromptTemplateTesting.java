package com.hybridrag.Test;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service 
public class PromptTemplateTesting {

    private final ChatModel chatModel;

    public PromptTemplateTesting(ChatModel chatModel){
        this.chatModel = chatModel;
    }

    public String getFoodDetailsData(String input){
        String proptDescription="You are helpfull assistatn , Give all the details about nutrition for the given food items {input} and return the response in formated way like price, protein, carbs, fat, calories";
        PromptTemplate promptTemplate = new PromptTemplate(proptDescription);
        Prompt prompt=promptTemplate.create(Map.of("input",input));
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }

    public String practiceRoles() {
        Message systemMessage = new SystemMessage("You are a sarcastic coding tutor.");
        Message userMessage = new UserMessage("What is a boolean?");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }

    public String practiceCustomDelimiters(String topic) {

        String template = "Tell me 3 facts about {topic}. Return the output as JSON using {jsonFormat} format.";
        
        String myJsonFormat = "{ \"facts\": [] }";
        
        PromptTemplate promptTemplate = new PromptTemplate(template);
        
        Prompt prompt = promptTemplate.create(Map.of(
            "topic", topic,
            "jsonFormat", myJsonFormat
        ));
        ChatResponse response = chatModel.call(prompt);
        String answer = response.getResult().getOutput().getContent();
        Usage usage = response.getMetadata().getUsage();
        long promptTokens = usage.getPromptTokens();
        long generationTokens = usage.getGenerationTokens();
        long totalTokens = usage.getTotalTokens();
        
        return "Answer: " + answer + "\n\n" + 
               "Tokens Used -> Prompt: " + promptTokens + 
               ", Generated: " + generationTokens + 
               ", Total: " + totalTokens;
    }
}
