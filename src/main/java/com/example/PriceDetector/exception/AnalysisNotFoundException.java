package com.example.PriceDetector.exception;

public class AnalysisNotFoundException extends RuntimeException
{
    public AnalysisNotFoundException(Long id)
    {
        super("Analisys of id not found" + id);
    }
}
