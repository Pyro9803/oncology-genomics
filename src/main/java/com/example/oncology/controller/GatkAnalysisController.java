package com.example.oncology.controller;

import com.example.oncology.entity.VariantCalling;
import com.example.oncology.service.GatkAnalysisService;
import com.example.oncology.service.VariantProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for GATK analysis operations
 */
@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class GatkAnalysisController {

    private final GatkAnalysisService gatkAnalysisService;
    private final VariantProcessingService variantProcessingService;
    
    /**
     * Submit a new GATK analysis job
     */
    @PostMapping("/submit")
    public ResponseEntity<VariantCalling> submitAnalysisJob(
            @RequestParam Long tumorSampleId,
            @RequestParam(required = false) Long normalSampleId,
            @RequestParam(defaultValue = "4.2.0.0") String pipelineVersion,
            @RequestParam(defaultValue = "Mutect2") String callingMethod) {
        
        VariantCalling job = gatkAnalysisService.submitAnalysisJob(
                tumorSampleId, normalSampleId, pipelineVersion, callingMethod);
        
        return ResponseEntity.ok(job);
    }
    
    /**
     * Check the status of a GATK analysis job
     */
    @GetMapping("/{jobId}/status")
    public ResponseEntity<VariantCalling> checkJobStatus(@PathVariable Long jobId) {
        VariantCalling job = gatkAnalysisService.checkJobStatus(jobId);
        return ResponseEntity.ok(job);
    }
    
    /**
     * Cancel a running GATK analysis job
     */
    @PostMapping("/{jobId}/cancel")
    public ResponseEntity<String> cancelJob(@PathVariable Long jobId) {
        boolean cancelled = gatkAnalysisService.cancelJob(jobId);
        if (cancelled) {
            return ResponseEntity.ok("Job cancellation requested");
        } else {
            return ResponseEntity.badRequest().body("Failed to cancel job or job already completed/failed");
        }
    }
    
    /**
     * Process a completed GATK analysis job
     */
    @PostMapping("/{jobId}/process")
    public ResponseEntity<String> processJob(@PathVariable Long jobId) {
        variantProcessingService.processCompletedJob(jobId);
        return ResponseEntity.ok("Job processing started");
    }
    
    /**
     * Annotate variants from a completed GATK analysis job
     */
    @PostMapping("/{jobId}/annotate")
    public ResponseEntity<String> annotateVariants(@PathVariable Long jobId) {
        variantProcessingService.annotateVariants(jobId);
        return ResponseEntity.ok("Variant annotation started");
    }
}
