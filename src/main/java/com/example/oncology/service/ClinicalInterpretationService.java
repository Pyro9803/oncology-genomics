package com.example.oncology.service;

import com.example.oncology.entity.*;
import com.example.oncology.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for clinical interpretation of somatic variants
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicalInterpretationService {

    private final SomaticVariantRepository somaticVariantRepository;
    private final ClinicalInterpretationRepository clinicalInterpretationRepository;
    private final TherapyRecommendationRepository therapyRecommendationRepository;
    private final DiagnosisRepository diagnosisRepository;
    
    /**
     * Interpret a somatic variant for clinical significance
     * @param variantId ID of the variant to interpret
     * @param diagnosisId ID of the diagnosis context
     * @param pathogenicity Pathogenicity classification
     * @param diagnosticSignificance Diagnostic significance
     * @param prognosticSignificance Prognostic significance
     * @param therapeuticSignificance Therapeutic significance
     * @param evidenceLevel Level of evidence
     * @param evidenceSources Sources of evidence
     * @param interpretationSummary Summary of interpretation
     * @param notes Additional notes
     * @param interpretedBy Name of the interpreter
     * @return The created clinical interpretation
     */
    @Transactional
    public ClinicalInterpretation interpretVariant(
            Long variantId,
            Long diagnosisId,
            String pathogenicity,
            String diagnosticSignificance,
            String prognosticSignificance,
            String therapeuticSignificance,
            String evidenceLevel,
            String evidenceSources,
            String interpretationSummary,
            String notes,
            String interpretedBy) {
        
        SomaticVariant variant = somaticVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));
        
        Diagnosis diagnosis = null;
        if (diagnosisId != null) {
            diagnosis = diagnosisRepository.findById(diagnosisId)
                    .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));
        }
        
        // Check if interpretation already exists
        Optional<ClinicalInterpretation> existingInterpretation = 
                clinicalInterpretationRepository.findBySomaticVariantVariantId(variantId)
                        .stream()
                        .filter(ci -> (diagnosisId == null && ci.getDiagnosis() == null) || 
                                (ci.getDiagnosis() != null && ci.getDiagnosis().getDiagnosisId().equals(diagnosisId)))
                        .findFirst();
        
        ClinicalInterpretation interpretation;
        if (existingInterpretation.isPresent()) {
            // Update existing interpretation
            interpretation = existingInterpretation.get();
            interpretation.setPathogenicity(pathogenicity);
            interpretation.setDiagnosticSignificance(diagnosticSignificance);
            interpretation.setPrognosticSignificance(prognosticSignificance);
            interpretation.setTherapeuticSignificance(therapeuticSignificance);
            interpretation.setEvidenceLevel(evidenceLevel);
            interpretation.setEvidenceSources(evidenceSources);
            interpretation.setInterpretationSummary(interpretationSummary);
            interpretation.setNotes(notes);
            interpretation.setInterpretedBy(interpretedBy);
            interpretation.setInterpretationDate(LocalDateTime.now());
        } else {
            // Create new interpretation
            interpretation = new ClinicalInterpretation();
            interpretation.setSomaticVariant(variant);
            interpretation.setDiagnosis(diagnosis);
            interpretation.setPathogenicity(pathogenicity);
            interpretation.setDiagnosticSignificance(diagnosticSignificance);
            interpretation.setPrognosticSignificance(prognosticSignificance);
            interpretation.setTherapeuticSignificance(therapeuticSignificance);
            interpretation.setEvidenceLevel(evidenceLevel);
            interpretation.setEvidenceSources(evidenceSources);
            interpretation.setInterpretationSummary(interpretationSummary);
            interpretation.setNotes(notes);
            interpretation.setInterpretedBy(interpretedBy);
            interpretation.setInterpretationDate(LocalDateTime.now());
        }
        
        return clinicalInterpretationRepository.save(interpretation);
    }
    
    /**
     * Add a therapy recommendation for a clinical interpretation
     * @param interpretationId ID of the clinical interpretation
     * @param therapyName Name of the therapy
     * @param therapyType Type of therapy
     * @param evidenceLevel Level of evidence
     * @param evidenceSource Source of evidence
     * @param referenceId Reference ID (e.g., clinical trial ID)
     * @param recommendationSummary Summary of recommendation
     * @param contraindications Contraindications
     * @param notes Additional notes
     * @return The created therapy recommendation
     */
    @Transactional
    public TherapyRecommendation addTherapyRecommendation(
            Long interpretationId,
            String therapyName,
            String therapyType,
            String evidenceLevel,
            String evidenceSource,
            String referenceId,
            String recommendationSummary,
            String contraindications,
            String notes) {
        
        ClinicalInterpretation interpretation = clinicalInterpretationRepository.findById(interpretationId)
                .orElseThrow(() -> new IllegalArgumentException("Clinical interpretation not found: " + interpretationId));
        
        // Check if recommendation already exists
        Optional<TherapyRecommendation> existingRecommendation = 
                therapyRecommendationRepository.findByClinicalInterpretationInterpretationId(interpretationId)
                        .stream()
                        .filter(tr -> tr.getTherapyName().equalsIgnoreCase(therapyName))
                        .findFirst();
        
        TherapyRecommendation recommendation;
        if (existingRecommendation.isPresent()) {
            // Update existing recommendation
            recommendation = existingRecommendation.get();
            recommendation.setTherapyType(therapyType);
            recommendation.setEvidenceLevel(evidenceLevel);
            recommendation.setEvidenceSource(evidenceSource);
            recommendation.setReferenceId(referenceId);
            recommendation.setRecommendationSummary(recommendationSummary);
            recommendation.setContraindications(contraindications);
            recommendation.setNotes(notes);
        } else {
            // Create new recommendation
            recommendation = new TherapyRecommendation();
            recommendation.setClinicalInterpretation(interpretation);
            recommendation.setTherapyName(therapyName);
            recommendation.setTherapyType(therapyType);
            recommendation.setEvidenceLevel(evidenceLevel);
            recommendation.setEvidenceSource(evidenceSource);
            recommendation.setReferenceId(referenceId);
            recommendation.setRecommendationSummary(recommendationSummary);
            recommendation.setContraindications(contraindications);
            recommendation.setNotes(notes);
        }
        
        return therapyRecommendationRepository.save(recommendation);
    }
    
    /**
     * Find variants with potential clinical significance for a patient
     * @param patientId ID of the patient
     * @return List of variants with potential clinical significance
     */
    public List<SomaticVariant> findClinicallySignificantVariants(Long patientId) {
        return somaticVariantRepository.findByPatientId(patientId);
    }
    
    /**
     * Find therapy recommendations for a patient
     * @param patientId ID of the patient
     * @return List of therapy recommendations
     */
    public List<TherapyRecommendation> findTherapyRecommendations(Long patientId) {
        return therapyRecommendationRepository.findByPatientId(patientId);
    }
    
    /**
     * Find therapy recommendations for a specific cancer type
     * @param cancerType Type of cancer
     * @return List of therapy recommendations
     */
    public List<TherapyRecommendation> findTherapyRecommendationsByCancerType(String cancerType) {
        return therapyRecommendationRepository.findByCancerType(cancerType);
    }
    
    /**
     * Find therapy recommendations for a specific gene
     * @param geneSymbol Gene symbol
     * @return List of therapy recommendations
     */
    public List<TherapyRecommendation> findTherapyRecommendationsByGene(String geneSymbol) {
        return therapyRecommendationRepository.findByGene(geneSymbol);
    }
}
