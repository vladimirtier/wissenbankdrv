package com.example.wissenbankdrv.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ingest")
public class IngestController {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbc;

    // Passe ggfs. Schema/Tabelle an, falls du andere Namen verwendest
    private static final String TABLE = "public.document_embeddings";

    public IngestController(VectorStore vectorStore, JdbcTemplate jdbc) {
        this.vectorStore = vectorStore;
        this.jdbc = jdbc;
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addPdf(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        try {
            if (file.isEmpty()) {
                redirect.addFlashAttribute("uploadError", "❌ Datei ist leer.");
                return "redirect:/";
            }

            // Sicheres Resource-Handling aus Multipart
            ByteArrayResource res = new ByteArrayResource(file.getBytes()) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            };

            // Seiten extrahieren (kein OCR – nur „echter“ PDF-Text)
            PagePdfDocumentReader reader = new PagePdfDocumentReader(res);
            List<Document> docs = reader.get();

            // Nur Seiten mit Text speichern (vermeidet leere Einträge)
            List<Document> nonEmpty = docs.stream()
                    .filter(d -> d.getFormattedContent() != null && !d.getFormattedContent().isBlank())
                    .toList();

            if (nonEmpty.isEmpty()) {
                redirect.addFlashAttribute("uploadError",
                        "❌ Keine extrahierbaren Textinhalte gefunden (PDF evtl. gescannt ohne OCR).");
                return "redirect:/";
            }

            vectorStore.add(nonEmpty);

            // Gesamtzahl in der DB (optional, für Statusbox)
            Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM " + TABLE, Integer.class);

            redirect.addFlashAttribute("uploadSuccess",
                    "✅ \"" + file.getOriginalFilename() + "\" gespeichert – " + nonEmpty.size() + " Seite(n) eingebettet.");
            redirect.addFlashAttribute("kbTotal", total != null ? total : 0);

        } catch (Exception e) {
            redirect.addFlashAttribute("uploadError", "❌ Upload fehlgeschlagen: " + e.getMessage());
        }
        // Zur Startseite zurück – Seite bleibt offen, bereit für neue Anfragen/Uploads
        return "redirect:/";
    }
}
