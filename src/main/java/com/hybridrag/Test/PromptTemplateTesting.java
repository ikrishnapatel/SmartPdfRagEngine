package com.hybridrag.Test;

import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service 
public class PromptTemplateTesting {

    private ChatModel chatModel;

    public PromptTemplateTesting(ChatModel chatModel){
        this.chatModel=chatModel;
    }

    public String getFoodDetailsData(String input){

        String proptDescription="You are helpfull assistatn , Give all the details about nutrition for the given food items {input} and return the response in formated way like price, protein, carbs, fat, calories";

        PromptTemplate promptTemplate = new PromptTemplate(proptDescription);

        Prompt prompt=promptTemplate.create(Map.of("input",input));
        

        String answer= chatModel.call(prompt).getResult().getOutput().getContent();

        return answer;
    }

}
