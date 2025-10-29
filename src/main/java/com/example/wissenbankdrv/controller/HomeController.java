package com.example.wissenbankdrv.controller;


import com.example.wissenbankdrv.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
//            answer = chatClient
//                    .prompt()
//                    .system("""
//            Du bist ein wissensbasiertes Assistenzsystem.
//            Verwende **ausschließlich** die folgenden Informationen aus der Wissensdatenbank (Context),
//            wenn du die Frage beantwortest und verbessere sie nicht.
//            Antworte nur auf Basis des Contexts. Wenn der Context keine passende Information enthält,
//            gib bitte an: "Ich habe dazu keine Informationen in der Wissensdatenbank."
//
//            Context:
//            """ + context)
//                    .user(prompt)
//                    .call()
//                    .content();

            answer = chatClient
                    .prompt()
                    .system("Nutze, **ausschließlich**, folgendes Wissen:\n" + context)
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

    @PostMapping("/add")
    public String addPdf(@RequestParam("file")MultipartFile file){
        try {
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(file.getResource());
            List<Document> documents = pdfReader.get();
            this.vectorStore.add(documents);
            return "200";
        }
        catch (Exception e){
            System.err.println(e);
            return  "400";
        }

    }
    // einfacher Healthcheck
    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }


}
