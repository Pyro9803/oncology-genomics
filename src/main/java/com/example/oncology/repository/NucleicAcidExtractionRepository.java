package com.example.oncology.repository;

import com.example.oncology.entity.NucleicAcidExtraction;
import com.example.oncology.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NucleicAcidExtractionRepository extends JpaRepository<NucleicAcidExtraction, Long> {
    
    List<NucleicAcidExtraction> findBySample(Sample sample);
    
    List<NucleicAcidExtraction> findByExtractionType(String extractionType);
}
