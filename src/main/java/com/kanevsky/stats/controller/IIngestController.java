package com.kanevsky.stats.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ResponseBody;

public interface IIngestController {
    long ingest(HttpServletRequest request);
}

