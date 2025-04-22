package com.example.oncology.controller;

import com.example.oncology.entity.ClinicalInterpretation;
import com.example.oncology.entity.SomaticVariant;
import com.example.oncology.entity.TherapyRecommendation;
import com.example.oncology.service.ClinicalInterpretationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for clinical interpretation operations
 */
@RestController
@RequestMapping("/clinical")
@RequiredArgsConstructor
public class ClinicalInterpretationController {

    private final ClinicalInterpretationService clinicalInterpretationService;
    
    /**
     * Interpret a somatic variant for clinical significance
     */
    @PostMapping("/variants/{variantId}/interpret")
    public ResponseEntity<ClinicalInterpretation> interpretVariant(
            @PathVariable Long variantId,
            @RequestParam(required = false) Long diagnosisId,
            @RequestParam String pathogenicity,
            @RequestParam(required = false) String diagnosticSignificance,
            @RequestParam(required = false) String prognosticSignificance,
            @RequestParam(required = false) String therapeuticSignificance,
            @RequestParam(required = false) String evidenceLevel,
            @RequestParam(required = false) String evidenceSources,
            @RequestParam(required = false) String interpretationSummary,
            @RequestParam(required = false) String notes,
            @RequestParam String interpretedBy) {
        
        ClinicalInterpretation interpretation = clinicalInterpretationService.interpretVariant(
                variantId, diagnosisId, pathogenicity, diagnosticSignificance, prognosticSignificance,
                therapeuticSignificance, evidenceLevel, evidenceSources, interpretationSummary,
                notes, interpretedBy);
        
        return ResponseEntity.ok(interpretation);
    }
    
    /**
     * Add a therapy recommendation for a clinical interpretation
     */
    @PostMapping("/interpretations/{interpretationId}/therapy")
    public ResponseEntity<TherapyRecommendation> addTherapyRecommendation(
            @PathVariable Long interpretationId,
            @RequestParam String therapyName,
            @RequestParam(required = false) String therapyType,
            @RequestParam(required = false) String evidenceLevel,
            @RequestParam(required = false) String evidenceSource,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String recommendationSummary,
            @RequestParam(required = false) String contraindications,
            @RequestParam(required = false) String notes) {
        
        TherapyRecommendation recommendation = clinicalInterpretationService.addTherapyRecommendation(
                interpretationId, therapyName, therapyType, evidenceLevel, evidenceSource,
                referenceId, recommendationSummary, contraindications, notes);
        
        return ResponseEntity.ok(recommendation);
    }
    
    /**
     * Find clinically significant variants for a patient
     */
    @GetMapping("/patients/{patientId}/significant-variants")
    public ResponseEntity<List<SomaticVariant>> findClinicallySignificantVariants(
            @PathVariable Long patientId) {
        
        List<SomaticVariant> variants = clinicalInterpretationService.findClinicallySignificantVariants(patientId);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Find therapy recommendations for a patient
     */
    @GetMapping("/patients/{patientId}/therapy-recommendations")
    public ResponseEntity<List<TherapyRecommendation>> findTherapyRecommendations(
            @PathVariable Long patientId) {
        
        List<TherapyRecommendation> recommendations = clinicalInterpretationService.findTherapyRecommendations(patientId);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Find therapy recommendations for a specific cancer type
     */
    @GetMapping("/therapy-recommendations/by-cancer")
    public ResponseEntity<List<TherapyRecommendation>> findTherapyRecommendationsByCancerType(
            @RequestParam String cancerType) {
        
        List<TherapyRecommendation> recommendations = 
                clinicalInterpretationService.findTherapyRecommendationsByCancerType(cancerType);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Find therapy recommendations for a specific gene
     */
    @GetMapping("/therapy-recommendations/by-gene")
    public ResponseEntity<List<TherapyRecommendation>> findTherapyRecommendationsByGene(
            @RequestParam String geneSymbol) {
        
        List<TherapyRecommendation> recommendations = 
                clinicalInterpretationService.findTherapyRecommendationsByGene(geneSymbol);
        return ResponseEntity.ok(recommendations);
    }
}
