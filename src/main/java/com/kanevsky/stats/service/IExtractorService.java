package com.kanevsky.stats.service;

import com.kanevsky.stats.IngestException;
import jakarta.servlet.ServletInputStream;

import java.io.InputStream;

public interface IExtractorService {
    long ingestInputStream(InputStream inputStream) throws IngestException;
}
