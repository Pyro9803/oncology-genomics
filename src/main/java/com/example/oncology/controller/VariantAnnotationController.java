package com.example.oncology.controller;

import com.example.oncology.entity.VariantAnnotation;
import com.example.oncology.service.VariantAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for variant annotation operations
 */
@RestController
@RequestMapping("/variant-annotations")
public class VariantAnnotationController {
    
    @Autowired
    private VariantAnnotationService annotationService;
    
    /**
     * Simple test endpoint to check if controller is registered
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "VariantAnnotationController is working");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Start annotation process for a variant calling
     */
    @PostMapping("/annotate/{variantCallingId}")
    public ResponseEntity<Map<String, String>> startAnnotation(@PathVariable Long variantCallingId) {
        annotationService.annotateVariants(variantCallingId);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "started");
        response.put("message", "Variant annotation process started for variant calling ID: " + variantCallingId);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    /**
     * Get annotations for a variant
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<VariantAnnotation>> getAnnotationsForVariant(@PathVariable Long variantId) {
        List<VariantAnnotation> annotations = annotationService.getAnnotationsForVariant(variantId);
        return ResponseEntity.ok(annotations);
    }
    
    /**
     * Get annotations for a variant calling
     */
    @GetMapping("/variant-calling/{variantCallingId}")
    public ResponseEntity<List<VariantAnnotation>> getAnnotationsForVariantCalling(@PathVariable Long variantCallingId) {
        List<VariantAnnotation> annotations = annotationService.getAnnotationsForVariantCalling(variantCallingId);
        return ResponseEntity.ok(annotations);
    }
    
    /**
     * Filter variants based on criteria
     */
    @GetMapping("/filter/{variantCallingId}")
    public ResponseEntity<List<VariantAnnotation>> filterVariants(
            @PathVariable Long variantCallingId,
            @RequestParam(required = false) String variantEffect,
            @RequestParam(required = false) String clinicalSignificance,
            @RequestParam(required = false) String maxPopulationFrequency,
            @RequestParam(required = false) String actionability) {
        
        Map<String, String> filterCriteria = new HashMap<>();
        
        if (variantEffect != null) {
            filterCriteria.put("variantEffect", variantEffect);
        }
        
        if (clinicalSignificance != null) {
            filterCriteria.put("clinicalSignificance", clinicalSignificance);
        }
        
        if (maxPopulationFrequency != null) {
            filterCriteria.put("maxPopulationFrequency", maxPopulationFrequency);
        }
        
        if (actionability != null) {
            filterCriteria.put("actionability", actionability);
        }
        
        List<VariantAnnotation> filteredAnnotations = annotationService.filterVariants(variantCallingId, filterCriteria);
        return ResponseEntity.ok(filteredAnnotations);
    }
    
    /**
     * Get pathogenic variants
     */
    @GetMapping("/pathogenic")
    public ResponseEntity<List<VariantAnnotation>> getPathogenicVariants() {
        List<VariantAnnotation> pathogenicVariants = annotationService.getPathogenicVariants();
        return ResponseEntity.ok(pathogenicVariants);
    }
    
    /**
     * Get clinically relevant variants
     */
    @GetMapping("/clinically-relevant")
    public ResponseEntity<List<VariantAnnotation>> getClinicallyRelevantVariants() {
        List<VariantAnnotation> clinicallyRelevantVariants = annotationService.getClinicallyRelevantVariants();
        return ResponseEntity.ok(clinicallyRelevantVariants);
    }
    
    /**
     * Get actionable variants
     */
    @GetMapping("/actionable")
    public ResponseEntity<List<VariantAnnotation>> getActionableVariants() {
        List<VariantAnnotation> actionableVariants = annotationService.getActionableVariants();
        return ResponseEntity.ok(actionableVariants);
    }
    
    /**
     * Request verification for a variant
     */
    @PostMapping("/{annotationId}/request-verification")
    public ResponseEntity<Map<String, String>> requestVerification(
            @PathVariable Long annotationId,
            @RequestParam String verificationMethod,
            @RequestParam(required = false) String notes) {
        
        annotationService.requestVerification(annotationId, verificationMethod, notes);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "requested");
        response.put("message", "Verification requested for annotation ID: " + annotationId);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    /**
     * Update verification status for a variant
     */
    @PostMapping("/{annotationId}/update-verification")
    public ResponseEntity<Map<String, String>> updateVerificationStatus(
            @PathVariable Long annotationId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        
        annotationService.updateVerificationStatus(annotationId, status, notes);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "updated");
        response.put("message", "Verification status updated for annotation ID: " + annotationId);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
