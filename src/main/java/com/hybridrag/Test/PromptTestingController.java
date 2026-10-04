package com.hybridrag.Test;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/prompttesting")
public class PromptTestingController {

    private final PromptTemplateTesting promptTemplateTesting;

    PromptTestingController(PromptTemplateTesting promptTemplateTesting) {
        this.promptTemplateTesting = promptTemplateTesting;
    }   

    @GetMapping("/test")
    public String getFoodDetailsData(@RequestParam("input") String input){
        return promptTemplateTesting.practiceCustomDelimiters(input);
    }


    
}
