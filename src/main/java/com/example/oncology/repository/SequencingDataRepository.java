package com.example.oncology.repository;

import com.example.oncology.entity.Sample;
import com.example.oncology.entity.SequencingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SequencingDataRepository extends JpaRepository<SequencingData, Long> {
    
    // Find sequencing data by sample ID
    List<SequencingData> findBySampleSampleId(Long sampleId);
    
    // Find sequencing data by sample
    List<SequencingData> findBySample(Sample sample);
    
    List<SequencingData> findByPlatform(String platform);
    
    List<SequencingData> findBySequencingType(String sequencingType);
    
    List<SequencingData> findByMeanCoverageGreaterThanEqual(Double minCoverage);
}
