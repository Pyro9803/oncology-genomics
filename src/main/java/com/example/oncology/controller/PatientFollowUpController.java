package com.example.oncology.controller;

import com.example.oncology.entity.PatientFollowUp;
import com.example.oncology.service.PatientFollowUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for patient follow-up operations
 */
@RestController
@RequestMapping("/patient-follow-ups")
@RequiredArgsConstructor
@Slf4j
public class PatientFollowUpController {

    private final PatientFollowUpService followUpService;

    /**
     * Create a new follow-up record
     */
    @PostMapping
    public ResponseEntity<PatientFollowUp> createFollowUp(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long therapyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate followUpDate,
            @RequestParam String clinicalStatus,
            @RequestParam(required = false) String responseAssessment,
            @RequestParam(required = false) String imagingResults,
            @RequestParam(required = false) String laboratoryResults,
            @RequestParam(required = false) Double tumorSizeChange,
            @RequestParam(required = false) String adverseEvents,
            @RequestParam(required = false) String performanceStatus,
            @RequestParam(required = false) String qualityOfLife,
            @RequestParam(required = false) String clinicalNotes,
            @RequestParam(required = false, defaultValue = "false") Boolean requiresNewBiopsy,
            @RequestParam(required = false, defaultValue = "false") Boolean requiresNewSequencing,
            @RequestParam String recordedBy) {

        PatientFollowUp followUp = followUpService.createFollowUp(
                patientId, therapyId, followUpDate, clinicalStatus, responseAssessment,
                imagingResults, laboratoryResults, tumorSizeChange, adverseEvents,
                performanceStatus, qualityOfLife, clinicalNotes,
                requiresNewBiopsy, requiresNewSequencing, recordedBy);

        return ResponseEntity.ok(followUp);
    }

    /**
     * Update an existing follow-up record
     */
    @PutMapping("/{followUpId}")
    public ResponseEntity<PatientFollowUp> updateFollowUp(
            @PathVariable Long followUpId,
            @RequestParam(required = false) Long therapyId,
            @RequestParam(required = false) String clinicalStatus,
            @RequestParam(required = false) String responseAssessment,
            @RequestParam(required = false) String imagingResults,
            @RequestParam(required = false) String laboratoryResults,
            @RequestParam(required = false) Double tumorSizeChange,
            @RequestParam(required = false) String adverseEvents,
            @RequestParam(required = false) String performanceStatus,
            @RequestParam(required = false) String qualityOfLife,
            @RequestParam(required = false) String clinicalNotes,
            @RequestParam(required = false) Boolean requiresNewBiopsy,
            @RequestParam(required = false) Boolean requiresNewSequencing,
            @RequestParam String recordedBy) {

        PatientFollowUp followUp = followUpService.updateFollowUp(
                followUpId, therapyId, clinicalStatus, responseAssessment,
                imagingResults, laboratoryResults, tumorSizeChange, adverseEvents,
                performanceStatus, qualityOfLife, clinicalNotes,
                requiresNewBiopsy, requiresNewSequencing, recordedBy);

        return ResponseEntity.ok(followUp);
    }

    /**
     * Get all follow-up records for a patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientFollowUp>> getPatientFollowUps(@PathVariable Long patientId) {
        List<PatientFollowUp> followUps = followUpService.getPatientFollowUps(patientId);
        return ResponseEntity.ok(followUps);
    }

    /**
     * Get follow-up records for a patient within a date range
     */
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<List<PatientFollowUp>> getPatientFollowUpsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PatientFollowUp> followUps = followUpService.getPatientFollowUpsByDateRange(
                patientId, startDate, endDate);
        return ResponseEntity.ok(followUps);
    }

    /**
     * Get follow-up records for a specific therapy
     */
    @GetMapping("/therapy/{therapyId}")
    public ResponseEntity<List<PatientFollowUp>> getFollowUpsByTherapy(@PathVariable Long therapyId) {
        List<PatientFollowUp> followUps = followUpService.getFollowUpsByTherapy(therapyId);
        return ResponseEntity.ok(followUps);
    }

    /**
     * Get follow-up records by clinical status
     */
    @GetMapping("/patient/{patientId}/status/{clinicalStatus}")
    public ResponseEntity<List<PatientFollowUp>> getFollowUpsByClinicalStatus(
            @PathVariable Long patientId,
            @PathVariable String clinicalStatus) {

        List<PatientFollowUp> followUps = followUpService.getFollowUpsByClinicalStatus(
                patientId, clinicalStatus);
        return ResponseEntity.ok(followUps);
    }

    /**
     * Get follow-up records that require new biopsy
     */
    @GetMapping("/requiring-biopsy")
    public ResponseEntity<List<PatientFollowUp>> getFollowUpsRequiringBiopsy() {
        List<PatientFollowUp> followUps = followUpService.getFollowUpsRequiringBiopsy();
        return ResponseEntity.ok(followUps);
    }

    /**
     * Get follow-up records that require new sequencing
     */
    @GetMapping("/requiring-sequencing")
    public ResponseEntity<List<PatientFollowUp>> getFollowUpsRequiringSequencing() {
        List<PatientFollowUp> followUps = followUpService.getFollowUpsRequiringSequencing();
        return ResponseEntity.ok(followUps);
    }

    /**
     * Delete a follow-up record
     */
    @DeleteMapping("/{followUpId}")
    public ResponseEntity<Void> deleteFollowUp(@PathVariable Long followUpId) {
        followUpService.deleteFollowUp(followUpId);
        return ResponseEntity.noContent().build();
    }
}
