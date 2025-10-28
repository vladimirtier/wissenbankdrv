package com.example.wissenbankdrv.model;

public class ChatResponse {

    private final String prompt;
    private final String answer;

    public ChatResponse(String prompt, String answer) {
        this.prompt = prompt;
        this.answer = answer;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getAnswer() {
        return answer;
    }
}
