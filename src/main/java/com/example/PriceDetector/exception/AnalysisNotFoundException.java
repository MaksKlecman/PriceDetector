package com.example.PriceDetector.exception;

public class AnalysisNotFoundException extends RuntimeException
{
    public AnalysisNotFoundException(Long id)
    {
        super("Analysis with id " + id + " not found");
    }
}
