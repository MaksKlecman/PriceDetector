package com.example.PriceDetector.service;

import com.example.PriceDetector.dto.AnalysisMapper;
import com.example.PriceDetector.dto.AnalysisRequest;
import com.example.PriceDetector.dto.AnalysisResponse;
import com.example.PriceDetector.exception.AnalysisNotFoundException;
import com.example.PriceDetector.model.Analysis;
import com.example.PriceDetector.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository repository;
    private final AnalysisMapper mapper;

    @Transactional
    public AnalysisResponse save(AnalysisRequest request)
    {
      Analysis analysis = mapper.toEntity(request);
      return mapper.toResponse(repository.save(analysis));

    }

    public AnalysisResponse getById(Long id)
    {
        return mapper.toResponse(getAnalysis(id));
    }
    @Transactional
    public List<AnalysisResponse> getAll()
    {
        return mapper.toResponseList(repository.findAll());
    }

    private Analysis getAnalysis(Long id)
    {
        return repository.findById(id)
            .orElseThrow(() -> new AnalysisNotFoundException(id));
    }


}
