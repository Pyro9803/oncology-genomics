package com.example.oncology.repository;

import com.example.oncology.entity.ClinicalInterpretation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalInterpretationRepository extends JpaRepository<ClinicalInterpretation, Long> {
    
    List<ClinicalInterpretation> findBySomaticVariantVariantId(Long variantId);
    
    List<ClinicalInterpretation> findByDiagnosisDiagnosisId(Long diagnosisId);
    
    List<ClinicalInterpretation> findByPathogenicity(String pathogenicity);
    
    List<ClinicalInterpretation> findByEvidenceLevel(String evidenceLevel);
    
    @Query("SELECT ci FROM ClinicalInterpretation ci WHERE ci.therapeuticSignificance IS NOT NULL")
    List<ClinicalInterpretation> findWithTherapeuticSignificance();
    
    @Query("SELECT ci FROM ClinicalInterpretation ci " +
           "WHERE ci.somaticVariant.variantCalling.tumorSample.patient.patientId = :patientId")
    List<ClinicalInterpretation> findByPatientId(Long patientId);
    
    @Query("SELECT ci FROM ClinicalInterpretation ci " +
           "JOIN ci.somaticVariant sv WHERE sv.geneSymbol = :geneSymbol")
    List<ClinicalInterpretation> findByGene(String geneSymbol);
}
