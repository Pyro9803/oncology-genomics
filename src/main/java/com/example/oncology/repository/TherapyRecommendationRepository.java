package com.example.oncology.repository;

import com.example.oncology.entity.TherapyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TherapyRecommendationRepository extends JpaRepository<TherapyRecommendation, Long> {
    
    List<TherapyRecommendation> findByClinicalInterpretationInterpretationId(Long interpretationId);
    
    List<TherapyRecommendation> findByTherapyNameContainingIgnoreCase(String therapyName);
    
    List<TherapyRecommendation> findByTherapyType(String therapyType);
    
    List<TherapyRecommendation> findByEvidenceLevel(String evidenceLevel);
    
    @Query("SELECT tr FROM TherapyRecommendation tr " +
           "WHERE tr.clinicalInterpretation.somaticVariant.variantCalling.tumorSample.patient.patientId = :patientId")
    List<TherapyRecommendation> findByPatientId(Long patientId);
    
    @Query("SELECT tr FROM TherapyRecommendation tr " +
           "JOIN tr.clinicalInterpretation ci " +
           "JOIN ci.somaticVariant sv " +
           "WHERE sv.geneSymbol = :geneSymbol")
    List<TherapyRecommendation> findByGene(String geneSymbol);
    
    @Query("SELECT tr FROM TherapyRecommendation tr " +
           "JOIN tr.clinicalInterpretation ci " +
           "JOIN ci.diagnosis d " +
           "WHERE d.cancerType = :cancerType")
    List<TherapyRecommendation> findByCancerType(String cancerType);
}
