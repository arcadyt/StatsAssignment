package com.kanevsky.stats.exceptions;

public class IngestException extends RuntimeException {
    public IngestException() {
        super();
    }
    
    public IngestException(String message) {
        super(message);
    }
    
    public IngestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public IngestException(Throwable cause) {
        super(cause);
    }
}