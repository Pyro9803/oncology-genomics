package com.example.oncology.controller;

import com.example.oncology.entity.Sample;
import com.example.oncology.entity.VariantCalling;
import com.example.oncology.repository.SampleRepository;
import com.example.oncology.repository.VariantCallingRepository;
import com.example.oncology.service.GatkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for variant calling operations using GATK
 */
@RestController
@RequestMapping("/variant-callings")
@RequiredArgsConstructor
public class VariantCallingController {

    private final VariantCallingRepository variantCallingRepository;
    private final SampleRepository sampleRepository;
    private final GatkService gatkService;
    
    /**
     * Get all variant callings
     */
    @GetMapping
    public ResponseEntity<List<VariantCalling>> getAllVariantCallings() {
        List<VariantCalling> variantCallings = variantCallingRepository.findAll();
        return ResponseEntity.ok(variantCallings);
    }
    
    /**
     * Get variant calling by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VariantCalling> getVariantCallingById(@PathVariable Long id) {
        return variantCallingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Start a new variant calling job
     */
    @PostMapping
    public ResponseEntity<VariantCalling> startVariantCalling(
            @RequestParam Long tumorSampleId,
            @RequestParam(required = false) Long normalSampleId,
            @RequestParam String pipelineVersion,
            @RequestParam String referenceGenome,
            @RequestParam(defaultValue = "Mutect2") String callingMethod,
            @RequestParam(defaultValue = "false") Boolean panelOfNormalsUsed,
            @RequestParam(required = false) String panelOfNormalsPath,
            @RequestParam(required = false) String dbsnpVersion,
            @RequestParam(required = false) String gnomadVersion,
            @RequestParam(required = false) String filtersApplied) {
        
        // Validate tumor sample
        Sample tumorSample = sampleRepository.findById(tumorSampleId)
                .orElseThrow(() -> new IllegalArgumentException("Tumor sample not found: " + tumorSampleId));
        
        // Validate normal sample if provided
        Sample normalSample = null;
        if (normalSampleId != null) {
            normalSample = sampleRepository.findById(normalSampleId)
                    .orElseThrow(() -> new IllegalArgumentException("Normal sample not found: " + normalSampleId));
        }
        
        // Create variant calling record
        VariantCalling variantCalling = new VariantCalling();
        variantCalling.setTumorSample(tumorSample);
        variantCalling.setNormalSample(normalSample);
        variantCalling.setPipelineVersion(pipelineVersion);
        variantCalling.setReferenceGenome(referenceGenome);
        variantCalling.setCallingMethod(callingMethod);
        variantCalling.setPanelOfNormalsUsed(panelOfNormalsUsed);
        variantCalling.setPanelOfNormalsPath(panelOfNormalsPath);
        variantCalling.setDbsnpVersion(dbsnpVersion);
        variantCalling.setGnomadVersion(gnomadVersion);
        variantCalling.setFiltersApplied(filtersApplied);
        variantCalling.setStatus("PENDING");
        variantCalling.setJobId(UUID.randomUUID().toString());
        
        // Save the variant calling record
        VariantCalling savedVariantCalling = variantCallingRepository.save(variantCalling);
        
        // Start the GATK pipeline asynchronously
        gatkService.startVariantCallingPipeline(savedVariantCalling.getVariantCallingId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVariantCalling);
    }
    
    /**
     * Get the status of a variant calling job
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getVariantCallingStatus(@PathVariable Long id) {
        return variantCallingRepository.findById(id)
                .map(variantCalling -> ResponseEntity.ok(variantCalling.getStatus()))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Cancel a variant calling job
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<VariantCalling> cancelVariantCalling(@PathVariable Long id) {
        return variantCallingRepository.findById(id)
                .map(variantCalling -> {
                    if ("PENDING".equals(variantCalling.getStatus()) || "IN_PROGRESS".equals(variantCalling.getStatus())) {
                        gatkService.cancelVariantCallingPipeline(variantCalling.getJobId());
                        variantCalling.setStatus("CANCELLED");
                        VariantCalling updatedVariantCalling = variantCallingRepository.save(variantCalling);
                        return ResponseEntity.ok(updatedVariantCalling);
                    } else {
                        return ResponseEntity.badRequest().body(variantCalling);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get variant callings by tumor sample ID
     */
    @GetMapping("/tumor-sample/{sampleId}")
    public ResponseEntity<List<VariantCalling>> getVariantCallingsByTumorSample(@PathVariable Long sampleId) {
        Sample tumorSample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + sampleId));
        
        List<VariantCalling> variantCallings = variantCallingRepository.findByTumorSample(tumorSample);
        return ResponseEntity.ok(variantCallings);
    }
}
