package com.example.oncology.controller;

import com.example.oncology.entity.Diagnosis;
import com.example.oncology.entity.Patient;
import com.example.oncology.entity.Sample;
import com.example.oncology.entity.SequencingData;
import com.example.oncology.repository.DiagnosisRepository;
import com.example.oncology.repository.PatientRepository;
import com.example.oncology.repository.SampleRepository;
import com.example.oncology.repository.SequencingDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for sample operations
 */
@RestController
@RequestMapping("/samples")
@RequiredArgsConstructor
public class SampleController {

    private final SampleRepository sampleRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final SequencingDataRepository sequencingDataRepository;
    
    /**
     * Get all samples
     */
    @GetMapping
    public ResponseEntity<List<Sample>> getAllSamples() {
        List<Sample> samples = sampleRepository.findAll();
        return ResponseEntity.ok(samples);
    }
    
    /**
     * Get sample by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sample> getSampleById(@PathVariable Long id) {
        return sampleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new sample
     */
    @PostMapping
    public ResponseEntity<Sample> createSample(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long diagnosisId,
            @RequestParam String sampleType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate collectionDate,
            @RequestParam(required = false) BigDecimal tumorPurity,
            @RequestParam(required = false) String tissueSite,
            @RequestParam(required = false) BigDecimal sampleQualityScore,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(required = false) String notes) {
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        
        Diagnosis diagnosis = null;
        if (diagnosisId != null) {
            diagnosis = diagnosisRepository.findById(diagnosisId)
                    .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));
        }
        
        Sample sample = new Sample();
        sample.setPatient(patient);
        sample.setDiagnosis(diagnosis);
        sample.setSampleType(sampleType);
        sample.setCollectionDate(collectionDate);
        sample.setTumorPurity(tumorPurity);
        sample.setTissueSite(tissueSite);
        sample.setSampleQualityScore(sampleQualityScore);
        sample.setStorageLocation(storageLocation);
        sample.setNotes(notes);
        
        Sample savedSample = sampleRepository.save(sample);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSample);
    }
    
    /**
     * Update an existing sample
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sample> updateSample(
            @PathVariable Long id,
            @RequestParam(required = false) String sampleType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate collectionDate,
            @RequestParam(required = false) BigDecimal tumorPurity,
            @RequestParam(required = false) String tissueSite,
            @RequestParam(required = false) BigDecimal sampleQualityScore,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(required = false) String notes) {
        
        return sampleRepository.findById(id)
                .map(sample -> {
                    if (sampleType != null) sample.setSampleType(sampleType);
                    if (collectionDate != null) sample.setCollectionDate(collectionDate);
                    if (tumorPurity != null) sample.setTumorPurity(tumorPurity);
                    if (tissueSite != null) sample.setTissueSite(tissueSite);
                    if (sampleQualityScore != null) sample.setSampleQualityScore(sampleQualityScore);
                    if (storageLocation != null) sample.setStorageLocation(storageLocation);
                    if (notes != null) sample.setNotes(notes);
                    
                    Sample updatedSample = sampleRepository.save(sample);
                    return ResponseEntity.ok(updatedSample);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete a sample
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSample(@PathVariable Long id) {
        return sampleRepository.findById(id)
                .map(sample -> {
                    sampleRepository.delete(sample);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get samples by patient ID
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Sample>> getSamplesByPatient(@PathVariable Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        
        List<Sample> samples = sampleRepository.findByPatient(patient);
        return ResponseEntity.ok(samples);
    }
    
    /**
     * Get samples by diagnosis ID
     */
    @GetMapping("/diagnosis/{diagnosisId}")
    public ResponseEntity<List<Sample>> getSamplesByDiagnosis(@PathVariable Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));
        
        List<Sample> samples = sampleRepository.findByDiagnosis(diagnosis);
        return ResponseEntity.ok(samples);
    }
    
    /**
     * Add sequencing data to a sample
     */
    @PostMapping("/{sampleId}/sequencing")
    public ResponseEntity<SequencingData> addSequencingData(
            @PathVariable Long sampleId,
            @RequestParam String platform,
            @RequestParam String libraryPrepKit,
            @RequestParam(required = false) String sequencingType,
            @RequestParam(required = false) Integer targetCoverage,
            @RequestParam(required = false) Double meanCoverageValue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sequencingDate) {
        
        return sampleRepository.findById(sampleId)
                .map(sample -> {
                    SequencingData sequencingData = new SequencingData();
                    sequencingData.setSample(sample);
                    sequencingData.setPlatform(platform);
                    sequencingData.setLibraryPrepKit(libraryPrepKit);
                    sequencingData.setSequencingType(sequencingType);
                    sequencingData.setTargetCoverage(targetCoverage);
                    sequencingData.setMeanCoverage(meanCoverageValue != null ? new BigDecimal(meanCoverageValue.toString()) : null);
                    sequencingData.setSequencingDate(sequencingDate);
                    
                    // Set a simple JSON string for quality metrics
                    String jsonQualityMetrics = String.format("{\"q30\":%.1f,\"totalBases\":%d,\"qualityScore\":35}", 
                            meanCoverageValue != null ? meanCoverageValue * 0.95 : 90.0,
                            targetCoverage != null ? targetCoverage * 1000000 : 100000000);
                    sequencingData.setQualityMetrics(jsonQualityMetrics);
                    
                    sequencingData.setStatus("PENDING");
                    
                    SequencingData savedData = sequencingDataRepository.save(sequencingData);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get sequencing data for a sample
     */
    @GetMapping("/{sampleId}/sequencing")
    public ResponseEntity<List<SequencingData>> getSequencingData(@PathVariable Long sampleId) {
        return sampleRepository.findById(sampleId)
                .map(sample -> {
                    List<SequencingData> sequencingData = sequencingDataRepository.findBySample(sample);
                    return ResponseEntity.ok(sequencingData);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
