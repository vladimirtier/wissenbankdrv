package com.example.wissenbankdrv.controller;


import com.example.wissenbankdrv.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class HomeController {



    private final ChatClient chatClient;


    // ChatClient.Builder wird vom Spring-AI-Starter automatisch bereitgestellt

    private final VectorStore vectorStore;
    public HomeController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }


    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html
    }

    // GET /ask -> nur Redirect (wenn man versehentlich /ask direkt im Browser aufruft)
    @GetMapping("/ask")
    public String askGet() {
        return "redirect:/";
    }

    // POST /ask -> hier kommt das Formular an
    @PostMapping("/ask")
    public String ask(@RequestParam("prompt") String prompt, Model model) {
        // Anfrage an das Ollama-/AI-Backend
        String answer;
        try {
            // 1) ähnliche Dokumente holen
            List<Document> hits = vectorStore.similaritySearch(
                    SearchRequest.builder().query(prompt).topK(5).build()
            );
            String context = hits.stream()
                    .map(d -> "- " + d.getFormattedContent())
                    .reduce("", (a,b) -> a + "\n" + b);

            // 2) Chat mit Kontext
            answer = chatClient
                    .prompt()
                    .system("Nutze, wenn passend, folgendes Wissen:\n" + context)
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            answer = "Fehler beim Abrufen: " + e.getMessage();
        }

        // Antwort + Prompt ins Model, damit Thymeleaf es anzeigen kann
        model.addAttribute("chat", new ChatResponse(prompt, answer));
        return "index"; // dieselbe Seite erneut rendern
    }

    // einfacher Healthcheck
    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }


}
