package com.example.PriceDetector.repository;


import com.example.PriceDetector.model.Analysis;
import com.example.PriceDetector.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long>
{


}
