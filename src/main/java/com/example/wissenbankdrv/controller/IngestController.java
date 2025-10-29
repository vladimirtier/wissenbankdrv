package com.example.wissenbankdrv.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ingest")
public class IngestController {

    private final VectorStore vectorStore;

    public IngestController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping(
            value = "/pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> addPdf(@RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "file is empty"));
            }

            // MultipartFile.getResource() ist nicht immer seekable.
            // ByteArrayResource ist sicherer:
            ByteArrayResource res = new ByteArrayResource(file.getBytes()) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            };

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(res);
            List<Document> docs = pdfReader.get();

            vectorStore.add(docs);

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "filename", file.getOriginalFilename(),
                    "pagesStored", docs.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
