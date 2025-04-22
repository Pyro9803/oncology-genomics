package com.example.oncology.controller;

import com.example.oncology.entity.SomaticVariant;
import com.example.oncology.repository.SomaticVariantRepository;
import com.example.oncology.repository.VariantCallingRepository;
import com.example.oncology.service.VariantProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for variant operations
 */
@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
public class VariantController {

    private final SomaticVariantRepository somaticVariantRepository;
    private final VariantCallingRepository variantCallingRepository;
    private final VariantProcessingService variantProcessingService;
    
    /**
     * Get all variants
     */
    @GetMapping
    public ResponseEntity<List<SomaticVariant>> getAllVariants() {
        List<SomaticVariant> variants = somaticVariantRepository.findAll();
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Get variant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SomaticVariant> getVariantById(@PathVariable Long id) {
        return somaticVariantRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get variants by variant calling ID
     */
    @GetMapping("/calling/{variantCallingId}")
    public ResponseEntity<List<SomaticVariant>> getVariantsByCallingId(@PathVariable Long variantCallingId) {
        return variantCallingRepository.findById(variantCallingId)
                .map(variantCalling -> {
                    List<SomaticVariant> variants = somaticVariantRepository.findByVariantCalling(variantCalling);
                    return ResponseEntity.ok(variants);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get variants by gene
     */
    @GetMapping("/gene/{gene}")
    public ResponseEntity<List<SomaticVariant>> getVariantsByGene(@PathVariable String gene) {
        List<SomaticVariant> variants = somaticVariantRepository.findByGeneSymbol(gene);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Get variants by chromosome and position range
     */
    @GetMapping("/location")
    public ResponseEntity<List<SomaticVariant>> getVariantsByLocation(
            @RequestParam String chromosome,
            @RequestParam Integer startPosition,
            @RequestParam Integer endPosition) {
        
        List<SomaticVariant> variants = somaticVariantRepository
                .findByChromosomeAndPositionBetween(chromosome, startPosition, endPosition);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Get variants by variant type
     */
    @GetMapping("/type/{variantType}")
    public ResponseEntity<List<SomaticVariant>> getVariantsByType(@PathVariable String variantType) {
        List<SomaticVariant> variants = somaticVariantRepository.findByVariantType(variantType);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Get variants by allele frequency range
     */
    @GetMapping("/allele-frequency")
    public ResponseEntity<List<SomaticVariant>> getVariantsByAlleleFrequency(
            @RequestParam BigDecimal minFrequency,
            @RequestParam BigDecimal maxFrequency) {
        
        List<SomaticVariant> variants = somaticVariantRepository
                .findByAlleleFrequencyBetween(minFrequency, maxFrequency);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Get variants by filter status
     */
    @GetMapping("/filter/{filterStatus}")
    public ResponseEntity<List<SomaticVariant>> getVariantsByFilterStatus(@PathVariable String filterStatus) {
        List<SomaticVariant> variants = somaticVariantRepository.findByFilterStatus(filterStatus);
        return ResponseEntity.ok(variants);
    }
    
    /**
     * Process a completed variant calling job
     */
    @PostMapping("/process/{jobId}")
    public ResponseEntity<String> processVariantCallingJob(@PathVariable Long jobId) {
        try {
            variantProcessingService.processCompletedJob(jobId);
            return ResponseEntity.ok("Variant processing started for job: " + jobId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Annotate variants for a job
     */
    @PostMapping("/annotate/{jobId}")
    public ResponseEntity<String> annotateVariants(@PathVariable Long jobId) {
        try {
            variantProcessingService.annotateVariants(jobId);
            return ResponseEntity.ok("Variant annotation started for job: " + jobId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Create a new somatic variant for a variant calling
     */
    @PostMapping("/variant-calling/{variantCallingId}")
    public ResponseEntity<SomaticVariant> createVariant(
            @PathVariable Long variantCallingId,
            @RequestBody SomaticVariant variant) {
        
        return variantCallingRepository.findById(variantCallingId)
                .map(variantCalling -> {
                    variant.setVariantCalling(variantCalling);
                    SomaticVariant savedVariant = somaticVariantRepository.save(variant);
                    return ResponseEntity.ok(savedVariant);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
