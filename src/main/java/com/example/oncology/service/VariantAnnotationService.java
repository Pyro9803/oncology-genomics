package com.example.oncology.service;

import com.example.oncology.entity.SomaticVariant;
import com.example.oncology.entity.VariantAnnotation;
import com.example.oncology.entity.VariantCalling;
import com.example.oncology.repository.SomaticVariantRepository;
import com.example.oncology.repository.VariantAnnotationRepository;
import com.example.oncology.repository.VariantCallingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for annotating somatic variants
 */
@Service
@Slf4j
public class VariantAnnotationService {
    
    @Autowired
    private VariantCallingRepository variantCallingRepository;
    
    @Autowired
    private SomaticVariantRepository somaticVariantRepository;
    
    @Autowired
    private VariantAnnotationRepository variantAnnotationRepository;
    
    /**
     * Get annotations for a variant
     */
    public List<VariantAnnotation> getAnnotationsForVariant(Long variantId) {
        return variantAnnotationRepository.findBySomaticVariantVariantId(variantId);
    }
    
    /**
     * Get annotations for a variant calling
     */
    public List<VariantAnnotation> getAnnotationsForVariantCalling(Long variantCallingId) {
        return variantAnnotationRepository.findByVariantCallingId(variantCallingId);
    }
    
    /**
     * Get pathogenic variants
     */
    public List<VariantAnnotation> getPathogenicVariants() {
        return variantAnnotationRepository.findPathogenicVariants();
    }
    
    /**
     * Get clinically relevant variants
     */
    public List<VariantAnnotation> getClinicallyRelevantVariants() {
        return variantAnnotationRepository.findClinicallyRelevantVariants();
    }
    
    /**
     * Get actionable variants
     */
    public List<VariantAnnotation> getActionableVariants() {
        return variantAnnotationRepository.findActionableVariants();
    }
    
