package com.example.PriceDetector.controller;


import com.example.PriceDetector.dto.AnalysisRequest;
import com.example.PriceDetector.dto.AnalysisResponse;
import com.example.PriceDetector.service.AiClient;
import com.example.PriceDetector.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/analysis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor

public class AnalysisController {
    private final AnalysisService analysisService;

    private final AiClient aiClient;




    @PostMapping("/{id}/analyze")
    public AnalysisResponse runAiAnalysis(@PathVariable Long id) throws Exception {

        return analysisService.runAiAnalysis(id);
    }

    @PostMapping("/{id}/analyze-photo")
    public AnalysisResponse runAiAnalysisWithPhoto(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) throws Exception {

        return analysisService.runAiAnalysisWithPhoto(id,  files);
    }

    @PostMapping
    public ResponseEntity<AnalysisResponse> save(@RequestBody @Valid AnalysisRequest request)
    {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(analysisService.save(request));

    }

    @GetMapping("/{id}")
    public AnalysisResponse getById(@PathVariable Long id)
    {
        return analysisService.getById(id);
    }

    @GetMapping
    public List<AnalysisResponse> getAll()
    {
        return analysisService.getAll();
    }
}
