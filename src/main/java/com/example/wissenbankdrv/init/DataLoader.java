package com.example.wissenbankdrv.init;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner load(VectorStore store) {
        return args -> {
            store.add(List.of(
                    new Document("Katzen sind tolle Haustiere. Sie sind unabhängig und verspielt.",
                            Map.of("source","seed","topic","katzen")),
                    new Document("Spring Boot vereinfacht die Java-Webentwicklung mit Auto-Configuration.",
                            Map.of("source","seed","topic","spring")),
                    new Document("Deutschland ist ein Land in Europa. Hauptstadt ist Berlin.",
                            Map.of("source","seed","topic","deutschland"))
            ));
            System.out.println(">> Seed-Dokumente eingebettet.");
        };
    }
}