    /**
     * Annotate variants for a variant calling
     */
    @Async
    @Transactional
    public CompletableFuture<Void> annotateVariants(Long variantCallingId) {
        try {
            log.info("Starting variant annotation for variant calling ID: {}", variantCallingId);
            
            VariantCalling variantCalling = variantCallingRepository.findById(variantCallingId)
                    .orElseThrow(() -> new RuntimeException("Variant calling not found with ID: " + variantCallingId));
            
            List<SomaticVariant> variants = variantCalling.getSomaticVariants();
            if (variants.isEmpty()) {
                log.warn("No variants found for variant calling ID: {}", variantCallingId);
                return CompletableFuture.completedFuture(null);
            }
            
            log.info("Found {} variants to annotate", variants.size());
            
            // Annotate each variant
            for (SomaticVariant variant : variants) {
                annotateVariant(variant);
            }
            
            log.info("Completed variant annotation for variant calling ID: {}", variantCallingId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error annotating variants for variant calling ID: {}", variantCallingId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Annotate a single variant
     */
    @Transactional
    public void annotateVariant(SomaticVariant variant) {
        try {
            log.info("Annotating variant: {}:{} {}>{}",
                    variant.getChromosome(), variant.getPosition(),
                    variant.getReferenceAllele(), variant.getAlternateAllele());
            
            // Create a new annotation
            VariantAnnotation annotation = new VariantAnnotation();
            annotation.setSomaticVariant(variant);
            
            // Set basic annotation fields
            annotation.setGeneSymbol(variant.getGeneSymbol());
            annotation.setVariantType(variant.getVariantType());
            
            // Determine variant effect based on variant type
            String variantEffect = determineVariantEffect(variant);
            annotation.setVariantEffect(variantEffect);
            
            // Query external databases for annotation
            Map<String, String> externalAnnotations = queryExternalDatabases(variant);
            
            // Set annotation fields from external databases
            annotation.setDbsnpId(externalAnnotations.get("dbsnpId"));
            annotation.setCosmicId(externalAnnotations.get("cosmicId"));
            annotation.setClinvarId(externalAnnotations.get("clinvarId"));
            annotation.setClinvarSignificance(externalAnnotations.get("clinvarSignificance"));
            annotation.setHgmdId(externalAnnotations.get("hgmdId"));
            annotation.setCivicId(externalAnnotations.get("civicId"));
            annotation.setOncokbId(externalAnnotations.get("oncokbId"));
            
            // Set population frequency
            String popFreqStr = externalAnnotations.get("populationFrequency");
            if (popFreqStr != null && !popFreqStr.isEmpty()) {
                try {
                    annotation.setPopulationFrequency(Double.parseDouble(popFreqStr));
                } catch (NumberFormatException e) {
                    log.warn("Invalid population frequency: {}", popFreqStr);
                }
            }
            
            // Determine clinical significance
            annotation.setClinicalSignificance(determineClinicalSignificance(externalAnnotations));
            
            // Determine oncogenic effect
            annotation.setOncogenicEffect(determineOncogenicEffect(externalAnnotations));
            
            // Determine actionability
            annotation.setActionability(determineActionability(externalAnnotations));
            
            // Set drug associations
            annotation.setDrugAssociations(externalAnnotations.get("drugAssociations"));
            
            // Set literature references
            annotation.setLiteratureReferences(externalAnnotations.get("literatureReferences"));
            
            // Set verification status
            annotation.setVerificationStatus("PENDING");
            
            // Set annotation source and version
            annotation.setAnnotationSource("Oncology Genomics Annotator");
            annotation.setAnnotationVersion("1.0");
            
            // Save the annotation
            variantAnnotationRepository.save(annotation);
            
            log.info("Variant annotation completed for variant ID: {}", variant.getVariantId());
        } catch (Exception e) {
            log.error("Error annotating variant ID: {}", variant.getVariantId(), e);
            throw e;
        }
    }
    
    /**
     * Determine variant effect based on variant type
     */
    private String determineVariantEffect(SomaticVariant variant) {
        String variantType = variant.getVariantType();
        
        if ("SNV".equals(variantType)) {
            // For SNVs, determine if it's missense, nonsense, etc.
            // This would require knowledge of the codon change
            // For simplicity, we'll return "missense_variant" for now
            return "missense_variant";
        } else if ("INSERTION".equals(variantType)) {
            // For insertions, determine if it's frameshift or in-frame
            // For simplicity, we'll return "frameshift_variant" for now
            return "frameshift_variant";
        } else if ("DELETION".equals(variantType)) {
            // For deletions, determine if it's frameshift or in-frame
            // For simplicity, we'll return "frameshift_variant" for now
            return "frameshift_variant";
        } else {
            return "unknown";
        }
    }
    
    /**
     * Query external databases for variant annotation
     * In a real implementation, this would make API calls to external databases
     * For this example, we'll simulate the responses
     */
    private Map<String, String> queryExternalDatabases(SomaticVariant variant) {
        Map<String, String> annotations = new HashMap<>();
        
        // Simulate dbSNP lookup
        if (Math.random() > 0.3) {
            annotations.put("dbsnpId", "rs" + (int)(Math.random() * 100000000));
        }
        
        // Simulate COSMIC lookup
        if (Math.random() > 0.5) {
            annotations.put("cosmicId", "COSM" + (int)(Math.random() * 1000000));
        }
        
        // Simulate ClinVar lookup
        if (Math.random() > 0.7) {
            annotations.put("clinvarId", "VCV" + (int)(Math.random() * 1000000));
            
            // Simulate ClinVar significance
            double rand = Math.random();
            if (rand < 0.1) {
                annotations.put("clinvarSignificance", "pathogenic");
            } else if (rand < 0.2) {
                annotations.put("clinvarSignificance", "likely_pathogenic");
            } else if (rand < 0.3) {
                annotations.put("clinvarSignificance", "uncertain_significance");
            } else if (rand < 0.4) {
                annotations.put("clinvarSignificance", "likely_benign");
            } else {
                annotations.put("clinvarSignificance", "benign");
            }
        }
        
        // Simulate HGMD lookup
        if (Math.random() > 0.8) {
            annotations.put("hgmdId", "CM" + (int)(Math.random() * 1000000));
        }
        
        // Simulate CIViC lookup
        if (Math.random() > 0.7) {
            annotations.put("civicId", (int)(Math.random() * 10000) + "");
            
            // Simulate drug associations
            if (Math.random() > 0.5) {
                annotations.put("drugAssociations", "Pembrolizumab, Nivolumab");
            }
        }
        
        // Simulate OncoKB lookup
        if (Math.random() > 0.7) {
            annotations.put("oncokbId", (int)(Math.random() * 10000) + "");
        }
        
        // Simulate population frequency
        annotations.put("populationFrequency", String.format("%.6f", Math.random() * 0.1));
        
        // Simulate literature references
        if (Math.random() > 0.6) {
            annotations.put("literatureReferences", "PMID:12345678, PMID:23456789");
        }
        
        return annotations;
    }
    
    /**
     * Determine clinical significance based on annotations
     */
    private String determineClinicalSignificance(Map<String, String> annotations) {
        String clinvarSignificance = annotations.get("clinvarSignificance");
        
        if (clinvarSignificance != null) {
            if ("pathogenic".equals(clinvarSignificance) || "likely_pathogenic".equals(clinvarSignificance)) {
                return clinvarSignificance;
            }
        }
        
        // If no ClinVar significance, determine based on other factors
        if (annotations.containsKey("cosmicId") && annotations.containsKey("oncokbId")) {
            return "likely_pathogenic";
        } else if (annotations.containsKey("cosmicId")) {
            return "uncertain_significance";
        } else {
            return "unknown";
        }
    }
    
    /**
     * Determine oncogenic effect based on annotations
     */
    private String determineOncogenicEffect(Map<String, String> annotations) {
        if (annotations.containsKey("oncokbId") && annotations.containsKey("cosmicId")) {
            return "oncogenic";
        } else if (annotations.containsKey("oncokbId") || annotations.containsKey("cosmicId")) {
            return "likely_oncogenic";
        } else {
            return "unknown";
        }
    }
    
    /**
     * Determine actionability based on annotations
     */
    private String determineActionability(Map<String, String> annotations) {
        if (annotations.containsKey("drugAssociations")) {
            return "therapeutic";
        } else if (annotations.containsKey("oncokbId") || annotations.containsKey("civicId")) {
            return "prognostic";
        } else if (annotations.containsKey("clinvarId") && "pathogenic".equals(annotations.get("clinvarSignificance"))) {
            return "diagnostic";
        } else {
            return "unknown";
        }
    }
    
    /**
     * Filter variants based on criteria
     */
    public List<VariantAnnotation> filterVariants(Long variantCallingId, Map<String, String> filterCriteria) {
        List<VariantAnnotation> annotations = variantAnnotationRepository.findByVariantCallingId(variantCallingId);
        List<VariantAnnotation> filteredAnnotations = new ArrayList<>();
        
        for (VariantAnnotation annotation : annotations) {
            boolean passesFilter = true;
            
            // Filter by variant effect
            if (filterCriteria.containsKey("variantEffect")) {
                String[] allowedEffects = filterCriteria.get("variantEffect").split(",");
                boolean effectMatches = false;
                for (String effect : allowedEffects) {
                    if (effect.trim().equals(annotation.getVariantEffect())) {
                        effectMatches = true;
                        break;
                    }
                }
                if (!effectMatches) {
                    passesFilter = false;
                }
            }
            
            // Filter by clinical significance
            if (filterCriteria.containsKey("clinicalSignificance")) {
                String[] allowedSignificances = filterCriteria.get("clinicalSignificance").split(",");
                boolean significanceMatches = false;
                for (String significance : allowedSignificances) {
                    if (significance.trim().equals(annotation.getClinicalSignificance())) {
                        significanceMatches = true;
                        break;
                    }
                }
                if (!significanceMatches) {
                    passesFilter = false;
                }
            }
            
            // Filter by population frequency
            if (filterCriteria.containsKey("maxPopulationFrequency") && annotation.getPopulationFrequency() != null) {
                double maxFreq = Double.parseDouble(filterCriteria.get("maxPopulationFrequency"));
                if (annotation.getPopulationFrequency() > maxFreq) {
                    passesFilter = false;
                }
            }
            
            // Filter by actionability
            if (filterCriteria.containsKey("actionability")) {
                String[] allowedActionabilities = filterCriteria.get("actionability").split(",");
                boolean actionabilityMatches = false;
                for (String actionability : allowedActionabilities) {
                    if (actionability.trim().equals(annotation.getActionability())) {
                        actionabilityMatches = true;
                        break;
                    }
                }
                if (!actionabilityMatches) {
                    passesFilter = false;
                }
            }
            
            // Add more filters as needed
            
            if (passesFilter) {
                filteredAnnotations.add(annotation);
            }
        }
        
        return filteredAnnotations;
    }
    
    /**
     * Request verification for a variant
     */
    @Transactional
    public void requestVerification(Long annotationId, String verificationMethod, String notes) {
        VariantAnnotation annotation = variantAnnotationRepository.findById(annotationId)
                .orElseThrow(() -> new RuntimeException("Annotation not found with ID: " + annotationId));
        
        annotation.setVerificationStatus("PENDING");
        annotation.setVerificationMethod(verificationMethod);
        annotation.setVerificationNotes(notes);
        
        variantAnnotationRepository.save(annotation);
        
        log.info("Verification requested for annotation ID: {}", annotationId);
    }
    
    /**
     * Update verification status for a variant
     */
    @Transactional
    public void updateVerificationStatus(Long annotationId, String status, String notes) {
        VariantAnnotation annotation = variantAnnotationRepository.findById(annotationId)
                .orElseThrow(() -> new RuntimeException("Annotation not found with ID: " + annotationId));
        
        annotation.setVerificationStatus(status);
        annotation.setVerificationNotes(notes);
        
        variantAnnotationRepository.save(annotation);
        
        log.info("Verification status updated for annotation ID: {}", annotationId);
    }
}
