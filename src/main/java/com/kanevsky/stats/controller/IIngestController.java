package com.kanevsky.stats.controller;

import com.kanevsky.stats.dto.StatsBatchRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

public interface IIngestController {
   void ingest(StatsBatchRequestDto request);
}

