package com.example.oncology.controller;

import com.example.oncology.entity.FollowUp;
import com.example.oncology.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for patient follow-up operations
 */
@RestController
@RequestMapping("/follow-up")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;
    
    /**
     * Record a new follow-up for a patient
     */
    @PostMapping("/record")
    public ResponseEntity<FollowUp> recordFollowUp(
            @RequestParam Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate followupDate,
            @RequestParam(required = false) Long therapyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate therapyStartedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate therapyEndedDate,
            @RequestParam(required = false) String responseStatus,
            @RequestParam(required = false) String toxicityGrade,
            @RequestParam(required = false) String toxicityDescription,
            @RequestParam(required = false) String diseaseStatus,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextFollowupDate) {
        
        FollowUp followUp = followUpService.recordFollowUp(
                patientId, followupDate, therapyId, therapyStartedDate, therapyEndedDate,
                responseStatus, toxicityGrade, toxicityDescription, diseaseStatus,
                notes, nextFollowupDate);
        
        return ResponseEntity.ok(followUp);
    }
    
    /**
     * Get all follow-ups for a patient
     */
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<List<FollowUp>> getPatientFollowUps(@PathVariable Long patientId) {
        List<FollowUp> followUps = followUpService.getPatientFollowUps(patientId);
        return ResponseEntity.ok(followUps);
    }
    
    /**
     * Get follow-ups for a specific therapy
     */
    @GetMapping("/therapy/{therapyId}")
    public ResponseEntity<List<FollowUp>> getTherapyFollowUps(@PathVariable Long therapyId) {
        List<FollowUp> followUps = followUpService.getTherapyFollowUps(therapyId);
        return ResponseEntity.ok(followUps);
    }
    
    /**
     * Get follow-ups by response status
     */
    @GetMapping("/by-response")
    public ResponseEntity<List<FollowUp>> getFollowUpsByResponseStatus(
            @RequestParam String responseStatus) {
        
        List<FollowUp> followUps = followUpService.getFollowUpsByResponseStatus(responseStatus);
        return ResponseEntity.ok(followUps);
    }
    
    /**
     * Get follow-ups by disease status
     */
    @GetMapping("/by-disease-status")
    public ResponseEntity<List<FollowUp>> getFollowUpsByDiseaseStatus(
            @RequestParam String diseaseStatus) {
        
        List<FollowUp> followUps = followUpService.getFollowUpsByDiseaseStatus(diseaseStatus);
        return ResponseEntity.ok(followUps);
    }
    
    /**
     * Get follow-ups within a date range
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<List<FollowUp>> getFollowUpsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<FollowUp> followUps = followUpService.getFollowUpsInDateRange(startDate, endDate);
        return ResponseEntity.ok(followUps);
    }
    
    /**
     * Get follow-ups for patients with a specific cancer type
     */
    @GetMapping("/by-cancer-type")
    public ResponseEntity<List<FollowUp>> getFollowUpsByCancerType(
            @RequestParam String cancerType) {
        
        List<FollowUp> followUps = followUpService.getFollowUpsByCancerType(cancerType);
        return ResponseEntity.ok(followUps);
    }
    
    /**
     * Get follow-ups for patients with variants in a specific gene
     */
    @GetMapping("/by-gene")
    public ResponseEntity<List<FollowUp>> getFollowUpsByGene(
            @RequestParam String geneSymbol) {
        
        List<FollowUp> followUps = followUpService.getFollowUpsByGene(geneSymbol);
        return ResponseEntity.ok(followUps);
    }
}
