package com.kanevsky.stats.controller;

import com.kanevsky.stats.dto.StatsBatchRequestDto;
import com.kanevsky.stats.exceptions.IngestException;
import com.kanevsky.stats.service.IngestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
public class IngestController implements IIngestController {

    @Autowired
    private IngestService ingestService;

    @PostMapping("/")
    @Override
    public void ingest(@Valid @RequestBody StatsBatchRequestDto request) {
        try {
            ingestService.processBatchEntries(request.getEntries());
        } catch (Exception e) {
            throw new IngestException("Failed to ingest stats: " + e.getMessage(), e);
        }
    }
}