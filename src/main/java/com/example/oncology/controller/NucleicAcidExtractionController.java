package com.example.oncology.controller;

import com.example.oncology.entity.NucleicAcidExtraction;
import com.example.oncology.entity.Sample;
import com.example.oncology.repository.NucleicAcidExtractionRepository;
import com.example.oncology.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for nucleic acid extraction operations
 */
@RestController
@RequestMapping("/extractions")
@RequiredArgsConstructor
public class NucleicAcidExtractionController {

    private final NucleicAcidExtractionRepository extractionRepository;
    private final SampleRepository sampleRepository;
    
    // Base directory for storing quality check images
    private final String UPLOAD_DIR = "/data/quality_images/";
    
    /**
     * Get all extractions
     */
    @GetMapping
    public ResponseEntity<List<NucleicAcidExtraction>> getAllExtractions() {
        List<NucleicAcidExtraction> extractions = extractionRepository.findAll();
        return ResponseEntity.ok(extractions);
    }
    
    /**
     * Get extraction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NucleicAcidExtraction> getExtractionById(@PathVariable Long id) {
        return extractionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new extraction
     */
    @PostMapping
    public ResponseEntity<NucleicAcidExtraction> createExtraction(
            @RequestParam Long sampleId,
            @RequestParam String extractionType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate extractionDate,
            @RequestParam String extractionMethod,
            @RequestParam(required = false) BigDecimal concentration,
            @RequestParam(required = false) BigDecimal purity260280,
            @RequestParam(required = false) BigDecimal purity260230,
            @RequestParam(required = false) String qualityCheckMethod,
            @RequestParam(required = false) String qualityCheckResult,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(required = false) String notes) {
        
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + sampleId));
        
        NucleicAcidExtraction extraction = new NucleicAcidExtraction();
        extraction.setSample(sample);
        extraction.setExtractionType(extractionType);
        extraction.setExtractionDate(extractionDate);
        extraction.setExtractionMethod(extractionMethod);
        extraction.setConcentration(concentration);
        extraction.setPurity260280(purity260280);
        extraction.setPurity260230(purity260230);
        extraction.setQualityCheckMethod(qualityCheckMethod);
        extraction.setQualityCheckResult(qualityCheckResult);
        extraction.setStorageLocation(storageLocation);
        extraction.setNotes(notes);
        
        NucleicAcidExtraction savedExtraction = extractionRepository.save(extraction);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExtraction);
    }
    
    /**
     * Upload quality check image for an extraction
     */
    @PostMapping("/{id}/quality-image")
    public ResponseEntity<NucleicAcidExtraction> uploadQualityImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        return extractionRepository.findById(id)
                .<ResponseEntity<NucleicAcidExtraction>>map(extraction -> {
                    try {
                        // Create directory if it doesn't exist
                        Path uploadPath = Paths.get(UPLOAD_DIR);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        
                        // Generate unique filename
                        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                        Path filePath = uploadPath.resolve(filename);
                        
                        // Save the file
                        Files.copy(file.getInputStream(), filePath);
                        
                        // Update extraction with image path
                        extraction.setQualityImagePath(UPLOAD_DIR + filename);
                        NucleicAcidExtraction updatedExtraction = extractionRepository.save(extraction);
                        
                        return ResponseEntity.<NucleicAcidExtraction>ok(updatedExtraction);
                    } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update an existing extraction
     */
    @PutMapping("/{id}")
    public ResponseEntity<NucleicAcidExtraction> updateExtraction(
            @PathVariable Long id,
            @RequestParam(required = false) String extractionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate extractionDate,
            @RequestParam(required = false) String extractionMethod,
            @RequestParam(required = false) BigDecimal concentration,
            @RequestParam(required = false) BigDecimal purity260280,
            @RequestParam(required = false) BigDecimal purity260230,
            @RequestParam(required = false) String qualityCheckMethod,
            @RequestParam(required = false) String qualityCheckResult,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(required = false) String notes) {
        
        return extractionRepository.findById(id)
                .map(extraction -> {
                    if (extractionType != null) extraction.setExtractionType(extractionType);
                    if (extractionDate != null) extraction.setExtractionDate(extractionDate);
                    if (extractionMethod != null) extraction.setExtractionMethod(extractionMethod);
                    if (concentration != null) extraction.setConcentration(concentration);
                    if (purity260280 != null) extraction.setPurity260280(purity260280);
                    if (purity260230 != null) extraction.setPurity260230(purity260230);
                    if (qualityCheckMethod != null) extraction.setQualityCheckMethod(qualityCheckMethod);
                    if (qualityCheckResult != null) extraction.setQualityCheckResult(qualityCheckResult);
                    if (storageLocation != null) extraction.setStorageLocation(storageLocation);
                    if (notes != null) extraction.setNotes(notes);
                    
                    NucleicAcidExtraction updatedExtraction = extractionRepository.save(extraction);
                    return ResponseEntity.ok(updatedExtraction);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete an extraction
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtraction(@PathVariable Long id) {
        return extractionRepository.findById(id)
                .map(extraction -> {
                    extractionRepository.delete(extraction);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get extractions by sample ID
     */
    @GetMapping("/sample/{sampleId}")
    public ResponseEntity<List<NucleicAcidExtraction>> getExtractionsBySample(@PathVariable Long sampleId) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + sampleId));
        
        List<NucleicAcidExtraction> extractions = extractionRepository.findBySample(sample);
        return ResponseEntity.ok(extractions);
    }
}
